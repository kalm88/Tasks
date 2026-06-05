using System.Net.Http.Json;
using System.Text.Json;
using PunchlistApp.Models;

namespace PunchlistApp.Services;

/// <summary>Cloud Firestore via REST API — no native SDK required.</summary>
public sealed class FirestoreRestService : IFirestoreService
{
    private readonly HttpClient _http;
    private readonly IAuthService _auth;

    private string Root =>
        $"https://firestore.googleapis.com/v1/projects/{FirebaseConfig.ProjectId}/databases/(default)/documents";

    public FirestoreRestService(HttpClient http, IAuthService auth)
    {
        _http = http;
        _auth = auth;
    }

    // ── HTTP helpers ───────────────────────────────────────────────────────

    private HttpRequestMessage Req(HttpMethod method, string path, object? body = null)
    {
        var req = new HttpRequestMessage(method, path);
        if (_auth.GetIdToken() is { } t)
            req.Headers.Authorization = new("Bearer", t);
        if (body != null)
            req.Content = JsonContent.Create(body);
        return req;
    }

    private async Task<JsonElement> SendAsync(HttpRequestMessage req)
    {
        var resp = await _http.SendAsync(req);
        var text = await resp.Content.ReadAsStringAsync();
        if (!resp.IsSuccessStatusCode)
            throw new Exception($"Firestore {resp.StatusCode}: {text}");
        return JsonDocument.Parse(text).RootElement.Clone();
    }

    // ── Document ID extraction ─────────────────────────────────────────────

    private static string IdFromName(JsonElement doc) =>
        doc.GetProperty("name").GetString()!.Split('/').Last();

    // ── Firestore value builders ───────────────────────────────────────────

    private static object Str(string v) => new { stringValue = v };
    private static object Int(int v) => new { integerValue = v.ToString() };
    private static object Bool(bool v) => new { booleanValue = v };
    private static object Ts(DateTime v) =>
        new { timestampValue = v.ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ss.fffZ") };
    private static object Arr(IEnumerable<string> list) =>
        new { arrayValue = new { values = list.Select(s => new { stringValue = s }).ToArray() } };

    // ── Firestore value readers ────────────────────────────────────────────

    private static string RS(JsonElement f, string k)
    {
        if (f.TryGetProperty(k, out var v) && v.TryGetProperty("stringValue", out var sv))
            return sv.GetString() ?? "";
        return "";
    }

    private static int RI(JsonElement f, string k, int def = 0)
    {
        if (f.TryGetProperty(k, out var v) && v.TryGetProperty("integerValue", out var iv))
            return int.TryParse(iv.GetString(), out var i) ? i : def;
        return def;
    }

    private static DateTime RD(JsonElement f, string k)
    {
        if (f.TryGetProperty(k, out var v) && v.TryGetProperty("timestampValue", out var tv))
            return DateTime.TryParse(tv.GetString(), out var dt) ? dt.ToUniversalTime() : DateTime.UtcNow;
        return DateTime.UtcNow;
    }

    private static List<string> RL(JsonElement f, string k)
    {
        if (!f.TryGetProperty(k, out var v)) return [];
        if (!v.TryGetProperty("arrayValue", out var av)) return [];
        if (!av.TryGetProperty("values", out var vals)) return [];
        return vals.EnumerateArray()
            .Where(e => e.TryGetProperty("stringValue", out _))
            .Select(e => e.GetProperty("stringValue").GetString() ?? "")
            .ToList();
    }

    // ── runQuery helper ────────────────────────────────────────────────────

    private async Task<List<JsonElement>> QueryAsync(string parentPath, object query)
    {
        var url = string.IsNullOrEmpty(parentPath)
            ? $"{Root}:runQuery"
            : $"{Root}/{parentPath}:runQuery";
        var resp = await SendAsync(Req(HttpMethod.Post, url, new { structuredQuery = query }));
        return resp.EnumerateArray()
            .Where(e => e.TryGetProperty("document", out _))
            .Select(e => e.GetProperty("document").Clone())
            .ToList();
    }

    // ── Users ──────────────────────────────────────────────────────────────

    public async Task SetUserAsync(User u)
    {
        await SendAsync(Req(HttpMethod.Patch, $"{Root}/users/{u.Id}", new
        {
            fields = new
            {
                id = Str(u.Id), email = Str(u.Email), displayName = Str(u.DisplayName),
                role = Str(u.Role.ToString()), createdAt = Ts(u.CreatedAt)
            }
        }));
    }

    public async Task<User?> GetUserAsync(string userId)
    {
        try
        {
            var doc = await SendAsync(Req(HttpMethod.Get, $"{Root}/users/{userId}"));
            return UserFromDoc(doc);
        }
        catch { return null; }
    }

    private static User UserFromDoc(JsonElement doc)
    {
        var f = doc.GetProperty("fields");
        return new User
        {
            Id = RS(f, "id"), Email = RS(f, "email"), DisplayName = RS(f, "displayName"),
            Role = Enum.TryParse<Role>(RS(f, "role"), out var r) ? r : Role.Worker
        };
    }

    // ── Projects ───────────────────────────────────────────────────────────

    public async Task<string> CreateProjectAsync(Project p)
    {
        var doc = await SendAsync(Req(HttpMethod.Post, $"{Root}/projects", ProjectBody(p)));
        p.Id = IdFromName(doc);
        // Write members subcollection
        await SendAsync(Req(HttpMethod.Patch,
            $"{Root}/projects/{p.Id}/members/{p.CreatedByUserId}",
            new { fields = new { role = Str("admin") } }));
        return p.Id;
    }

    public async Task<List<Project>> GetProjectsAsync(string userId)
    {
        var docs = await QueryAsync("", new
        {
            from = new[] { new { collectionId = "projects" } },
            where = new
            {
                fieldFilter = new
                {
                    field = new { fieldPath = "createdByUserId" },
                    op = "EQUAL",
                    value = Str(userId)
                }
            },
            orderBy = new[] { new { field = new { fieldPath = "createdAt" }, direction = "DESCENDING" } }
        });
        return docs.Select(ProjectFromDoc).ToList();
    }

    private static object ProjectBody(Project p) => new
    {
        fields = new
        {
            id = Str(p.Id), name = Str(p.Name), description = Str(p.Description),
            createdByUserId = Str(p.CreatedByUserId), createdAt = Ts(p.CreatedAt),
            memberCount = Int(p.MemberCount)
        }
    };

    private static Project ProjectFromDoc(JsonElement doc)
    {
        var f = doc.GetProperty("fields");
        return new Project
        {
            Id = IdFromName(doc), Name = RS(f, "name"), Description = RS(f, "description"),
            CreatedByUserId = RS(f, "createdByUserId"),
            CreatedAt = RD(f, "createdAt"), MemberCount = RI(f, "memberCount", 1)
        };
    }

    // ── PunchItems ─────────────────────────────────────────────────────────

    public async Task<string> CreatePunchItemAsync(PunchItem item)
    {
        var doc = await SendAsync(Req(HttpMethod.Post,
            $"{Root}/projects/{item.ProjectId}/punchItems", PunchItemBody(item)));
        item.Id = IdFromName(doc);
        return item.Id;
    }

    public async Task UpdatePunchItemAsync(PunchItem item)
    {
        await SendAsync(Req(HttpMethod.Patch,
            $"{Root}/projects/{item.ProjectId}/punchItems/{item.Id}", PunchItemBody(item)));
    }

    public async Task<List<PunchItem>> GetPunchItemsAsync(string projectId)
    {
        var docs = await QueryAsync($"projects/{projectId}", new
        {
            from = new[] { new { collectionId = "punchItems" } },
            orderBy = new[] { new { field = new { fieldPath = "createdAt" }, direction = "DESCENDING" } }
        });
        return docs.Select(d => PunchItemFromDoc(d, projectId)).ToList();
    }

    public async Task<PunchItem?> GetPunchItemAsync(string projectId, string itemId)
    {
        try
        {
            var doc = await SendAsync(Req(HttpMethod.Get,
                $"{Root}/projects/{projectId}/punchItems/{itemId}"));
            return PunchItemFromDoc(doc, projectId);
        }
        catch { return null; }
    }

    public async Task UpdatePunchItemStatusAsync(string projectId, string itemId, Status status)
    {
        var url = $"{Root}/projects/{projectId}/punchItems/{itemId}" +
                  "?updateMask.fieldPaths=status&updateMask.fieldPaths=updatedAt";
        await SendAsync(Req(HttpMethod.Patch, url, new
        {
            fields = new { status = Str(status.ToString()), updatedAt = Ts(DateTime.UtcNow) }
        }));
    }

    private static object PunchItemBody(PunchItem i) => new
    {
        fields = new
        {
            title = Str(i.Title), issueDescription = Str(i.IssueDescription),
            workRequired = Str(i.WorkRequired), location = Str(i.Location),
            priority = Str(i.Priority.ToString()), status = Str(i.Status.ToString()),
            assignedToUserId = Str(i.AssignedToUserId), assignedToUserName = Str(i.AssignedToUserName),
            createdByUserId = Str(i.CreatedByUserId), createdByUserName = Str(i.CreatedByUserName),
            createdAt = Ts(i.CreatedAt), updatedAt = Ts(i.UpdatedAt),
            photoUrls = Arr(i.PhotoUrls), completionPhotoUrls = Arr(i.CompletionPhotoUrls),
            commentCount = Int(i.CommentCount), sku = Str(i.Sku)
        }
    };

    private static PunchItem PunchItemFromDoc(JsonElement doc, string projectId)
    {
        var f = doc.GetProperty("fields");
        return new PunchItem
        {
            Id = IdFromName(doc), ProjectId = projectId,
            Title = RS(f, "title"), IssueDescription = RS(f, "issueDescription"),
            WorkRequired = RS(f, "workRequired"), Location = RS(f, "location"),
            Priority = Enum.TryParse<Priority>(RS(f, "priority"), out var pr) ? pr : Priority.Medium,
            Status = Enum.TryParse<Status>(RS(f, "status"), out var st) ? st : Status.Open,
            AssignedToUserId = RS(f, "assignedToUserId"), AssignedToUserName = RS(f, "assignedToUserName"),
            CreatedByUserId = RS(f, "createdByUserId"), CreatedByUserName = RS(f, "createdByUserName"),
            CreatedAt = RD(f, "createdAt"), UpdatedAt = RD(f, "updatedAt"),
            PhotoUrls = RL(f, "photoUrls"), CompletionPhotoUrls = RL(f, "completionPhotoUrls"),
            CommentCount = RI(f, "commentCount"), Sku = RS(f, "sku")
        };
    }

    // ── Comments ───────────────────────────────────────────────────────────

    public async Task<string> AddCommentAsync(Comment c)
    {
        var doc = await SendAsync(Req(HttpMethod.Post,
            $"{Root}/projects/{c.ProjectId}/punchItems/{c.ItemId}/comments",
            new
            {
                fields = new
                {
                    projectId = Str(c.ProjectId), itemId = Str(c.ItemId),
                    userId = Str(c.UserId), userName = Str(c.UserName),
                    text = Str(c.Text), createdAt = Ts(c.CreatedAt), photoUrl = Str(c.PhotoUrl)
                }
            }));
        c.Id = IdFromName(doc);
        return c.Id;
    }

    public async Task<List<Comment>> GetCommentsAsync(string projectId, string itemId)
    {
        var docs = await QueryAsync($"projects/{projectId}/punchItems/{itemId}", new
        {
            from = new[] { new { collectionId = "comments" } },
            orderBy = new[] { new { field = new { fieldPath = "createdAt" }, direction = "ASCENDING" } }
        });
        return docs.Select(d =>
        {
            var f = d.GetProperty("fields");
            return new Comment
            {
                Id = IdFromName(d), ProjectId = projectId, ItemId = itemId,
                UserId = RS(f, "userId"), UserName = RS(f, "userName"),
                Text = RS(f, "text"), CreatedAt = RD(f, "createdAt"), PhotoUrl = RS(f, "photoUrl")
            };
        }).ToList();
    }
}

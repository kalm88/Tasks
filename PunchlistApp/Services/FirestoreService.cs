using Plugin.Firebase.CloudFirestore;
using PunchlistApp.Models;

namespace PunchlistApp.Services;

public class FirestoreService : IFirestoreService
{
    private readonly IFirebaseFirestore _db;

    public FirestoreService(IFirebaseFirestore db)
    {
        _db = db;
    }

    // ── Users ──────────────────────────────────────────────────────────────

    public async Task SetUserAsync(User user)
    {
        await _db.GetCollection("users")
                 .GetDocument(user.Id)
                 .SetDataAsync(UserToDict(user));
    }

    public async Task<User?> GetUserAsync(string userId)
    {
        var snap = await _db.GetCollection("users").GetDocument(userId).GetDocumentSnapshotAsync<Dictionary<string, object>>();
        return snap.Exists ? DictToUser(userId, snap.Data!) : null;
    }

    // ── Projects ───────────────────────────────────────────────────────────

    public async Task<string> CreateProjectAsync(Project project)
    {
        var docRef = _db.GetCollection("projects").CreateDocument();
        project.Id = docRef.Id;
        await docRef.SetDataAsync(ProjectToDict(project));
        await docRef.GetCollection("members")
                    .GetDocument(project.CreatedByUserId)
                    .SetDataAsync(new Dictionary<string, object> { ["role"] = "admin" });
        return docRef.Id;
    }

    public async Task<List<Project>> GetProjectsAsync(string userId)
    {
        var snaps = await _db.GetCollection("projects")
                             .WhereEqualsTo("createdByUserId", userId)
                             .OrderByDescending("createdAt")
                             .GetDocumentSnapshotsAsync<Dictionary<string, object>>();
        return snaps.Select(s => DictToProject(s.Id, s.Data!)).ToList();
    }

    // ── PunchItems ─────────────────────────────────────────────────────────

    public async Task<string> CreatePunchItemAsync(PunchItem item)
    {
        var docRef = _db.GetCollection("projects")
                        .GetDocument(item.ProjectId)
                        .GetCollection("punchItems")
                        .CreateDocument();
        item.Id = docRef.Id;
        await docRef.SetDataAsync(PunchItemToDict(item));
        return docRef.Id;
    }

    public async Task UpdatePunchItemAsync(PunchItem item)
    {
        await _db.GetCollection("projects")
                 .GetDocument(item.ProjectId)
                 .GetCollection("punchItems")
                 .GetDocument(item.Id)
                 .SetDataAsync(PunchItemToDict(item));
    }

    public async Task<List<PunchItem>> GetPunchItemsAsync(string projectId)
    {
        var snaps = await _db.GetCollection("projects")
                             .GetDocument(projectId)
                             .GetCollection("punchItems")
                             .OrderByDescending("createdAt")
                             .GetDocumentSnapshotsAsync<Dictionary<string, object>>();
        return snaps.Select(s => DictToPunchItem(s.Id, projectId, s.Data!)).ToList();
    }

    public async Task<PunchItem?> GetPunchItemAsync(string projectId, string itemId)
    {
        var snap = await _db.GetCollection("projects")
                            .GetDocument(projectId)
                            .GetCollection("punchItems")
                            .GetDocument(itemId)
                            .GetDocumentSnapshotAsync<Dictionary<string, object>>();
        return snap.Exists ? DictToPunchItem(itemId, projectId, snap.Data!) : null;
    }

    public async Task UpdatePunchItemStatusAsync(string projectId, string itemId, Status status)
    {
        await _db.GetCollection("projects")
                 .GetDocument(projectId)
                 .GetCollection("punchItems")
                 .GetDocument(itemId)
                 .UpdateDataAsync(new Dictionary<string, object>
                 {
                     ["status"] = status.ToString(),
                     ["updatedAt"] = DateTime.UtcNow
                 });
    }

    // ── Comments ───────────────────────────────────────────────────────────

    public async Task<string> AddCommentAsync(Comment comment)
    {
        var docRef = _db.GetCollection("projects")
                        .GetDocument(comment.ItemId.Split('/')[0])
                        .GetCollection("punchItems")
                        .GetDocument(comment.ItemId)
                        .GetCollection("comments")
                        .CreateDocument();
        comment.Id = docRef.Id;
        await docRef.SetDataAsync(CommentToDict(comment));
        return docRef.Id;
    }

    public async Task<List<Comment>> GetCommentsAsync(string projectId, string itemId)
    {
        var snaps = await _db.GetCollection("projects")
                             .GetDocument(projectId)
                             .GetCollection("punchItems")
                             .GetDocument(itemId)
                             .GetCollection("comments")
                             .OrderByAscending("createdAt")
                             .GetDocumentSnapshotsAsync<Dictionary<string, object>>();
        return snaps.Select(s => DictToComment(s.Id, itemId, s.Data!)).ToList();
    }

    // ── Mapping helpers ────────────────────────────────────────────────────

    private static Dictionary<string, object> UserToDict(User u) => new()
    {
        ["id"] = u.Id, ["email"] = u.Email, ["displayName"] = u.DisplayName,
        ["role"] = u.Role.ToString(), ["createdAt"] = u.CreatedAt
    };

    private static User DictToUser(string id, Dictionary<string, object> d) => new()
    {
        Id = id, Email = d.GetStr("email"), DisplayName = d.GetStr("displayName"),
        Role = Enum.TryParse<Role>(d.GetStr("role"), out var r) ? r : Role.Worker
    };

    private static Dictionary<string, object> ProjectToDict(Project p) => new()
    {
        ["id"] = p.Id, ["name"] = p.Name, ["description"] = p.Description,
        ["createdByUserId"] = p.CreatedByUserId, ["createdAt"] = p.CreatedAt,
        ["memberCount"] = p.MemberCount
    };

    private static Project DictToProject(string id, Dictionary<string, object> d) => new()
    {
        Id = id, Name = d.GetStr("name"), Description = d.GetStr("description"),
        CreatedByUserId = d.GetStr("createdByUserId"),
        CreatedAt = d.GetDate("createdAt"), MemberCount = d.GetInt("memberCount", 1)
    };

    private static Dictionary<string, object> PunchItemToDict(PunchItem i) => new()
    {
        ["id"] = i.Id, ["projectId"] = i.ProjectId, ["title"] = i.Title,
        ["issueDescription"] = i.IssueDescription, ["workRequired"] = i.WorkRequired,
        ["location"] = i.Location, ["priority"] = i.Priority.ToString(),
        ["status"] = i.Status.ToString(), ["assignedToUserId"] = i.AssignedToUserId,
        ["assignedToUserName"] = i.AssignedToUserName, ["createdByUserId"] = i.CreatedByUserId,
        ["createdByUserName"] = i.CreatedByUserName, ["createdAt"] = i.CreatedAt,
        ["updatedAt"] = i.UpdatedAt, ["photoUrls"] = i.PhotoUrls,
        ["completionPhotoUrls"] = i.CompletionPhotoUrls, ["commentCount"] = i.CommentCount,
        ["sku"] = i.Sku
    };

    private static PunchItem DictToPunchItem(string id, string projectId, Dictionary<string, object> d) => new()
    {
        Id = id, ProjectId = projectId, Title = d.GetStr("title"),
        IssueDescription = d.GetStr("issueDescription"), WorkRequired = d.GetStr("workRequired"),
        Location = d.GetStr("location"),
        Priority = Enum.TryParse<Priority>(d.GetStr("priority"), out var pr) ? pr : Priority.Medium,
        Status = Enum.TryParse<Status>(d.GetStr("status"), out var st) ? st : Status.Open,
        AssignedToUserId = d.GetStr("assignedToUserId"), AssignedToUserName = d.GetStr("assignedToUserName"),
        CreatedByUserId = d.GetStr("createdByUserId"), CreatedByUserName = d.GetStr("createdByUserName"),
        CreatedAt = d.GetDate("createdAt"), UpdatedAt = d.GetDate("updatedAt"),
        PhotoUrls = d.GetStrList("photoUrls"), CompletionPhotoUrls = d.GetStrList("completionPhotoUrls"),
        CommentCount = d.GetInt("commentCount"), Sku = d.GetStr("sku")
    };

    private static Dictionary<string, object> CommentToDict(Comment c) => new()
    {
        ["id"] = c.Id, ["itemId"] = c.ItemId, ["userId"] = c.UserId,
        ["userName"] = c.UserName, ["text"] = c.Text, ["createdAt"] = c.CreatedAt,
        ["photoUrl"] = c.PhotoUrl
    };

    private static Comment DictToComment(string id, string itemId, Dictionary<string, object> d) => new()
    {
        Id = id, ItemId = itemId, UserId = d.GetStr("userId"), UserName = d.GetStr("userName"),
        Text = d.GetStr("text"), CreatedAt = d.GetDate("createdAt"), PhotoUrl = d.GetStr("photoUrl")
    };
}

internal static class DictExtensions
{
    public static string GetStr(this Dictionary<string, object> d, string key) =>
        d.TryGetValue(key, out var v) ? v?.ToString() ?? "" : "";

    public static int GetInt(this Dictionary<string, object> d, string key, int def = 0) =>
        d.TryGetValue(key, out var v) && int.TryParse(v?.ToString(), out var i) ? i : def;

    public static DateTime GetDate(this Dictionary<string, object> d, string key) =>
        d.TryGetValue(key, out var v) && v is DateTime dt ? dt : DateTime.UtcNow;

    public static List<string> GetStrList(this Dictionary<string, object> d, string key) =>
        d.TryGetValue(key, out var v) && v is IEnumerable<object> list
            ? list.Select(x => x?.ToString() ?? "").ToList()
            : [];
}

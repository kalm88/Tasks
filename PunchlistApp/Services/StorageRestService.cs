using System.Text.Json;

namespace PunchlistApp.Services;

/// <summary>Firebase Storage via REST API — no native SDK required.</summary>
public sealed class StorageRestService : IStorageService
{
    private readonly HttpClient _http;
    private readonly IAuthService _auth;

    private const string BaseUrl = "https://firebasestorage.googleapis.com/v0/b";

    public StorageRestService(HttpClient http, IAuthService auth)
    {
        _http = http;
        _auth = auth;
    }

    public async Task<string> UploadPhotoAsync(string projectId, string itemId, Stream imageStream, string fileName)
    {
        var storagePath = $"projects/{projectId}/punchItems/{itemId}/photos/{fileName}";
        var encodedPath = Uri.EscapeDataString(storagePath);
        var uploadUrl = $"{BaseUrl}/{FirebaseConfig.StorageBucket}/o?uploadType=media&name={encodedPath}";

        using var ms = new MemoryStream();
        await imageStream.CopyToAsync(ms);
        var bytes = ms.ToArray();

        var req = new HttpRequestMessage(HttpMethod.Post, uploadUrl);
        req.Content = new ByteArrayContent(bytes);
        req.Content.Headers.ContentType = new("image/jpeg");
        if (_auth.GetIdToken() is { } token)
            req.Headers.Authorization = new("Bearer", token);

        var resp = await _http.SendAsync(req);
        var text = await resp.Content.ReadAsStringAsync();
        if (!resp.IsSuccessStatusCode)
            throw new Exception($"Upload failed {resp.StatusCode}: {text}");

        using var doc = JsonDocument.Parse(text);
        var downloadToken = doc.RootElement.GetProperty("downloadTokens").GetString();

        return $"{BaseUrl}/{FirebaseConfig.StorageBucket}/o/{encodedPath}?alt=media&token={downloadToken}";
    }
}

using Plugin.Firebase.Storage;

namespace PunchlistApp.Services;

public class FirebaseStorageService : IStorageService
{
    private readonly IFirebaseStorage _storage;

    public FirebaseStorageService(IFirebaseStorage storage)
    {
        _storage = storage;
    }

    public async Task<string> UploadPhotoAsync(string projectId, string itemId, Stream imageStream, string fileName)
    {
        var path = $"projects/{projectId}/punchItems/{itemId}/photos/{fileName}";
        var reference = _storage.GetReferenceFromPath(path);

        using var ms = new MemoryStream();
        await imageStream.CopyToAsync(ms);
        await reference.PutBytesAsync(ms.ToArray());

        return await reference.GetDownloadUrlAsync();
    }
}

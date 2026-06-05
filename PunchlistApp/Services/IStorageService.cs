namespace PunchlistApp.Services;

public interface IStorageService
{
    Task<string> UploadPhotoAsync(string projectId, string itemId, Stream imageStream, string fileName);
}

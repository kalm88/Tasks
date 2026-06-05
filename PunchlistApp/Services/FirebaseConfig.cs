namespace PunchlistApp.Services;

/// <summary>
/// Fill these values from Firebase Console → Project Settings before building.
/// </summary>
public static class FirebaseConfig
{
    /// Firebase Console → Project Settings → General → Web API Key
    public const string WebApiKey = "YOUR_WEB_API_KEY";

    /// Firebase Console → Project Settings → General → Project ID
    public const string ProjectId = "YOUR_PROJECT_ID";

    /// Firebase Console → Storage → Files tab (strip the "gs://" prefix)
    public const string StorageBucket = "YOUR_PROJECT_ID.firebasestorage.app";
}

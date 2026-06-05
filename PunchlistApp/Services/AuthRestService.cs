using System.Net.Http.Json;
using System.Text.Json;
using PunchlistApp.Models;

namespace PunchlistApp.Services;

/// <summary>Firebase Authentication via Identity Toolkit REST API.</summary>
public sealed class AuthRestService : IAuthService
{
    private readonly HttpClient _http;
    private string? _idToken;
    private string? _userId;
    private string? _userEmail;

    private const string Base = "https://identitytoolkit.googleapis.com/v1/accounts";
    private static readonly JsonSerializerOptions Json = new() { PropertyNameCaseInsensitive = true };

    public AuthRestService(HttpClient http)
    {
        _http = http;
        _idToken = SecureStorage.Default.GetAsync("fb_token").GetAwaiter().GetResult();
        _userId = Preferences.Default.Get("fb_uid", (string?)null);
        _userEmail = Preferences.Default.Get("fb_email", (string?)null);
    }

    public string? CurrentUserId => _userId;
    public string? CurrentUserEmail => _userEmail;
    public bool IsLoggedIn => _idToken != null && _userId != null;
    public string? GetIdToken() => _idToken;

    public async Task<User> SignInAsync(string email, string password)
    {
        var r = await PostAsync($"{Base}:signInWithPassword", new { email, password, returnSecureToken = true });
        await SaveAsync(r);
        return new User { Id = r.LocalId, Email = r.Email, DisplayName = r.DisplayName ?? email };
    }

    public async Task<User> RegisterAsync(string email, string password, string displayName)
    {
        var r = await PostAsync($"{Base}:signUp", new { email, password, returnSecureToken = true });
        await SaveAsync(r);
        return new User { Id = r.LocalId, Email = email, DisplayName = displayName, CreatedAt = DateTime.UtcNow };
    }

    public Task SignOutAsync()
    {
        _idToken = _userId = _userEmail = null;
        SecureStorage.Default.Remove("fb_token");
        Preferences.Default.Remove("fb_uid");
        Preferences.Default.Remove("fb_email");
        return Task.CompletedTask;
    }

    private async Task<AuthResult> PostAsync(string url, object body)
    {
        var resp = await _http.PostAsJsonAsync($"{url}?key={FirebaseConfig.WebApiKey}", body);
        var text = await resp.Content.ReadAsStringAsync();
        if (!resp.IsSuccessStatusCode)
        {
            var err = JsonDocument.Parse(text).RootElement
                .GetProperty("error").GetProperty("message").GetString();
            throw new Exception(err ?? "Authentication failed");
        }
        return JsonSerializer.Deserialize<AuthResult>(text, Json)!;
    }

    private async Task SaveAsync(AuthResult r)
    {
        _idToken = r.IdToken; _userId = r.LocalId; _userEmail = r.Email;
        await SecureStorage.Default.SetAsync("fb_token", _idToken);
        Preferences.Default.Set("fb_uid", _userId);
        Preferences.Default.Set("fb_email", _userEmail);
    }

    private sealed record AuthResult(string IdToken, string LocalId, string Email, string? DisplayName);
}

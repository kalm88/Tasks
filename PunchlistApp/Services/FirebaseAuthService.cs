using Plugin.Firebase.Auth;
using PunchlistApp.Models;

namespace PunchlistApp.Services;

public class FirebaseAuthService : IAuthService
{
    private readonly IFirebaseAuth _auth;
    private readonly IFirestoreService _firestore;

    public FirebaseAuthService(IFirebaseAuth auth, IFirestoreService firestore)
    {
        _auth = auth;
        _firestore = firestore;
    }

    public string? CurrentUserId => _auth.CurrentUser?.Uid;
    public string? CurrentUserEmail => _auth.CurrentUser?.Email;
    public bool IsLoggedIn => _auth.CurrentUser != null;

    public async Task<User> SignInAsync(string email, string password)
    {
        var result = await _auth.SignInWithEmailAndPasswordAsync(email, password);
        return new User
        {
            Id = result.Uid,
            Email = result.Email ?? email,
            DisplayName = result.DisplayName ?? email
        };
    }

    public async Task<User> RegisterAsync(string email, string password, string displayName)
    {
        var result = await _auth.CreateUserWithEmailAndPasswordAsync(email, password);
        var user = new User
        {
            Id = result.Uid,
            Email = email,
            DisplayName = displayName,
            CreatedAt = DateTime.UtcNow
        };
        await _firestore.SetUserAsync(user);
        return user;
    }

    public async Task SignOutAsync()
    {
        await _auth.SignOutAsync();
    }
}

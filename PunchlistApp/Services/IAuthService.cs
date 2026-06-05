using PunchlistApp.Models;

namespace PunchlistApp.Services;

public interface IAuthService
{
    string? CurrentUserId { get; }
    string? CurrentUserEmail { get; }
    bool IsLoggedIn { get; }
    string? GetIdToken();

    Task<User> SignInAsync(string email, string password);
    Task<User> RegisterAsync(string email, string password, string displayName);
    Task SignOutAsync();
}

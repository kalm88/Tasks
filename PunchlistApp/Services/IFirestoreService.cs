using PunchlistApp.Models;

namespace PunchlistApp.Services;

public interface IFirestoreService
{
    Task SetUserAsync(User user);
    Task<User?> GetUserAsync(string userId);

    Task<string> CreateProjectAsync(Project project);
    Task<List<Project>> GetProjectsAsync(string userId);

    Task<string> CreatePunchItemAsync(PunchItem item);
    Task UpdatePunchItemAsync(PunchItem item);
    Task<List<PunchItem>> GetPunchItemsAsync(string projectId);
    Task<PunchItem?> GetPunchItemAsync(string projectId, string itemId);
    Task UpdatePunchItemStatusAsync(string projectId, string itemId, Status status);

    Task<string> AddCommentAsync(Comment comment);
    Task<List<Comment>> GetCommentsAsync(string projectId, string itemId);
}

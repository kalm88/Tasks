namespace PunchlistApp.Models;

public class User
{
    public string Id { get; set; } = "";
    public string Email { get; set; } = "";
    public string DisplayName { get; set; } = "";
    public string PhotoUrl { get; set; } = "";
    public Role Role { get; set; } = Role.Worker;
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
}

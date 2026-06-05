namespace PunchlistApp.Models;

public class Comment
{
    public string Id { get; set; } = "";
    public string ProjectId { get; set; } = "";
    public string ItemId { get; set; } = "";
    public string UserId { get; set; } = "";
    public string UserName { get; set; } = "";
    public string Text { get; set; } = "";
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
    public string PhotoUrl { get; set; } = "";
}

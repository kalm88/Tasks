namespace PunchlistApp.Models;

public class PunchItem
{
    public string Id { get; set; } = "";
    public string ProjectId { get; set; } = "";
    public string Title { get; set; } = "";
    public string IssueDescription { get; set; } = "";
    public string WorkRequired { get; set; } = "";
    public string Location { get; set; } = "";
    public Priority Priority { get; set; } = Priority.Medium;
    public Status Status { get; set; } = Status.Open;
    public string AssignedToUserId { get; set; } = "";
    public string AssignedToUserName { get; set; } = "";
    public string CreatedByUserId { get; set; } = "";
    public string CreatedByUserName { get; set; } = "";
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
    public DateTime UpdatedAt { get; set; } = DateTime.UtcNow;
    public DateTime? DueDate { get; set; }
    public List<string> PhotoUrls { get; set; } = [];
    public List<string> CompletionPhotoUrls { get; set; } = [];
    public int CommentCount { get; set; } = 0;
    public string Sku { get; set; } = "";
}

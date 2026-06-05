using Microsoft.Maui.Graphics;

namespace PunchlistApp.Models;

public enum Status
{
    Open,
    InProgress,
    NeedsReview,
    Complete
}

public static class StatusExtensions
{
    public static string Label(this Status s) => s switch
    {
        Status.Open => "Open",
        Status.InProgress => "In Progress",
        Status.NeedsReview => "Needs Review",
        Status.Complete => "Complete",
        _ => "Unknown"
    };

    public static Color Color(this Status s) => s switch
    {
        Status.Open => Microsoft.Maui.Graphics.Color.FromArgb("#2196F3"),
        Status.InProgress => Microsoft.Maui.Graphics.Color.FromArgb("#FF9800"),
        Status.NeedsReview => Microsoft.Maui.Graphics.Color.FromArgb("#9C27B0"),
        Status.Complete => Microsoft.Maui.Graphics.Color.FromArgb("#4CAF50"),
        _ => Microsoft.Maui.Graphics.Color.FromArgb("#9E9E9E")
    };
}

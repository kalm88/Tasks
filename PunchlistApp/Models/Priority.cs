using Microsoft.Maui.Graphics;

namespace PunchlistApp.Models;

public enum Priority
{
    Low,
    Medium,
    High,
    Urgent
}

public static class PriorityExtensions
{
    public static string Label(this Priority p) => p switch
    {
        Priority.Low => "Low",
        Priority.Medium => "Medium",
        Priority.High => "High",
        Priority.Urgent => "Urgent",
        _ => "Unknown"
    };

    public static Color Color(this Priority p) => p switch
    {
        Priority.Low => Microsoft.Maui.Graphics.Color.FromArgb("#4CAF50"),
        Priority.Medium => Microsoft.Maui.Graphics.Color.FromArgb("#FF9800"),
        Priority.High => Microsoft.Maui.Graphics.Color.FromArgb("#F44336"),
        Priority.Urgent => Microsoft.Maui.Graphics.Color.FromArgb("#9C27B0"),
        _ => Microsoft.Maui.Graphics.Color.FromArgb("#9E9E9E")
    };
}

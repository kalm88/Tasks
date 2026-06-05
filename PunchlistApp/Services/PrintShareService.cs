using PdfSharpCore.Drawing;
using PdfSharpCore.Pdf;
using PunchlistApp.Models;

namespace PunchlistApp.Services;

public class PrintShareService
{
    public async Task<string> GeneratePdfAsync(PunchItem item, string projectName, List<Comment> comments)
    {
        var doc = new PdfDocument();
        doc.Info.Title = item.Title;

        var page = doc.AddPage();
        page.Width = XUnit.FromPoint(612);
        page.Height = XUnit.FromPoint(792);
        var gfx = XGraphics.FromPdfPage(page);

        var blue = XColor.FromArgb(0xFF, 0x15, 0x65, 0xC0);
        var white = XColors.White;
        var darkGray = XColor.FromArgb(0xFF, 0x33, 0x33, 0x33);
        var lightGray = XColor.FromArgb(0xFF, 0xF5, 0xF5, 0xF5);

        var boldFont = new XFont("Arial", 14, XFontStyle.Bold);
        var headerFont = new XFont("Arial", 11, XFontStyle.Bold);
        var bodyFont = new XFont("Arial", 10, XFontStyle.Regular);
        var smallFont = new XFont("Arial", 8, XFontStyle.Regular);

        double margin = 36;
        double y = margin;
        double contentWidth = 612 - margin * 2;

        // Header bar
        gfx.DrawRectangle(new XSolidBrush(blue), margin, y, contentWidth, 48);
        gfx.DrawString("PUNCHLIST REPORT", new XFont("Arial", 16, XFontStyle.Bold),
            new XSolidBrush(white), new XRect(margin + 8, y, contentWidth, 48), XStringFormats.CenterLeft);
        gfx.DrawString(DateTime.Now.ToString("MMM dd, yyyy"), bodyFont,
            new XSolidBrush(white), new XRect(margin, y, contentWidth - 8, 48), XStringFormats.CenterRight);
        y += 56;

        gfx.DrawString(projectName, new XFont("Arial", 12, XFontStyle.Bold),
            new XSolidBrush(darkGray), margin, y + 12);
        y += 28;

        // Title
        gfx.DrawString(item.Title, boldFont, new XSolidBrush(darkGray), margin, y + 14);
        y += 32;

        // Status + Priority pills
        DrawPill(gfx, item.Status.Label(), item.Status.Color(), margin, y);
        DrawPill(gfx, item.Priority.Label(), item.Priority.Color(), margin + 90, y);
        if (!string.IsNullOrEmpty(item.Sku))
        {
            gfx.DrawString($"SKU: {item.Sku}", bodyFont, new XSolidBrush(darkGray), margin + 190, y + 5);
        }
        y += 28;

        // Section helper
        void DrawSection(string label, string value)
        {
            if (string.IsNullOrWhiteSpace(value)) return;
            gfx.DrawRectangle(new XSolidBrush(lightGray), margin, y, contentWidth, 18);
            gfx.DrawString(label.ToUpperInvariant(), smallFont, new XSolidBrush(XColor.FromArgb(0xFF, 0x66, 0x66, 0x66)), margin + 4, y + 13);
            y += 20;
            var lines = WrapText(value, bodyFont, gfx, contentWidth - 8);
            foreach (var line in lines)
            {
                gfx.DrawString(line, bodyFont, new XSolidBrush(darkGray), margin + 4, y + 12);
                y += 16;
            }
            y += 8;
        }

        DrawSection("Issue Description", item.IssueDescription);
        DrawSection("Work Required", item.WorkRequired);
        DrawSection("Location", item.Location);

        // Meta grid
        gfx.DrawRectangle(new XSolidBrush(lightGray), margin, y, contentWidth, 18);
        gfx.DrawString("DETAILS", smallFont, new XSolidBrush(XColor.FromArgb(0xFF, 0x66, 0x66, 0x66)), margin + 4, y + 13);
        y += 22;
        DrawMeta(gfx, "Assigned To", item.AssignedToUserName, "Created By", item.CreatedByUserName, bodyFont, margin, y, contentWidth);
        y += 18;
        DrawMeta(gfx, "Created", item.CreatedAt.ToString("MMM dd, yyyy"), "Due Date", item.DueDate?.ToString("MMM dd, yyyy") ?? "—", bodyFont, margin, y, contentWidth);
        y += 26;

        // Comments
        if (comments.Count > 0)
        {
            gfx.DrawRectangle(new XSolidBrush(lightGray), margin, y, contentWidth, 18);
            gfx.DrawString("COMMENTS", smallFont, new XSolidBrush(XColor.FromArgb(0xFF, 0x66, 0x66, 0x66)), margin + 4, y + 13);
            y += 22;
            foreach (var c in comments)
            {
                gfx.DrawString($"{c.UserName}  •  {c.CreatedAt:MMM dd}", smallFont,
                    new XSolidBrush(XColor.FromArgb(0xFF, 0x88, 0x88, 0x88)), margin + 4, y + 10);
                y += 14;
                var lines = WrapText(c.Text, bodyFont, gfx, contentWidth - 8);
                foreach (var line in lines)
                {
                    gfx.DrawString(line, bodyFont, new XSolidBrush(darkGray), margin + 4, y + 12);
                    y += 16;
                }
                y += 6;
            }
        }

        var path = Path.Combine(FileSystem.CacheDirectory, $"punchlist_{item.Id}.pdf");
        doc.Save(path);
        return path;
    }

    public async Task SharePdfAsync(string filePath, string title)
    {
        await Share.Default.RequestAsync(new ShareFileRequest
        {
            Title = title,
            File = new ShareFile(filePath, "application/pdf")
        });
    }

    public async Task ShareViaEmailAsync(PunchItem item, string projectName)
    {
        var subject = Uri.EscapeDataString($"Punchlist: {item.Title} — {projectName}");
        var body = Uri.EscapeDataString(
            $"Punchlist Item: {item.Title}\n" +
            $"Project: {projectName}\n" +
            $"Status: {item.Status.Label()}\n" +
            $"Priority: {item.Priority.Label()}\n" +
            $"Location: {item.Location}\n\n" +
            $"Issue: {item.IssueDescription}\n\n" +
            $"Work Required: {item.WorkRequired}");
        await Launcher.Default.OpenAsync($"mailto:?subject={subject}&body={body}");
    }

    private static void DrawPill(XGraphics gfx, string text, Microsoft.Maui.Graphics.Color mauiColor, double x, double y)
    {
        var color = XColor.FromArgb(
            (int)(mauiColor.Alpha * 255),
            (int)(mauiColor.Red * 255),
            (int)(mauiColor.Green * 255),
            (int)(mauiColor.Blue * 255));
        gfx.DrawRoundedRectangle(new XSolidBrush(color), x, y, 80, 18, 9, 9);
        gfx.DrawString(text, new XFont("Arial", 8, XFontStyle.Bold), new XSolidBrush(XColors.White),
            new XRect(x, y, 80, 18), XStringFormats.Center);
    }

    private static void DrawMeta(XGraphics gfx, string l1, string v1, string l2, string v2,
        XFont font, double margin, double y, double width)
    {
        double col = width / 2;
        gfx.DrawString($"{l1}: {v1}", font, new XSolidBrush(XColor.FromArgb(0xFF, 0x33, 0x33, 0x33)), margin + 4, y + 12);
        gfx.DrawString($"{l2}: {v2}", font, new XSolidBrush(XColor.FromArgb(0xFF, 0x33, 0x33, 0x33)), margin + col, y + 12);
    }

    private static List<string> WrapText(string text, XFont font, XGraphics gfx, double maxWidth)
    {
        var words = text.Split(' ');
        var lines = new List<string>();
        var line = "";
        foreach (var word in words)
        {
            var test = line.Length == 0 ? word : line + " " + word;
            if (gfx.MeasureString(test, font).Width > maxWidth)
            {
                if (line.Length > 0) lines.Add(line);
                line = word;
            }
            else
            {
                line = test;
            }
        }
        if (line.Length > 0) lines.Add(line);
        return lines;
    }
}

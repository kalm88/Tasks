using System.Collections.ObjectModel;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using PunchlistApp.Models;
using PunchlistApp.Services;

namespace PunchlistApp.ViewModels;

[QueryProperty(nameof(ProjectId), "projectId")]
[QueryProperty(nameof(ItemId), "itemId")]
[QueryProperty(nameof(ProjectName), "projectName")]
public partial class PunchItemDetailViewModel : BaseViewModel
{
    private readonly IFirestoreService _firestore;
    private readonly PrintShareService _printShare;

    [ObservableProperty] private string _projectId = "";
    [ObservableProperty] private string _itemId = "";
    [ObservableProperty] private string _projectName = "";
    [ObservableProperty] private PunchItem? _item;
    [ObservableProperty] private ObservableCollection<Comment> _comments = [];
    [ObservableProperty] private string _newCommentText = "";

    public List<Status> Statuses { get; } = [Status.Open, Status.InProgress, Status.NeedsReview, Status.Complete];

    public PunchItemDetailViewModel(IFirestoreService firestore, PrintShareService printShare)
    {
        _firestore = firestore;
        _printShare = printShare;
    }

    partial void OnItemIdChanged(string value)
    {
        if (!string.IsNullOrEmpty(value) && !string.IsNullOrEmpty(ProjectId))
            LoadCommand.Execute(null);
    }

    partial void OnProjectIdChanged(string value)
    {
        if (!string.IsNullOrEmpty(value) && !string.IsNullOrEmpty(ItemId))
            LoadCommand.Execute(null);
    }

    [RelayCommand]
    private async Task LoadAsync()
    {
        await RunAsync(async () =>
        {
            Item = await _firestore.GetPunchItemAsync(ProjectId, ItemId);
            var comments = await _firestore.GetCommentsAsync(ProjectId, ItemId);
            Comments = new ObservableCollection<Comment>(comments);
        });
    }

    [RelayCommand]
    private async Task UpdateStatusAsync(Status status)
    {
        await RunAsync(async () =>
        {
            await _firestore.UpdatePunchItemStatusAsync(ProjectId, ItemId, status);
            if (Item != null) Item.Status = status;
            OnPropertyChanged(nameof(Item));
        });
    }

    [RelayCommand]
    private async Task AddCommentAsync()
    {
        if (string.IsNullOrWhiteSpace(NewCommentText)) return;
        await RunAsync(async () =>
        {
            var comment = new Comment
            {
                ProjectId = ProjectId,
                ItemId = ItemId,
                Text = NewCommentText.Trim(),
                CreatedAt = DateTime.UtcNow
            };
            await _firestore.AddCommentAsync(comment);
            Comments.Add(comment);
            NewCommentText = "";
        });
    }

    [RelayCommand]
    private async Task SharePdfAsync()
    {
        if (Item == null) return;
        await RunAsync(async () =>
        {
            var path = await _printShare.GeneratePdfAsync(Item, ProjectName, Comments.ToList());
            await _printShare.SharePdfAsync(path, $"Punchlist — {Item.Title}");
        });
    }

    [RelayCommand]
    private async Task ShareEmailAsync()
    {
        if (Item == null) return;
        await _printShare.ShareViaEmailAsync(Item, ProjectName);
    }

    [RelayCommand]
    private async Task BackAsync() => await Shell.Current.GoToAsync("..");
}

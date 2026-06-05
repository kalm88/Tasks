using System.Collections.ObjectModel;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using PunchlistApp.Models;
using PunchlistApp.Services;

namespace PunchlistApp.ViewModels;

[QueryProperty(nameof(ProjectId), "projectId")]
[QueryProperty(nameof(ProjectName), "projectName")]
public partial class PunchItemFeedViewModel : BaseViewModel
{
    private readonly IFirestoreService _firestore;

    [ObservableProperty] private string _projectId = "";
    [ObservableProperty] private string _projectName = "";
    [ObservableProperty] private ObservableCollection<PunchItem> _items = [];
    [ObservableProperty] private bool _isEmpty;

    public PunchItemFeedViewModel(IFirestoreService firestore)
    {
        _firestore = firestore;
    }

    partial void OnProjectIdChanged(string value)
    {
        if (!string.IsNullOrEmpty(value))
            LoadCommand.Execute(null);
    }

    [RelayCommand]
    private async Task LoadAsync()
    {
        await RunAsync(async () =>
        {
            var list = await _firestore.GetPunchItemsAsync(ProjectId);
            Items = new ObservableCollection<PunchItem>(list);
            IsEmpty = Items.Count == 0;
        });
    }

    [RelayCommand]
    private async Task OpenItemAsync(PunchItem item)
    {
        await Shell.Current.GoToAsync(
            $"detail?projectId={ProjectId}&itemId={item.Id}&projectName={Uri.EscapeDataString(ProjectName)}");
    }

    [RelayCommand]
    private async Task CreateItemAsync()
    {
        await Shell.Current.GoToAsync($"createItem?projectId={ProjectId}");
    }

    [RelayCommand]
    private async Task BackAsync() => await Shell.Current.GoToAsync("..");
}

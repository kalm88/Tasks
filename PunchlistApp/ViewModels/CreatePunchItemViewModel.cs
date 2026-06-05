using System.Collections.ObjectModel;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using PunchlistApp.Models;
using PunchlistApp.Services;

namespace PunchlistApp.ViewModels;

[QueryProperty(nameof(ProjectId), "projectId")]
[QueryProperty(nameof(ScannedSku), "scannedSku")]
public partial class CreatePunchItemViewModel : BaseViewModel
{
    private readonly IFirestoreService _firestore;
    private readonly IStorageService _storage;
    private readonly IAuthService _auth;

    [ObservableProperty] private string _projectId = "";
    [ObservableProperty] private string _title = "";
    [ObservableProperty] private string _issueDescription = "";
    [ObservableProperty] private string _workRequired = "";
    [ObservableProperty] private string _location = "";
    [ObservableProperty] private string _sku = "";
    [ObservableProperty] private Priority _selectedPriority = Priority.Medium;
    [ObservableProperty] private ObservableCollection<string> _localPhotoUris = [];

    public List<Priority> Priorities { get; } = [Priority.Low, Priority.Medium, Priority.High, Priority.Urgent];

    partial void OnScannedSkuChanged(string value)
    {
        if (!string.IsNullOrEmpty(value)) Sku = value;
    }

    public string? ScannedSku { get; set; }

    public CreatePunchItemViewModel(IFirestoreService firestore, IStorageService storage, IAuthService auth)
    {
        _firestore = firestore;
        _storage = storage;
        _auth = auth;
    }

    [RelayCommand]
    private async Task TakePhotoAsync()
    {
        var result = await MediaPicker.Default.CapturePhotoAsync();
        if (result != null)
            LocalPhotoUris.Add(result.FullPath);
    }

    [RelayCommand]
    private async Task ScanBarcodeAsync()
    {
        await Shell.Current.GoToAsync($"scanner?returnTo=createItem&projectId={ProjectId}");
    }

    [RelayCommand]
    private async Task SaveAsync()
    {
        await RunAsync(async () =>
        {
            if (string.IsNullOrWhiteSpace(Title)) throw new Exception("Title is required.");
            var userId = _auth.CurrentUserId ?? throw new Exception("Not logged in.");
            var tempId = Guid.NewGuid().ToString();

            var photoUrls = new List<string>();
            foreach (var uri in LocalPhotoUris)
            {
                using var stream = File.OpenRead(uri);
                var url = await _storage.UploadPhotoAsync(ProjectId, tempId, stream, Path.GetFileName(uri));
                photoUrls.Add(url);
            }

            var item = new PunchItem
            {
                ProjectId = ProjectId,
                Title = Title.Trim(),
                IssueDescription = IssueDescription.Trim(),
                WorkRequired = WorkRequired.Trim(),
                Location = Location.Trim(),
                Priority = SelectedPriority,
                CreatedByUserId = userId,
                PhotoUrls = photoUrls,
                Sku = Sku.Trim(),
                CreatedAt = DateTime.UtcNow,
                UpdatedAt = DateTime.UtcNow
            };

            await _firestore.CreatePunchItemAsync(item);
            await Shell.Current.GoToAsync("..");
        });
    }

    [RelayCommand]
    private async Task BackAsync() => await Shell.Current.GoToAsync("..");
}

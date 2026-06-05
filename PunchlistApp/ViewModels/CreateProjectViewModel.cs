using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using PunchlistApp.Models;
using PunchlistApp.Services;

namespace PunchlistApp.ViewModels;

public partial class CreateProjectViewModel : BaseViewModel
{
    private readonly IFirestoreService _firestore;
    private readonly IAuthService _auth;

    [ObservableProperty] private string _name = "";
    [ObservableProperty] private string _description = "";

    public CreateProjectViewModel(IFirestoreService firestore, IAuthService auth)
    {
        _firestore = firestore;
        _auth = auth;
    }

    [RelayCommand]
    private async Task CreateAsync()
    {
        await RunAsync(async () =>
        {
            if (string.IsNullOrWhiteSpace(Name)) throw new Exception("Project name is required.");
            var userId = _auth.CurrentUserId ?? throw new Exception("Not logged in.");
            var project = new Project
            {
                Name = Name.Trim(),
                Description = Description.Trim(),
                CreatedByUserId = userId,
                CreatedAt = DateTime.UtcNow
            };
            var id = await _firestore.CreateProjectAsync(project);
            await Shell.Current.GoToAsync($"..?refresh=true");
        });
    }

    [RelayCommand]
    private async Task BackAsync() => await Shell.Current.GoToAsync("..");
}

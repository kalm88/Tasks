using System.Collections.ObjectModel;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using PunchlistApp.Models;
using PunchlistApp.Services;

namespace PunchlistApp.ViewModels;

public partial class ProjectListViewModel : BaseViewModel
{
    private readonly IFirestoreService _firestore;
    private readonly IAuthService _auth;

    [ObservableProperty] private ObservableCollection<Project> _projects = [];
    [ObservableProperty] private bool _isEmpty;

    public ProjectListViewModel(IFirestoreService firestore, IAuthService auth)
    {
        _firestore = firestore;
        _auth = auth;
    }

    [RelayCommand]
    private async Task LoadProjectsAsync()
    {
        await RunAsync(async () =>
        {
            var userId = _auth.CurrentUserId ?? throw new Exception("Not logged in.");
            var list = await _firestore.GetProjectsAsync(userId);
            Projects = new ObservableCollection<Project>(list);
            IsEmpty = Projects.Count == 0;
        });
    }

    [RelayCommand]
    private async Task OpenProjectAsync(Project project)
    {
        await Shell.Current.GoToAsync($"feed?projectId={project.Id}&projectName={Uri.EscapeDataString(project.Name)}");
    }

    [RelayCommand]
    private async Task CreateProjectAsync()
    {
        await Shell.Current.GoToAsync("createProject");
    }

    [RelayCommand]
    private async Task SignOutAsync()
    {
        await _auth.SignOutAsync();
        await Shell.Current.GoToAsync("//login");
    }
}

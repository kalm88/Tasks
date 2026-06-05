using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using PunchlistApp.Services;

namespace PunchlistApp.ViewModels;

public partial class RegisterViewModel : BaseViewModel
{
    private readonly IAuthService _auth;

    [ObservableProperty] private string _email = "";
    [ObservableProperty] private string _password = "";
    [ObservableProperty] private string _displayName = "";

    public RegisterViewModel(IAuthService auth)
    {
        _auth = auth;
    }

    [RelayCommand]
    private async Task RegisterAsync()
    {
        await RunAsync(async () =>
        {
            if (string.IsNullOrWhiteSpace(DisplayName))
                throw new Exception("Display name is required.");
            await _auth.RegisterAsync(Email, Password, DisplayName);
            await Shell.Current.GoToAsync("//projects");
        });
    }

    [RelayCommand]
    private async Task GoToLoginAsync()
    {
        await Shell.Current.GoToAsync("..");
    }
}

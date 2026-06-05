using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using PunchlistApp.Services;

namespace PunchlistApp.ViewModels;

public partial class LoginViewModel : BaseViewModel
{
    private readonly IAuthService _auth;

    [ObservableProperty] private string _email = "";
    [ObservableProperty] private string _password = "";

    public LoginViewModel(IAuthService auth)
    {
        _auth = auth;
    }

    [RelayCommand]
    private async Task LoginAsync()
    {
        await RunAsync(async () =>
        {
            await _auth.SignInAsync(Email, Password);
            await Shell.Current.GoToAsync("//projects");
        });
    }

    [RelayCommand]
    private async Task GoToRegisterAsync()
    {
        await Shell.Current.GoToAsync("register");
    }
}

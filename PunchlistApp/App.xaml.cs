using PunchlistApp.Services;

namespace PunchlistApp;

public partial class App : Application
{
    public App(IAuthService auth, AppShell shell)
    {
        InitializeComponent();
        MainPage = shell;

        // Route to login if not signed in
        if (!auth.IsLoggedIn)
        {
            Shell.Current.GoToAsync("//login");
        }
        else
        {
            Shell.Current.GoToAsync("//projects");
        }
    }
}

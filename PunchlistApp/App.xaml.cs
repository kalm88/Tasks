using PunchlistApp.Services;

namespace PunchlistApp;

public partial class App : Application
{
    private readonly IAuthService _auth;
    private readonly AppShell _shell;

    public App(IAuthService auth, AppShell shell)
    {
        InitializeComponent();
        _auth = auth;
        _shell = shell;
    }

    protected override Window CreateWindow(IActivationState? activationState)
    {
        // Navigate to the right starting page once the window/shell is live
        _shell.Dispatcher.Dispatch(async () =>
        {
            if (!_auth.IsLoggedIn)
                await Shell.Current.GoToAsync("//login");
            else
                await Shell.Current.GoToAsync("//projects");
        });

        return new Window(_shell);
    }
}

using PunchlistApp.Services;

namespace PunchlistApp;

public partial class App : Application
{
    private readonly IAuthService _auth;
    private readonly IServiceProvider _sp;

    public App(IAuthService auth, IServiceProvider sp)
    {
        // InitializeComponent merges App.xaml resources BEFORE AppShell is created,
        // so StaticResource lookups in AppShell.xaml and all pages succeed.
        InitializeComponent();
        _auth = auth;
        _sp = sp;
    }

    protected override Window CreateWindow(IActivationState? activationState)
    {
        // AppShell is resolved here — after resources are loaded — so XAML
        // StaticResource bindings in the shell and all navigated pages work.
        var shell = _sp.GetRequiredService<AppShell>();

        shell.Dispatcher.Dispatch(async () =>
        {
            if (!_auth.IsLoggedIn)
                await Shell.Current.GoToAsync("//login");
            else
                await Shell.Current.GoToAsync("//projects");
        });

        return new Window(shell);
    }
}

using PunchlistApp.Views;

namespace PunchlistApp;

public partial class AppShell : Shell
{
    public AppShell()
    {
        InitializeComponent();

        // Register routes not in the visual tree
        Routing.RegisterRoute("register", typeof(RegisterPage));
        Routing.RegisterRoute("feed", typeof(PunchItemFeedPage));
        Routing.RegisterRoute("feed/detail", typeof(PunchItemDetailPage));
        Routing.RegisterRoute("feed/createItem", typeof(CreatePunchItemPage));
        Routing.RegisterRoute("feed/createItem/scanner", typeof(BarcodeScannerPage));
        Routing.RegisterRoute("createProject", typeof(CreateProjectPage));
        Routing.RegisterRoute("detail", typeof(PunchItemDetailPage));
        Routing.RegisterRoute("createItem", typeof(CreatePunchItemPage));
        Routing.RegisterRoute("scanner", typeof(BarcodeScannerPage));
    }
}

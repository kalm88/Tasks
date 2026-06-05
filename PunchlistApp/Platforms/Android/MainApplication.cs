using Android.App;
using Android.Runtime;
using Plugin.Firebase.Core.Platforms.Android;

namespace PunchlistApp.Platforms.Android;

[Application]
public class MainApplication : MauiApplication
{
    public MainApplication(IntPtr handle, JniHandleOwnership ownership)
        : base(handle, ownership)
    {
    }

    protected override MauiApp CreateMauiApp() => MauiProgram.CreateMauiApp();

    public override void OnCreate()
    {
        base.OnCreate();
        FirebaseApp.InitializeApp(this);
    }
}

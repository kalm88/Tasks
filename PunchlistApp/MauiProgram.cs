using CommunityToolkit.Maui;
using Microsoft.Extensions.Logging;
using Plugin.Firebase.Auth;
using Plugin.Firebase.CloudFirestore;
using Plugin.Firebase.Storage;
using PunchlistApp.Converters;
using PunchlistApp.Services;
using PunchlistApp.ViewModels;
using PunchlistApp.Views;
using ZXing.Net.Maui.Controls;

namespace PunchlistApp;

public static class MauiProgram
{
    public static MauiApp CreateMauiApp()
    {
        var builder = MauiApp.CreateBuilder();
        builder
            .UseMauiApp<App>()
            .UseMauiCommunityToolkit()
            .UseBarcodeReader()
            .ConfigureFonts(fonts =>
            {
                fonts.AddFont("OpenSans-Regular.ttf", "OpenSansRegular");
                fonts.AddFont("OpenSans-Semibold.ttf", "OpenSansSemibold");
            });

        // ── Firebase ───────────────────────────────────────────────────────
        // Add google-services.json (Android) and GoogleService-Info.plist (iOS)
        // to the Platforms/Android and Platforms/iOS folders before building.
        builder.Services.AddSingleton(_ => CrossFirebaseAuth.Current);
        builder.Services.AddSingleton(_ => CrossFirebaseFirestore.Current);
        builder.Services.AddSingleton(_ => CrossFirebaseStorage.Current);

        // ── App Services ───────────────────────────────────────────────────
        builder.Services.AddSingleton<IFirestoreService, FirestoreService>();
        builder.Services.AddSingleton<IStorageService, FirebaseStorageService>();
        builder.Services.AddSingleton<IAuthService, FirebaseAuthService>();
        builder.Services.AddSingleton<PrintShareService>();

        // ── ViewModels ─────────────────────────────────────────────────────
        builder.Services.AddTransient<LoginViewModel>();
        builder.Services.AddTransient<RegisterViewModel>();
        builder.Services.AddTransient<ProjectListViewModel>();
        builder.Services.AddTransient<CreateProjectViewModel>();
        builder.Services.AddTransient<PunchItemFeedViewModel>();
        builder.Services.AddTransient<CreatePunchItemViewModel>();
        builder.Services.AddTransient<PunchItemDetailViewModel>();
        builder.Services.AddTransient<BarcodeScannerViewModel>();

        // ── Pages ──────────────────────────────────────────────────────────
        builder.Services.AddTransient<LoginPage>();
        builder.Services.AddTransient<RegisterPage>();
        builder.Services.AddTransient<ProjectListPage>();
        builder.Services.AddTransient<CreateProjectPage>();
        builder.Services.AddTransient<PunchItemFeedPage>();
        builder.Services.AddTransient<CreatePunchItemPage>();
        builder.Services.AddTransient<PunchItemDetailPage>();
        builder.Services.AddTransient<BarcodeScannerPage>();

        // Shell
        builder.Services.AddSingleton<AppShell>();

        // ── Converters (app-level resource) ───────────────────────────────
        // Converters are declared in App.xaml resources

#if DEBUG
        builder.Logging.AddDebug();
#endif

        return builder.Build();
    }
}

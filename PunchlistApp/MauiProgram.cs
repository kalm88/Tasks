using CommunityToolkit.Maui;
using Microsoft.Extensions.Logging;
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
            .ConfigureFonts(fonts => { });

        // ── HTTP (shared client for all Firebase REST calls) ───────────────
        builder.Services.AddSingleton<HttpClient>();

        // ── Services ───────────────────────────────────────────────────────
        builder.Services.AddSingleton<IAuthService, AuthRestService>();
        builder.Services.AddSingleton<IFirestoreService, FirestoreRestService>();
        builder.Services.AddSingleton<IStorageService, StorageRestService>();
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

        builder.Services.AddSingleton<AppShell>();

#if DEBUG
        builder.Logging.AddDebug();
#endif

        return builder.Build();
    }
}

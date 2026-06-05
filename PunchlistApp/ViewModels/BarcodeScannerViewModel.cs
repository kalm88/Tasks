using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;

namespace PunchlistApp.ViewModels;

[QueryProperty(nameof(ReturnTo), "returnTo")]
[QueryProperty(nameof(ProjectId), "projectId")]
public partial class BarcodeScannerViewModel : BaseViewModel
{
    [ObservableProperty] private string _returnTo = "";
    [ObservableProperty] private string _projectId = "";
    [ObservableProperty] private bool _isScanning = true;
    [ObservableProperty] private string _scannedValue = "";

    [RelayCommand]
    private async Task BarcodeDetectedAsync(string value)
    {
        if (!IsScanning || string.IsNullOrEmpty(value)) return;
        IsScanning = false;
        ScannedValue = value;

        // Navigate back passing the scanned SKU
        await Shell.Current.GoToAsync($"..?scannedSku={Uri.EscapeDataString(value)}");
    }

    [RelayCommand]
    private async Task BackAsync() => await Shell.Current.GoToAsync("..");
}

using PunchlistApp.ViewModels;
using ZXing.Net.Maui;

namespace PunchlistApp.Views;

public partial class BarcodeScannerPage : ContentPage
{
    private readonly BarcodeScannerViewModel _vm;

    public BarcodeScannerPage(BarcodeScannerViewModel vm)
    {
        InitializeComponent();
        BindingContext = _vm = vm;
        BarcodeReader.Options = new BarcodeReaderOptions
        {
            Formats = BarcodeFormats.All,
            AutoRotate = true,
            Multiple = false
        };
    }

    private void OnBarcodesDetected(object sender, BarcodeDetectionEventArgs e)
    {
        var value = e.Results.FirstOrDefault()?.Value;
        if (!string.IsNullOrEmpty(value))
        {
            MainThread.BeginInvokeOnMainThread(() =>
                _vm.BarcodeDetectedCommand.Execute(value));
        }
    }
}

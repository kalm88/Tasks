using CommunityToolkit.Mvvm.ComponentModel;

namespace PunchlistApp.ViewModels;

public abstract partial class BaseViewModel : ObservableObject
{
    [ObservableProperty]
    [NotifyPropertyChangedFor(nameof(IsNotBusy))]
    private bool _isBusy;

    [ObservableProperty]
    private string _errorMessage = "";

    public bool IsNotBusy => !IsBusy;

    protected async Task RunAsync(Func<Task> action)
    {
        if (IsBusy) return;
        ErrorMessage = "";
        IsBusy = true;
        try
        {
            await action();
        }
        catch (Exception ex)
        {
            ErrorMessage = ex.Message;
        }
        finally
        {
            IsBusy = false;
        }
    }
}

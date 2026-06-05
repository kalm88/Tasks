using PunchlistApp.ViewModels;

namespace PunchlistApp.Views;

public partial class PunchItemDetailPage : ContentPage
{
    public PunchItemDetailPage(PunchItemDetailViewModel vm)
    {
        InitializeComponent();
        BindingContext = vm;
    }
}

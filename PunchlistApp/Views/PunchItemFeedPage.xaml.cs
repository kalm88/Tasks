using PunchlistApp.ViewModels;

namespace PunchlistApp.Views;

public partial class PunchItemFeedPage : ContentPage
{
    public PunchItemFeedPage(PunchItemFeedViewModel vm)
    {
        InitializeComponent();
        BindingContext = vm;
    }
}

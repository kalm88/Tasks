using PunchlistApp.ViewModels;

namespace PunchlistApp.Views;

public partial class CreatePunchItemPage : ContentPage
{
    public CreatePunchItemPage(CreatePunchItemViewModel vm)
    {
        InitializeComponent();
        BindingContext = vm;
    }
}

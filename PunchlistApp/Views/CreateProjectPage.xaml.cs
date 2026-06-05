using PunchlistApp.ViewModels;

namespace PunchlistApp.Views;

public partial class CreateProjectPage : ContentPage
{
    public CreateProjectPage(CreateProjectViewModel vm)
    {
        InitializeComponent();
        BindingContext = vm;
    }
}

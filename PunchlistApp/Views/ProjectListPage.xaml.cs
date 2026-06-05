using PunchlistApp.ViewModels;

namespace PunchlistApp.Views;

public partial class ProjectListPage : ContentPage
{
    private readonly ProjectListViewModel _vm;

    public ProjectListPage(ProjectListViewModel vm)
    {
        InitializeComponent();
        BindingContext = _vm = vm;
    }

    protected override void OnAppearing()
    {
        base.OnAppearing();
        _vm.LoadProjectsCommand.Execute(null);
    }
}

using System.Globalization;
using PunchlistApp.Models;

namespace PunchlistApp.Converters;

public class PriorityToColorConverter : IValueConverter
{
    public object Convert(object? value, Type targetType, object? parameter, CultureInfo culture) =>
        value is Priority p ? p.Color() : Colors.Gray;

    public object ConvertBack(object? value, Type targetType, object? parameter, CultureInfo culture) =>
        throw new NotImplementedException();
}

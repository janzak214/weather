package ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.materialkolor.ktx.toColor
import com.materialkolor.ktx.toHct
import compose.icons.WeatherIcons
import compose.icons.weathericons.Barometer
import compose.icons.weathericons.Cloud
import compose.icons.weathericons.Humidity
import compose.icons.weathericons.Raindrop
import compose.icons.weathericons.Thermometer
import compose.icons.weathericons.WindDeg
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.format.DayOfWeekNames
import model.CurrentWeather
import model.DayWeather
import pl.janzak.weather.model.LocationName
import org.jetbrains.compose.resources.stringResource
import resources.Res
import resources.label_apparent_temperature
import resources.weekday_abbreviation_1
import resources.weekday_abbreviation_2
import resources.weekday_abbreviation_3
import resources.weekday_abbreviation_4
import resources.weekday_abbreviation_5
import resources.weekday_abbreviation_6
import resources.weekday_abbreviation_7
import ui.util.LocalUnits


@Composable
fun WeatherOverviewCard(
    location: LocationName,
    currentWeather: CurrentWeather,
    forecast: List<DayWeather>,
    goToDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.elevatedCardColors(),
        elevation = CardDefaults.elevatedCardElevation(),
        shape = CardDefaults.elevatedShape,
        onClick = goToDetails,
        modifier = modifier.width(380.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
                .background(MaterialTheme.colorScheme.secondaryContainer).padding(12.dp)
        ) {
            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSecondaryContainer) {
                Column(
                    verticalArrangement = Arrangement.SpaceAround,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        location.name,
                        style = MaterialTheme.typography.titleLarge.copy(lineHeight = 1.em)
                    )
                    Text(
                        location.region,
                        style = MaterialTheme.typography.titleSmall.copy(lineHeight = 1.em),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.alpha(0.7f)
                    )
                }

            }
        }
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {

            Row(
                horizontalArrangement = Arrangement.spacedBy(
                    16.dp,
                    alignment = Alignment.CenterHorizontally
                ),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp, start = 16.dp, end = 16.dp)
            ) {

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(WeatherIcons.Thermometer, null)
                    Text(
                        LocalUnits.current.formatTemperature(currentWeather.temperature),
                        style = MaterialTheme.typography.headlineLarge
                    )
                }
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.alpha(0.7f)
                ) {
                    Text(
                        LocalUnits.current.formatTemperature(currentWeather.apparentTemperature),
                        style = MaterialTheme.typography.titleMedium.copy(lineHeight = 1.em),

                        )
                    Text(
                        stringResource(Res.string.label_apparent_temperature),
                        style = MaterialTheme.typography.labelSmall.copy(lineHeight = 1.em),
                    )
                }
                Spacer(modifier.weight(1f))
                WeatherIcon(
                    currentWeather.weatherCode,
                    currentWeather.isDay,
                    modifier = Modifier.padding(4.dp).size(54.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(
                    16.dp,
                    alignment = Alignment.CenterHorizontally
                ),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        WeatherIcons.WindDeg,
                        null,
                        modifier = Modifier.size(24.dp)
                            .rotate(currentWeather.windDirection.toFloat())
                            .padding(horizontal = 2.dp)
                    )
                    Text(
                        LocalUnits.current.formatWindSpeed(currentWeather.windSpeed),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        WeatherIcons.Barometer,
                        null,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        LocalUnits.current.formatPressure(currentWeather.surfacePressure),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        WeatherIcons.Cloud,
                        null,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        LocalUnits.current.formatPercentage(currentWeather.cloudCover),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        WeatherIcons.Humidity,
                        null,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        LocalUnits.current.formatPercentage(currentWeather.relativeHumidity),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)
                    .padding(top = 4.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val red =
                    MaterialTheme.colorScheme.error
                val blue =
                    MaterialTheme.colorScheme.error.toHct().withHue(280.0).toColor()

                val firstModifier =
                    Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh)

                Column(
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Icon(
                        WeatherIcons.Thermometer,
                        null,
                        Modifier.padding(vertical = 6.dp).size(20.dp)
                    )
                    Icon(
                        WeatherIcons.Raindrop,
                        null,
                        Modifier.padding(vertical = 14.dp).size(20.dp)
                    )
                }

                val weekdayNames = DayOfWeekNames(
                    stringResource(Res.string.weekday_abbreviation_1),
                    stringResource(Res.string.weekday_abbreviation_2),
                    stringResource(Res.string.weekday_abbreviation_3),
                    stringResource(Res.string.weekday_abbreviation_4),
                    stringResource(Res.string.weekday_abbreviation_5),
                    stringResource(Res.string.weekday_abbreviation_6),
                    stringResource(Res.string.weekday_abbreviation_7),
                )

                forecast.subList(0, 6).forEachIndexed { index, day ->
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clip(MaterialTheme.shapes.small).then(
                            if (index == 0) {
                                firstModifier
                            } else {
                                Modifier
                            }
                        ).padding(8.dp)
                    ) {
                        Text(
                            "%d".format(day.date.dayOfMonth),
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            day.date.format(LocalDate.Format { dayOfWeek(weekdayNames) }),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.alpha(0.7f)
                        )
                        Spacer(Modifier.height(8.dp))
                        WeatherIcon(
                            code = day.weatherCode, isDay = true,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            LocalUnits.current.formatTemperature(day.temperatureMax),
                            style = MaterialTheme.typography.labelSmall,
                            color = red,
                        )
                        Text(
                            LocalUnits.current.formatTemperature(day.temperatureMin),
                            style = MaterialTheme.typography.labelSmall,
                            color = blue,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            LocalUnits.current.formatPrecipitation(day.precipitation),
                            style = MaterialTheme.typography.labelSmall,
                        )
                        Text(
                            LocalUnits.current.formatPercentage(day.precipitationProbability),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }
        }

    }
}

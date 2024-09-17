@file:OptIn(ExperimentalSharedTransitionApi::class)

package pl.janzak.weather.ui.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import compose.icons.WeatherIcons
import compose.icons.weathericons.Barometer
import compose.icons.weathericons.Cloud
import compose.icons.weathericons.Humidity
import compose.icons.weathericons.Thermometer
import compose.icons.weathericons.WindDeg
import org.jetbrains.compose.resources.stringResource
import pl.janzak.weather.model.CurrentWeather
import pl.janzak.weather.model.LocationName
import resources.Res
import resources.label_apparent_temperature
import ui.util.LocalUnits

context(SharedTransitionScope, AnimatedVisibilityScope)
@Composable
fun CurrentWeather(data: CurrentWeather, locationName: LocationName) {
    Column(Modifier
        .widthIn(max = 380.dp)
        .fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(
                16.dp,
                alignment = Alignment.CenterHorizontally
            ),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(top = 8.dp, bottom = 4.dp, start = 16.dp, end = 16.dp)

        ) {

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(WeatherIcons.Thermometer, null)
                Text(
                    LocalUnits.current.formatTemperature(data.temperature),
                    style = MaterialTheme.typography.headlineLarge
                )
            }
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(0.7f)
            ) {
                Text(
                    LocalUnits.current.formatTemperature(data.apparentTemperature),
                    style = MaterialTheme.typography.titleMedium.copy(lineHeight = 1.em),

                    )
                Text(
                    stringResource(Res.string.label_apparent_temperature),
                    style = MaterialTheme.typography.labelSmall.copy(lineHeight = 1.em),
                )
            }
            Spacer(Modifier.weight(1f))
            WeatherIcon(
                data.weatherCode,
                data.isDay,
                modifier = Modifier.padding(4.dp).size(54.dp),
                tint = MaterialTheme.colorScheme.secondary
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
                        .rotate(data.windDirection.toFloat())
                        .padding(horizontal = 2.dp)
                )
                Text(
                    LocalUnits.current.formatWindSpeed(data.windSpeed),
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
                    LocalUnits.current.formatPressure(data.surfacePressure),
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
                    LocalUnits.current.formatPercentage(data.cloudCover),
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
                    LocalUnits.current.formatPercentage(data.relativeHumidity),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}
@file:OptIn(ExperimentalSharedTransitionApi::class)

package pl.janzak.weather.ui.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.materialkolor.ktx.toColor
import com.materialkolor.ktx.toHct
import compose.icons.WeatherIcons
import compose.icons.weathericons.Raindrop
import compose.icons.weathericons.Thermometer
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.format.DayOfWeekNames
import org.jetbrains.compose.resources.stringArrayResource
import pl.janzak.weather.model.CurrentWeather
import pl.janzak.weather.model.DayWeather
import pl.janzak.weather.model.LocationName
import pl.janzak.weather.ui.util.SharedElementKey
import pl.janzak.weather.ui.util.SharedElementType
import pl.janzak.weather.ui.util.containerTransform
import resources.Res
import resources.weekday_abbreviations
import ui.util.LocalUnits


context(AnimatedVisibilityScope, SharedTransitionScope)
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
        modifier = modifier.width(380.dp)
            .containerTransform(SharedElementKey(location, SharedElementType.Card))
            .skipToLookaheadSize(),
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(12.dp)

        ) {
            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSecondaryContainer) {
                Column(
                    verticalArrangement = Arrangement.SpaceAround,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        location.name,
                        style = MaterialTheme.typography.titleLarge.copy(lineHeight = 1.em),
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

            CurrentWeather(currentWeather, location)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(top = 4.dp, bottom = 12.dp)
                    .animateEnterExit(),
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
                    stringArrayResource(Res.array.weekday_abbreviations)
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
                            code = day.weatherCode,
                            isDay = true,
                            tint = MaterialTheme.colorScheme.secondary
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

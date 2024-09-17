package pl.janzak.weather

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import pl.janzak.weather.model.CurrentWeather
import pl.janzak.weather.model.DayWeather
import pl.janzak.weather.model.LocationName
import pl.janzak.weather.model.WeatherCode
import pl.janzak.weather.model.Coordinates
import pl.janzak.weather.ui.components.WeatherOverviewCard
import pl.janzak.weather.ui.components.WeatherPlot
import kotlin.math.max
import kotlin.random.Random
import kotlin.random.nextInt

val location = LocationName(
    name = "Bronowice Małe",
    region = "Kraków, Polska"
)

val currentWeather = CurrentWeather(
    time = LocalDateTime(2024, 9, 11, 14, 0, 0),
    temperature = 29.42,
    apparentTemperature = 20.24,
    relativeHumidity = 42.0,
    isDay = true,
    surfacePressure = 982.4,
    windSpeed = 6.1,
    windDirection = 17.0,
    cloudCover = 80.0,
    weatherCode = WeatherCode.RAIN_SLIGHT,
    coordinates = Coordinates(0.0, 0.0)
)


val forecast = listOf(
    DayWeather(
        date = LocalDate(2024, 9, 11),
        temperatureMax = 30.5,
        temperatureMin = 16.5,
        precipitation = 20.0,
        precipitationProbability = 90.0,
        weatherCode = WeatherCode.RAIN_SLIGHT
    ),
    DayWeather(
        date = LocalDate(2024, 9, 12),
        temperatureMax = 30.5,
        temperatureMin = 16.5,
        precipitation = 20.0,
        precipitationProbability = 90.0,
        weatherCode = WeatherCode.FOG
    ),
    DayWeather(
        date = LocalDate(2024, 9, 13),
        temperatureMax = 30.5,
        temperatureMin = 16.5,
        precipitation = 20.0,
        precipitationProbability = 90.0,
        weatherCode = WeatherCode.DRIZZLE_MODERATE
    ),
    DayWeather(
        date = LocalDate(2024, 9, 14),
        temperatureMax = 30.5,
        temperatureMin = 16.5,
        precipitation = 20.0,
        precipitationProbability = 90.0,
        weatherCode = WeatherCode.SNOW_FALL_MODERATE
    ),
    DayWeather(
        date = LocalDate(2024, 9, 15),
        temperatureMax = 30.5,
        temperatureMin = 16.5,
        precipitation = 20.0,
        precipitationProbability = 90.0,
        weatherCode = WeatherCode.SNOW_FALL_MODERATE
    ),
    DayWeather(
        date = LocalDate(2024, 9, 16),
        temperatureMax = 30.5,
        temperatureMin = 16.5,
        precipitation = 20.0,
        precipitationProbability = 90.0,
        weatherCode = WeatherCode.SNOW_FALL_MODERATE
    ),
    DayWeather(
        date = LocalDate(2024, 9, 17),
        temperatureMax = 30.5,
        temperatureMin = 16.5,
        precipitation = 20.0,
        precipitationProbability = 90.0,
        weatherCode = WeatherCode.SNOW_FALL_MODERATE
    )
)

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
fun WeatherOverviewCardPreview() {
    SharedTransitionLayout {
        AnimatedVisibility(visible = true) {
            WeatherOverviewCard(location, currentWeather, forecast, goToDetails = {})
        }
    }
}


@Preview
@Composable
fun PlotPreview() {
    val count = 24 * 7
    val random = Random(42)
    val baseTemperature = List(count) { random.nextDouble(-10.0, 20.0) }
        .windowed(10) { it.average() }
    val basePrecipitation = List(count) { random.nextDouble(-10.0, 10.0) }
        .windowed(10) { it.average() }


    val temperatures = List(4) {
        baseTemperature.map {
            it + random.nextDouble(-5.0, 5.0)
        }.windowed(10) { it.average() }.toDoubleArray()
    }
    val visibilities = List(4) { true }
    val weatherCode =
        List(count) {
            random.nextInt(0..<WeatherCode.entries.size)
                .let { WeatherCode.entries[it] }
        }
    val precipitations = List(4) {
        basePrecipitation.map {
            it + random.nextDouble(-3.0, 2.0)
        }.windowed(10) { it.average() }
            .map { max(it, 0.0) }
            .toDoubleArray()
    }

    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .horizontalScroll(
                rememberScrollState()
            )
    ) {
        WeatherPlot(
            temperatures = temperatures,
            visibilities = visibilities,
            weatherCode = weatherCode,
            precipitations = precipitations,
            startDate = LocalDate(2024, 9, 1),
            scrollPosition = 0f,
            colors = listOf(Color.Red, Color.Green, Color.Blue, Color.Magenta),
            aggregate = false,
        )
    }
}

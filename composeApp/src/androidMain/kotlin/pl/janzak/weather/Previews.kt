package pl.janzak.weather

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import model.CurrentWeather
import model.DayWeather
import pl.janzak.weather.model.LocationName
import model.WeatherCode
import pl.janzak.weather.data.api.Coordinates
import ui.components.WeatherOverviewCard

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

@Preview
@Composable
fun WeatherOverviewCardPreview() {
    WeatherOverviewCard(location, currentWeather, forecast, goToDetails = {})
}

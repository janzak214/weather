package model

import kotlinx.datetime.LocalDateTime
import pl.janzak.weather.data.api.Coordinates

data class CurrentWeather(
    val coordinates: Coordinates,
    val time: LocalDateTime,
    val temperature: Double,
    val apparentTemperature: Double,
    val relativeHumidity: Double,
    val isDay: Boolean,
    val surfacePressure: Double,
    val windSpeed: Double,
    val windDirection: Double,
    val cloudCover: Double,
    val weatherCode: WeatherCode,
)

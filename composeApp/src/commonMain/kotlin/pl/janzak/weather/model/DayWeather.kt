package model

import kotlinx.datetime.LocalDate

data class DayWeather(
    val date: LocalDate,
    val temperatureMin: Double,
    val temperatureMax: Double,
    val precipitation: Double,
    val precipitationProbability: Double,
    val weatherCode: WeatherCode,
)
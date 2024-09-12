package pl.janzak.weather.ui.util

import pl.janzak.weather.model.CurrentWeather
import pl.janzak.weather.model.DayWeather
import pl.janzak.weather.model.WeatherCode
import pl.janzak.weather.model.Coordinates
import pl.janzak.weather.data.api.CurrentWeatherResponse
import pl.janzak.weather.data.api.DailyForecastResponse

fun currentWeatherResponseToModel(
    response: CurrentWeatherResponse,
    coordinates: Coordinates
): CurrentWeather = response.run {
    CurrentWeather(
        coordinates = coordinates,
        time = current.time,
        temperature = current.temperature2m,
        apparentTemperature = current.apparentTemperature,
        relativeHumidity = current.relativeHumidity2m,
        isDay = current.isDay == 1,
        surfacePressure = current.surfacePressure,
        windSpeed = current.windSpeed10m,
        windDirection = current.windDirection10m,
        cloudCover = current.cloudCover,
        weatherCode = WeatherCode.get(current.weatherCode)!!,
    )
}

fun dailyForecastResponseToModel(response: DailyForecastResponse): List<DayWeather> = response.run {
    daily.time.mapIndexed { i, date ->
        DayWeather(
            date = date,
            temperatureMin = daily.temperature2mMin[i],
            temperatureMax = daily.temperature2mMax[i],
            precipitation = daily.precipitationSum[i],
            precipitationProbability = daily.precipitationProbabilityMax[i],
            weatherCode = WeatherCode.get(daily.weatherCode[i])!!,
        )
    }
}

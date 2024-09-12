package pl.janzak.weather.model

import model.DayWeather

data class FavoriteLocation(
    val info: LocationInfo,
    val currentWeather: model.CurrentWeather,
    val forecast: List<DayWeather>,
)
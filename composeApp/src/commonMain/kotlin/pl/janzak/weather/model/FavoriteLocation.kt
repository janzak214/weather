package pl.janzak.weather.model

data class FavoriteLocation(
    val info: LocationInfo,
    val currentWeather: CurrentWeather,
    val forecast: List<DayWeather>,
)
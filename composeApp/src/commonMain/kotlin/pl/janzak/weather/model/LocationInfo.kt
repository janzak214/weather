package pl.janzak.weather.model

import kotlinx.serialization.Serializable

@Serializable
data class LocationInfo(
    val coordinates: Coordinates,
    val name: LocationName,
)
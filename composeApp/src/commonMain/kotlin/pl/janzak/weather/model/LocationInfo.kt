package pl.janzak.weather.model

import kotlinx.serialization.Serializable
import pl.janzak.weather.data.api.Coordinates

@Serializable
data class LocationInfo(
    val coordinates: Coordinates,
    val name: LocationName,
)
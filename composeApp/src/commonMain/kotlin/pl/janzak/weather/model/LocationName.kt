package pl.janzak.weather.model

import kotlinx.serialization.Serializable

@Serializable
data class LocationName(val name: String, val region: String)

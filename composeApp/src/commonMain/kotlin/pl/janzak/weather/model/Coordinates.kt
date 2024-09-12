package pl.janzak.weather.model

import kotlinx.serialization.Serializable

@Serializable
data class Coordinates(
    val latitude: Double,
    val longitude: Double,
) {
    override fun toString() = "%.2f;%.2f".format(latitude, longitude).replace(',', '.')

    companion object {
        fun parse(value: String): Coordinates {
            val split = value.split(";")
            return Coordinates(split[0].toDouble(), split[1].toDouble())
        }
    }
}
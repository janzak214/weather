package pl.janzak.weather.data.api

import com.skydoves.sandwich.ApiResponse
import com.skydoves.sandwich.ktor.getApiResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import kotlinx.serialization.SerialName
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

@Serializable
data class ReverseGeocodingResponse(
    val name: String,
    @SerialName("display_name") val displayName: String,
)


class NominatimApi(private val baseUrl: String, private val client: HttpClient) {
    suspend fun reverseGeocode(
        coordinates: Coordinates,
        language: String = "en-US"
    ): ApiResponse<ReverseGeocodingResponse> =
        client.getApiResponse(baseUrl) {
            url {
                pathSegments = listOf("reverse")
            }
            parameter("lat", coordinates.latitude)
            parameter("lon", coordinates.longitude)
            parameter("format", "jsonv2")
            parameter("zoom", 12)
            parameter("layer", "address")
            parameter("accept-language", language)
        }
}

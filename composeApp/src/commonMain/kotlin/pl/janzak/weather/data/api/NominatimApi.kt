package pl.janzak.weather.data.api

import com.skydoves.sandwich.ApiResponse
import com.skydoves.sandwich.ktor.getApiResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import kotlinx.serialization.Serializable


@Serializable
data class Coordinates(
    val latitude: Float,
    val longitude: Float
)

@Serializable
data class ReverseGeocodingResponse(val name: String)


class NominatimApi(private val baseUrl: String, private val client: HttpClient) {
    suspend fun reverseGeocode(coordinates: Coordinates, language: String = "en-US"): ApiResponse<ReverseGeocodingResponse> =
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

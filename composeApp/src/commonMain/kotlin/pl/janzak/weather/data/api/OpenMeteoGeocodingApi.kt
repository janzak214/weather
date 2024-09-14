package pl.janzak.weather.data.api

import com.skydoves.sandwich.ApiResponse
import com.skydoves.sandwich.ktor.getApiResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import kotlinx.serialization.Serializable


@Serializable
data class GeocodingResponse(val results: List<GeocodingEntry>? = null)

@Serializable
data class GeocodingEntry(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String = "",
)


class OpenMeteoGeocodingApi(private val baseUrl: String, private val client: HttpClient) {
    suspend fun geocode(
        query: String,
        count: Int = 10,
        language: String = "en"
    ): ApiResponse<GeocodingResponse> =
        client.getApiResponse(baseUrl) {
            url {
                pathSegments = listOf("v1", "search")
            }
            parameter("name", query)
            parameter("count", count)
            parameter("language", language.split("-").first())
            parameter("format", "json")
        }
}

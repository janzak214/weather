package pl.janzak.weather.data.api

import com.skydoves.sandwich.ApiResponse
import com.skydoves.sandwich.ktor.getApiResponse
import com.skydoves.sandwich.mapSuccess
import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pl.janzak.weather.model.Coordinates

@Serializable
data class CurrentWeatherResponse(
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    val current: CurrentWeather,
)

@Serializable
data class CurrentWeather(
    val time: LocalDateTime,
    val interval: Int,
    @SerialName("temperature_2m") val temperature2m: Double,
    @SerialName("relative_humidity_2m") val relativeHumidity2m: Double,
    @SerialName("apparent_temperature") val apparentTemperature: Double,
    @SerialName("is_day") val isDay: Int,
    @SerialName("weather_code") val weatherCode: Int,
    @SerialName("cloud_cover") val cloudCover: Double,
    @SerialName("surface_pressure") val surfacePressure: Double,
    @SerialName("wind_speed_10m") val windSpeed10m: Double,
    @SerialName("wind_direction_10m") val windDirection10m: Double,
)

@Serializable
data class DailyForecastResponse(
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    val daily: DailyForecast,
)

@Serializable
data class DailyForecast(
    val time: List<LocalDate>,
    @SerialName("weather_code") val weatherCode: IntArray,
    @SerialName("temperature_2m_max") val temperature2mMax: DoubleArray,
    @SerialName("temperature_2m_min") val temperature2mMin: DoubleArray,
    val sunrise: List<LocalDateTime>,
    val sunset: List<LocalDateTime>,
    @SerialName("precipitation_sum") val precipitationSum: DoubleArray,
    @SerialName("precipitation_probability_max") val precipitationProbabilityMax: DoubleArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DailyForecast

        if (time != other.time) return false
        if (!weatherCode.contentEquals(other.weatherCode)) return false
        if (!temperature2mMax.contentEquals(other.temperature2mMax)) return false
        if (!temperature2mMin.contentEquals(other.temperature2mMin)) return false
        if (sunrise != other.sunrise) return false
        if (sunset != other.sunset) return false
        if (!precipitationSum.contentEquals(other.precipitationSum)) return false
        if (!precipitationProbabilityMax.contentEquals(other.precipitationProbabilityMax)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = time.hashCode()
        result = 31 * result + weatherCode.contentHashCode()
        result = 31 * result + temperature2mMax.contentHashCode()
        result = 31 * result + temperature2mMin.contentHashCode()
        result = 31 * result + sunrise.hashCode()
        result = 31 * result + sunset.hashCode()
        result = 31 * result + precipitationSum.contentHashCode()
        result = 31 * result + precipitationProbabilityMax.contentHashCode()
        return result
    }
}


@Serializable
data class HourlyForecastResponse(
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    val hourly: HourlyForecast,
)

@Serializable
data class HourlyForecast(
    val time: List<LocalDateTime>,
    @SerialName("temperature_2m") val temperature2m: DoubleArray,
    @SerialName("relative_humidity_2m") val relativeHumidity2m: DoubleArray,
    @SerialName("precipitation_probability") val precipitationProbability: DoubleArray,
    @SerialName("precipitation") val precipitation: DoubleArray,
    @SerialName("weather_code") val weatherCode: DoubleArray,
    @SerialName("surface_pressure") val surfacePressure: DoubleArray,
    @SerialName("cloud_cover") val cloudCover: DoubleArray,
    @SerialName("wind_speed_10m") val windSpeed10m: DoubleArray,
    @SerialName("wind_direction_10m") val windDirection10m: DoubleArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HourlyForecast

        if (time != other.time) return false
        if (!temperature2m.contentEquals(other.temperature2m)) return false
        if (!relativeHumidity2m.contentEquals(other.relativeHumidity2m)) return false
        if (!precipitationProbability.contentEquals(other.precipitationProbability)) return false
        if (!precipitation.contentEquals(other.precipitation)) return false
        if (!weatherCode.contentEquals(other.weatherCode)) return false
        if (!surfacePressure.contentEquals(other.surfacePressure)) return false
        if (!cloudCover.contentEquals(other.cloudCover)) return false
        if (!windSpeed10m.contentEquals(other.windSpeed10m)) return false
        if (!windDirection10m.contentEquals(other.windDirection10m)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = time.hashCode()
        result = 31 * result + temperature2m.contentHashCode()
        result = 31 * result + relativeHumidity2m.contentHashCode()
        result = 31 * result + precipitationProbability.contentHashCode()
        result = 31 * result + precipitation.contentHashCode()
        result = 31 * result + weatherCode.contentHashCode()
        result = 31 * result + surfacePressure.contentHashCode()
        result = 31 * result + cloudCover.contentHashCode()
        result = 31 * result + windSpeed10m.contentHashCode()
        result = 31 * result + windDirection10m.contentHashCode()
        return result
    }
}

class OpenMeteoWeatherApi(private val baseUrl: String, private val client: HttpClient) {
    suspend fun currentWeather(
        coordinates: Coordinates
    ): ApiResponse<CurrentWeatherResponse> =
        client.getApiResponse<CurrentWeatherResponse>(baseUrl) {
            url {
                pathSegments = listOf("v1", "forecast")
            }
            val current = listOf(
                "temperature_2m",
                "relative_humidity_2m",
                "apparent_temperature",
                "is_day",
                "weather_code",
                "cloud_cover",
                "surface_pressure",
                "wind_speed_10m",
                "wind_direction_10m",
            )
            parameter("latitude", coordinates.latitude)
            parameter("longitude", coordinates.longitude)
            parameter("current", current.joinToString(","))
            parameter("timezone", "auto")
        }.mapSuccess { copy(latitude = coordinates.latitude, longitude = coordinates.longitude) }

    suspend fun dailyForecast(coordinates: Coordinates): ApiResponse<DailyForecastResponse> =
        client.getApiResponse(baseUrl)
        {
            url {
                pathSegments = listOf("v1", "forecast")
            }
            val daily = listOf(
                "weather_code",
                "temperature_2m_max",
                "temperature_2m_min",
                "sunrise",
                "sunset",
                "precipitation_sum",
                "precipitation_probability_max",
            )
            parameter("latitude", coordinates.latitude)
            parameter("longitude", coordinates.longitude)
            parameter("daily", daily.joinToString(","))
            parameter("timezone", "auto")
        }


    suspend fun hourlyForecast(
        coordinates: Coordinates,
        model: String = "best_match"
    ): ApiResponse<HourlyForecastResponse> =
        client.getApiResponse(baseUrl)
        {
            url {
                pathSegments = listOf("v1", "forecast")
            }
            val hourly = listOf(
                "temperature_2m",
                "relative_humidity_2m",
                "precipitation_probability",
                "precipitation",
                "weather_code",
                "surface_pressure",
                "cloud_cover",
                "wind_speed_10m",
                "wind_direction_10m",
            )
            parameter("latitude", coordinates.latitude)
            parameter("longitude", coordinates.longitude)
            parameter("hourly", hourly.joinToString(","))
            parameter("timezone", "auto")
            parameter("models", model)
        }
}

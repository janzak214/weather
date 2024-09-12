package pl.janzak.weather.data.api

import com.skydoves.sandwich.ApiResponse
import com.skydoves.sandwich.ktor.getApiResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CurrentWeatherResponse(
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
    val timezone: String,
    val daily: DailyForecast,
)

@Serializable
data class DailyForecast(
    val time: List<LocalDate>,
    @SerialName("weather_code") val weatherCode: List<Int>,
    @SerialName("temperature_2m_max") val temperature2mMax: List<Double>,
    @SerialName("temperature_2m_min") val temperature2mMin: List<Double>,
    val sunrise: List<LocalDateTime>,
    val sunset: List<LocalDateTime>,
    @SerialName("precipitation_sum") val precipitationSum: List<Double>,
    @SerialName("precipitation_probability_max") val precipitationProbabilityMax: List<Double>,
)


@Serializable
data class HourlyForecastResponse(
    val timezone: String,
    val hourly: HourlyForecast,
)

@Serializable
data class HourlyForecast(
    val time: List<LocalDateTime>,
    @SerialName("temperature_2m") val temperature2m: List<Double>,
    @SerialName("relative_humidity_2m") val relativeHumidity2m: List<Double>,
    @SerialName("precipitation_probability") val precipitationProbability: List<Double>,
    @SerialName("precipitation") val precipitation: List<Double>,
    @SerialName("weather_code") val weatherCode: List<Double>,
    @SerialName("surface_pressure") val surfacePressure: List<Double>,
    @SerialName("cloud_cover") val cloudCover: List<Double>,
    @SerialName("wind_speed_10m") val windSpeed10m: List<Double>,
    @SerialName("wind_direction_10m") val windDirection10m: List<Double>,
)

class OpenMeteoWeatherApi(private val baseUrl: String, private val client: HttpClient) {
    suspend fun currentWeather(
        coordinates: Coordinates
    ): ApiResponse<CurrentWeatherResponse> =
        client.getApiResponse(baseUrl) {
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
        }

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

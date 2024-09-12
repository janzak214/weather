package pl.janzak.weather.data.api

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module

private const val NOMINATIM_URL = "https://nominatim.openstreetmap.org"
private const val OPENMETEO_GEOCODING_URL = "https://geocoding-api.open-meteo.com"
private const val OPENMETEO_WEATHER_URL = "https://api.open-meteo.com"

val apiModule = module {
    single<HttpClient> {
        HttpClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }
    single<NominatimApi> {
        NominatimApi(NOMINATIM_URL, get())
    }
    single<OpenMeteoGeocodingApi> {
        OpenMeteoGeocodingApi(OPENMETEO_GEOCODING_URL, get())
    }
    single<OpenMeteoWeatherApi> {
        OpenMeteoWeatherApi(OPENMETEO_WEATHER_URL, get())
    }
}

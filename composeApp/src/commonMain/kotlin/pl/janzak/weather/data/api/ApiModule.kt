package pl.janzak.weather.data.api

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.cache.storage.CacheStorage
import io.ktor.client.plugins.cache.storage.CachedResponseData
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.Headers
import io.ktor.http.HttpProtocolVersion
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.date.GMTDate
import io.ktor.util.date.plus
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.getString
import org.koin.dsl.module
import pl.janzak.weather.database.CacheEntry
import pl.janzak.weather.database.Database
import resources.Res
import resources.nominatim_url
import resources.openmeteo_geocoding_url
import resources.openmeteo_weather_url
import kotlin.time.Duration.Companion.hours

private fun cacheEntryMapper(
    url: Url,
    statusCode: Int,
    statusDescription: String,
    requestTime: GMTDate,
    responseTime: GMTDate,
    expires: GMTDate,
    version: HttpProtocolVersion,
    varyKeys: Map<String, String>,
    headers: Headers,
    body: ByteArray,
) =
    CachedResponseData(
        url = url,
        statusCode = HttpStatusCode(statusCode, statusDescription),
        requestTime = requestTime,
        responseTime = responseTime,
        version = version,
        expires = expires,
        headers = headers,
        varyKeys = varyKeys,
        body = body,
    )

private val logger = KotlinLogging.logger {}

class SqliteStorage(val database: Database) : CacheStorage {
    override suspend fun find(url: Url, varyKeys: Map<String, String>): CachedResponseData? {
        val result = database.cacheQueries
            .readCache(url, varyKeys, ::cacheEntryMapper)
            .executeAsOneOrNull()

        logger.atInfo {
            message = "finding cache entry"
            payload = mapOf("url" to url, "varyKeys" to varyKeys, "entry" to result)
        }

        return result
    }

    override suspend fun findAll(url: Url): Set<CachedResponseData> {
        val result = database.cacheQueries
            .readCacheAll(url, ::cacheEntryMapper)
            .executeAsList()
            .toSet()

        logger.atInfo {
            message = "finding cache entries"
            payload = mapOf("url" to url, "entries" to result)
        }

        return result
    }

    override suspend fun store(url: Url, data: CachedResponseData) {
        logger.atInfo {
            message = "storing cache entry"
            payload = mapOf("url" to url)
        }
        database.cacheQueries.clearExpired()
        database.cacheQueries.writeCache(
            CacheEntry(
                url = url,
                statusCode = data.statusCode.value,
                statusDescription = data.statusCode.description,
                version = data.version,
                requestTime = data.requestTime,
                responseTime = data.responseTime,
                expires = data.expires + 1.hours,
                varyKeys = data.varyKeys,
                headers = data.headers,
                body = data.body,
            )
        )
    }

}


val apiModule = module {
    single<HttpClient> {
        HttpClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            install(HttpCache) {
                publicStorage(SqliteStorage(get()))
            }
        }
    }
    single<NominatimApi> {
        NominatimApi(runBlocking { getString(Res.string.nominatim_url) }, get())
    }
    single<OpenMeteoGeocodingApi> {
        OpenMeteoGeocodingApi(runBlocking { getString(Res.string.openmeteo_geocoding_url) }, get())
    }
    single<OpenMeteoWeatherApi> {
        OpenMeteoWeatherApi(runBlocking { getString(Res.string.openmeteo_weather_url) }, get())
    }
}

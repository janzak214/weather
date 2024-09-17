package pl.janzak.weather.data

import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.db.SqlDriver
import io.ktor.http.Headers
import io.ktor.http.HttpProtocolVersion
import io.ktor.http.Url
import io.ktor.util.date.GMTDate
import io.ktor.util.toMap
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import pl.janzak.weather.database.CacheEntry
import pl.janzak.weather.database.Database

expect class DriverFactory() {
    fun createDriver(): SqlDriver
}

private val urlAdapter = object : ColumnAdapter<Url, String> {
    override fun decode(databaseValue: String): Url = Url(databaseValue)
    override fun encode(value: Url): String = value.toString()
}

private val intAdapter = object : ColumnAdapter<Int, Long> {
    override fun decode(databaseValue: Long): Int = databaseValue.toInt()
    override fun encode(value: Int): Long = value.toLong()
}

private val gmtDateAdapter = object : ColumnAdapter<GMTDate, Long> {
    override fun decode(databaseValue: Long): GMTDate = GMTDate(databaseValue)
    override fun encode(value: GMTDate): Long = value.timestamp
}

private val mapAdapter = object : ColumnAdapter<Map<String, String>, String> {
    override fun decode(databaseValue: String): Map<String, String> =
        Json.decodeFromString(databaseValue)

    override fun encode(value: Map<String, String>): String = Json.encodeToString(value)
}

private val versionAdapter = object : ColumnAdapter<HttpProtocolVersion, String> {
    override fun decode(databaseValue: String): HttpProtocolVersion =
        HttpProtocolVersion.parse(databaseValue)

    override fun encode(value: HttpProtocolVersion): String = value.toString()
}

private val headersAdapter = object : ColumnAdapter<Headers, String> {
    override fun decode(databaseValue: String): Headers =
        Json.decodeFromString<Map<String, List<String>>>(databaseValue)
            .let {
                Headers.build {
                    it.forEach {
                        appendAll(it.key, it.value)
                    }
                }
            }

    override fun encode(value: Headers): String = value.toMap().let(Json::encodeToString)
}

private val cacheEntryAdapter = CacheEntry.Adapter(
    urlAdapter = urlAdapter,
    statusCodeAdapter = intAdapter,
    requestTimeAdapter = gmtDateAdapter,
    responseTimeAdapter = gmtDateAdapter,
    expiresAdapter = gmtDateAdapter,
    versionAdapter = versionAdapter,
    varyKeysAdapter = mapAdapter,
    headersAdapter = headersAdapter,
)

val databaseModule = module {
    factory<SqlDriver> { DriverFactory().createDriver() }
    single { Database(get(), cacheEntryAdapter) }
}

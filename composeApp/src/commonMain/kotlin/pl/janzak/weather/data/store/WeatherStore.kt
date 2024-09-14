package pl.janzak.weather.data.store

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.skydoves.sandwich.getOrThrow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDateTime
import pl.janzak.weather.model.WeatherCode
import org.mobilenativefoundation.store.store5.Converter
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.StoreBuilder
import pl.janzak.weather.database.Database
import pl.janzak.weather.model.Coordinates
import pl.janzak.weather.data.api.CurrentWeatherResponse
import pl.janzak.weather.data.api.OpenMeteoWeatherApi
import pl.janzak.weather.model.CurrentWeather
import pl.janzak.weather.database.CurrentWeather as DbCurrentWeather

class WeatherStoreProvider(
    private val openMeteoWeatherApi: OpenMeteoWeatherApi,
    private val database: Database,
) {
    fun current(): Store<Coordinates, CurrentWeather> {
        val fetcher: Fetcher<Coordinates, CurrentWeatherResponse> = Fetcher.of {
            println("XXX fetching")
            openMeteoWeatherApi.currentWeather(
                it
            ).getOrThrow()
        }

        val converter: Converter.Builder<CurrentWeatherResponse, DbCurrentWeather, CurrentWeather> =
            Converter.Builder<CurrentWeatherResponse, DbCurrentWeather, CurrentWeather>()
                .fromOutputToLocal { output ->
                    DbCurrentWeather(
                        coordinates = output.coordinates.toString(),
                        time = output.time.toString(),
                        temperature = output.temperature,
                        apparentTemperature = output.apparentTemperature,
                        relativeHumidity = output.relativeHumidity,
                        isDay = if (output.isDay) {
                            1
                        } else {
                            0
                        },
                        surfacePressure = output.surfacePressure,
                        windSpeed = output.windSpeed,
                        windDirection = output.windDirection,
                        cloudCover = output.cloudCover,
                        weatherCode = output.weatherCode.code.toLong(),
                    )
                }.fromNetworkToLocal { network ->
                    DbCurrentWeather(
                        coordinates = Coordinates(network.latitude, network.longitude).toString(),
                        time = network.current.time.toString(),
                        temperature = network.current.temperature2m,
                        apparentTemperature = network.current.apparentTemperature,
                        relativeHumidity = network.current.relativeHumidity2m,
                        isDay = network.current.isDay.toLong(),
                        surfacePressure = network.current.surfacePressure,
                        windSpeed = network.current.windSpeed10m,
                        windDirection = network.current.windDirection10m,
                        cloudCover = network.current.cloudCover,
                        weatherCode = network.current.weatherCode.toLong(),
                    )
                }
        val sourceOfTruth2: SourceOfTruth<Coordinates, CurrentWeatherResponse, CurrentWeather> =
            SourceOfTruth.of(
                reader = {
                    println("XXX reading $it")
                    database.currentQueries.loadCurrent(it.toString()).asFlow()
                        .mapToOneOrNull(Dispatchers.IO).map { local ->
                            println("XXX reading $it: $local")

                            if (local == null) {
                                null
                            } else {
                                CurrentWeather(
                                    coordinates = Coordinates.parse(local.coordinates),
                                    time = LocalDateTime.parse(local.time),
                                    temperature = local.temperature,
                                    apparentTemperature = local.apparentTemperature,
                                    relativeHumidity = local.relativeHumidity,
                                    isDay = local.isDay == 1L,
                                    surfacePressure = local.surfacePressure,
                                    windSpeed = local.windSpeed,
                                    windDirection = local.windDirection,
                                    cloudCover = local.cloudCover,
                                    weatherCode = WeatherCode.get(local.weatherCode.toInt())!!,
                                )
                            }
                        }
                },
                writer = { id, sot ->
                    println("xxx writing $id"); database.currentQueries.insertCurrent(
                    converter.fromNetworkToLocal(sot)
                )
                },
                delete = { database.currentQueries.deleteCurrent(it.toString()) },
                deleteAll = { database.currentQueries.deleteAllCurrent() },
            )

        return StoreBuilder.from(
            fetcher = fetcher,
            sourceOfTruth = sourceOfTruth2,
        )

            .build()

    }
}


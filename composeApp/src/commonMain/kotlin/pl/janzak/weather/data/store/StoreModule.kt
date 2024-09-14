package pl.janzak.weather.data.store

import org.koin.dsl.module
import pl.janzak.weather.database.Database

val storeModule = module {
    single { WeatherStoreProvider(get(), get()) }
    single { Database(get()) }
}

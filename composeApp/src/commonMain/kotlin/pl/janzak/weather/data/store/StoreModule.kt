package pl.janzak.weather.data.store

import org.koin.dsl.module

val storeModule = module {
    single { WeatherStoreProvider(get(), get()) }
}

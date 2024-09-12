package pl.janzak.weather.ui.util

import dev.jordond.compass.geolocation.Locator
import org.koin.dsl.module

expect fun getLocator(): Locator

val geolocalizationModule = module {
    factory<Locator> { getLocator() }
}

package pl.janzak.weather

import androidx.compose.runtime.Composable
import pl.janzak.weather.navigation.AppNavHost
import org.koin.dsl.module
import pl.janzak.weather.ui.theme.AppTheme
import pl.janzak.weather.data.api.apiModule
import pl.janzak.weather.data.databaseModule
import pl.janzak.weather.data.store.storeModule
import pl.janzak.weather.ui.util.geolocalizationModule

val appModule = module {
    includes(apiModule, storeModule, geolocalizationModule, databaseModule)
}

@Composable
fun App() {
    AppTheme {
        AppNavHost()
    }
}

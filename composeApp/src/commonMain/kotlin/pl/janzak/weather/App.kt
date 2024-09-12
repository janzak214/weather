import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import app.cash.sqldelight.db.SqlDriver
import kotlinx.coroutines.CoroutineScope
import pl.janzak.weather.model.CounterRepository
import navigation.AppNavHost
import org.koin.compose.KoinApplication
import org.koin.dsl.module
import ui.theme.AppTheme
import data.DriverFactory
import pl.janzak.weather.model.DbCounterRepository
import org.koin.core.module.Module
import pl.janzak.weather.data.api.apiModule

val appModule = module {
    single<CounterRepository> { DbCounterRepository(get(), get()) }
    factory<SqlDriver> { DriverFactory().createDriver() }
}

expect fun Module.contextModule(context: Context)

@Composable
expect fun getContext(): Context

expect abstract class Context

@Composable
fun App() {
    val coroutineScope = rememberCoroutineScope()
    val context = getContext()

    KoinApplication(application = {
        modules(
            appModule,
            apiModule,
            module {
                single<CoroutineScope> { coroutineScope }
                contextModule(context)
            })
    }) {
        AppTheme {
            AppNavHost()
        }
    }
}

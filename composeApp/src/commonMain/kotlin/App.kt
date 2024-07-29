import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import model.CounterRepository
import model.CounterRepositoryImpl
import navigation.AppNavHost
import org.koin.compose.KoinApplication
import org.koin.dsl.module
import ui.theme.AppTheme

val appModule = module {
    single<CounterRepository> { CounterRepositoryImpl() }
}

@Composable
fun App() {
    val coroutineScope = rememberCoroutineScope()

    KoinApplication(application = {
        modules(appModule, module {
            single<CoroutineScope> { coroutineScope }
        })
    }) {
        AppTheme {
            AppNavHost()
        }
    }
}

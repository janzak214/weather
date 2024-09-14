import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import org.koin.core.module.Module

actual fun Module.contextModule(context: Context) {

    single<android.content.Context> { context }
}

actual typealias Context = android.content.Context

@Composable
actual fun getContext(): Context {
    return LocalContext.current
}

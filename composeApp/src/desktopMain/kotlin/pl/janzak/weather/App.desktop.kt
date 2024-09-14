import androidx.compose.runtime.Composable
import org.koin.core.module.Module

actual fun Module.contextModule(context: Context) {
}

actual abstract class Context

@Composable
actual fun getContext(): Context {
    return object : Context() {}
}

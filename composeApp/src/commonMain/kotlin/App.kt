import androidx.compose.runtime.Composable
import navigation.AppNavHost
import ui.theme.AppTheme


@Composable
fun App() {
    AppTheme {
        AppNavHost()
    }
}

import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.core.context.startKoin
import resources.Res
import resources.app_name
import resources.icon_rounded
import pl.janzak.weather.appModule
import pl.janzak.weather.App

fun main() = application {
    startKoin { modules(appModule) }

    val painter = rememberVectorPainter(vectorResource(Res.drawable.icon_rounded))
    Window(
        onCloseRequest = ::exitApplication,
        title = stringResource(Res.string.app_name),
        icon = painter,
    ) {
        App()
    }
}

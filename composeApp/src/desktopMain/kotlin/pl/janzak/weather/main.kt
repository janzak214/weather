import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import resources.Res
import resources.app_name
import resources.icon_rounded

fun main() = application {
    val painter = rememberVectorPainter(vectorResource(Res.drawable.icon_rounded))
    Window(
        onCloseRequest = ::exitApplication,
        title = stringResource(Res.string.app_name),
        icon = painter,
    ) {
        App()
    }
}

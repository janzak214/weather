import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.runDesktopComposeUiTest

@OptIn(ExperimentalTestApi::class)
actual abstract class UiTest {
    actual fun runTest(block: ComposeUiTest.() -> Unit) {
        runComposeUiTest(block = block)
    }

    actual fun runAppTest(block: ComposeUiTest.() -> Unit) = Unit
}
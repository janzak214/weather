import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
actual abstract class UiTest {
    actual fun runTest(block: ComposeUiTest.() -> Unit) {
        runComposeUiTest(block = block)
    }

    actual fun runAppTest(block: ComposeUiTest.() -> Unit) {
        runComposeUiTest(block = block)
    }
}
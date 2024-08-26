import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi

@OptIn(ExperimentalTestApi::class)
expect abstract class UiTest() {
    fun runTest(block: ComposeUiTest.() -> Unit)
    fun runAppTest(block: ComposeUiTest.() -> Unit)
}
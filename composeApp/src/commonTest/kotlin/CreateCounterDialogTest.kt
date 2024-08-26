import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString
import resources.Res
import resources.counter_dialog_create_cancel
import resources.counter_dialog_create_confirm
import ui.screens.CreateCounterDialog
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(ExperimentalTestApi::class)
class CreateCounterDialogTest : UiTest() {
    private fun confirmButton() =
        SemanticsMatcher.expectValue(
            SemanticsProperties.Role,
            Role.Button
        ) and hasText(runBlocking { getString(Res.string.counter_dialog_create_confirm) })

    private fun cancelButton() =
        SemanticsMatcher.expectValue(
            SemanticsProperties.Role,
            Role.Button
        ) and hasText(runBlocking { getString(Res.string.counter_dialog_create_cancel) })


    private val textField = SemanticsMatcher.keyIsDefined(SemanticsProperties.EditableText)


    @Test
    fun canReadStringResources() = runTest {
        val string = runBlocking { getString(Res.string.counter_dialog_create_confirm) }
        assertNotNull(string)
    }

    @Test
    fun confirmButtonIsActiveOnlyIfTheFieldIsNotEmpty() = runTest {
            setContent { CreateCounterDialog({}, {}) }
            onNode(
                confirmButton()
            ).assertIsNotEnabled()

            val name = "counter name"

            onNode(textField).performTextInput(name)
            waitForIdle()

            onNode(
                confirmButton()
            ).assertIsEnabled()
        }

    @Test
    fun onConfirmShouldBeCalledIfConfirmButtonClicked() = runTest {
            var result: String? = null

            setContent { CreateCounterDialog(onConfirm = { result = it }, onDismiss = {}) }

            val name = "counter name"

            onNode(textField).performTextInput(name)
            waitForIdle()

            onNode(confirmButton()).performClick()
            waitForIdle()


            assertEquals(result, name)
        }

    @Test
    fun onDismissShouldBeCalledIfCancelButtonIsClicked() = runTest {
            var called = false

            setContent { CreateCounterDialog(onConfirm = { }, onDismiss = { called = true }) }

            onNode(cancelButton()).performClick()
            waitForIdle()

            assert(called)
        }
}

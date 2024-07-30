import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isEditable
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString
import resources.Res
import resources.counter_dialog_create_cancel
import resources.counter_dialog_create_confirm
import ui.screens.CreateCounterDialog
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class CreateCounterDialogTest {
    private val confirmButton =
        SemanticsMatcher.expectValue(
            SemanticsProperties.Role,
            Role.Button
        ) and hasText(runBlocking { getString(Res.string.counter_dialog_create_confirm) })

    private val cancelButton =
        SemanticsMatcher.expectValue(
            SemanticsProperties.Role,
            Role.Button
        ) and hasText(runBlocking { getString(Res.string.counter_dialog_create_cancel) })


    private val textField = isEditable()


    // @Test
    // fun `confirm button is active only if the text field isn't empty`() =
    //     runComposeUiTest {
    //         setContent { CreateCounterDialog({}, {}) }

    //         onNode(
    //             confirmButton
    //         ).assertIsNotEnabled()

    //         val name = "counter name"

    //         onNode(textField).performTextInput(name)
    //         waitForIdle()

    //         onNode(
    //             confirmButton
    //         ).assertIsEnabled()
    //     }

    // @Test
    // fun `onConfirm should be called if confirm button is clicked`() =
    //     runComposeUiTest {
    //         var result: String? = null

    //         setContent { CreateCounterDialog(onConfirm = { result = it }, onDismiss = {}) }

    //         val name = "counter name"

    //         onNode(textField).performTextInput(name)
    //         waitForIdle()

    //         onNode(confirmButton).performClick()
    //         waitForIdle()

    //         assertEquals(result, name)
    //     }

    // @Test
    // fun `onDismiss should be called if cancel button is clicked`() =
    //     runComposeUiTest {
    //         var called = false

    //         setContent { CreateCounterDialog(onConfirm = { }, onDismiss = { called = true }) }

    //         onNode(cancelButton).performClick()
    //         waitForIdle()

    //         assert(called)
    //     }
}

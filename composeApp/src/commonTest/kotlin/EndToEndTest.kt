import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.runComposeUiTest
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString
import resources.Res
import resources.main_screen_create_button
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class EndToEndTest {
    private val fab = SemanticsMatcher.expectValue(
        SemanticsProperties.Role,
        Role.Button
    ) and hasText(runBlocking { getString(Res.string.main_screen_create_button) })

    @Test
    fun `FAB should be displayed`() =
        runComposeUiTest {
            setContent { CompositionLocalProvider() { App() } }

            onNode(fab).assertExists()
        }
}

package pl.janzak.cmp_demo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import model.Counter
import model.CounterId
import model.HomeViewModel
import model.NumberViewModel
import ui.screens.CreateCounterDialog
import ui.screens.DeleteCounterDialog
import ui.screens.EditCounterDialog
import ui.screens.HomeScreen
import ui.screens.NumberScreen

val defaultCounter = Counter(id = CounterId(0), name = "Test counter", value = 42)

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
fun HomeScreenPreview() {
    SharedTransitionScope {
        AnimatedVisibility(true, modifier = it) {
            HomeScreen(
                navigateCounter = {},
                sharedTransitionScope = this@SharedTransitionScope,
                animatedVisibilityScope = this@AnimatedVisibility,
                viewModel = object : HomeViewModel() {
                    override val counters: Flow<List<Pair<CounterId, Counter>>> =
                        flowOf(listOf(defaultCounter.id to defaultCounter))

                    override fun create(name: String) = Unit
                }
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
fun NumberScreenPreview() {
    SharedTransitionScope {
        AnimatedVisibility(true, modifier = it) {
            NumberScreen(
                goUp = {},
                counterId = defaultCounter.id,
                sharedTransitionScope = this@SharedTransitionScope,
                animatedVisibilityScope = this@AnimatedVisibility,
                viewModel = object : NumberViewModel() {
                    override val counter: Flow<Counter?> = flowOf(defaultCounter)
                    override fun increment() = Unit
                    override fun decrement() = Unit
                    override fun delete() = Unit
                    override fun rename(newName: String) = Unit
                }
            )
        }
    }
}


@Preview
@Composable
fun DeleteCounterDialogPreview() {
    DeleteCounterDialog(name = "Test counter", onConfirm = {}, onDismiss = {})
}

@Preview
@Composable
fun EditCounterDialogPreview() {
    EditCounterDialog(name = "Test counter", onConfirm = {}, onDismiss = {})
}

@Preview
@Composable
fun CreateCounterDialogPreview() {
    CreateCounterDialog(onConfirm = {}, onDismiss = {})
}



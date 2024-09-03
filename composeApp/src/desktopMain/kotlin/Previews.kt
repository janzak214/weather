import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import model.Counter
import model.CounterId
import model.HomeViewModel
import model.NumberViewModel
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
                counterId = CounterId(0),
                sharedTransitionScope = this@SharedTransitionScope,
                animatedVisibilityScope = this@AnimatedVisibility,
                viewModel = object : NumberViewModel() {
                    override val counter: Flow<Counter?> = flowOf(defaultCounter)
                    override fun increment() = Unit
                    override fun decrement() = Unit
                    override fun delete() = Unit
                    override fun rename(newName: String) = Unit
                },
            )
        }
    }
}

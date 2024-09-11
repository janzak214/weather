import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import pl.janzak.weather.model.Counter
import pl.janzak.weather.model.CounterId
import model.CounterListViewModel
import model.CounterViewModel
import ui.screens.CounterListScreen
import ui.screens.CounterScreen

val defaultCounter = Counter(id = CounterId(0), name = "Test counter", value = 42)

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
fun HomeScreenPreview() {
    SharedTransitionScope {
        AnimatedVisibility(true, modifier = it) {
            CounterListScreen(
                navigateCounter = {},
                sharedTransitionScope = this@SharedTransitionScope,
                animatedVisibilityScope = this@AnimatedVisibility,
                viewModel = object : CounterListViewModel() {
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
            CounterScreen(
                goUp = {},
                counterId = CounterId(0),
                sharedTransitionScope = this@SharedTransitionScope,
                animatedVisibilityScope = this@AnimatedVisibility,
                viewModel = object : CounterViewModel() {
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

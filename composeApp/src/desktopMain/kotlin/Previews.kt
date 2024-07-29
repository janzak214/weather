import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import model.CounterId
import ui.screens.HomeScreen
import ui.screens.NumberScreen

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

                )
        }
    }
}

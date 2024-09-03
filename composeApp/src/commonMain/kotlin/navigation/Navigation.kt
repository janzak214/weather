package navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import model.CounterId
import ui.screens.CounterListScreen
import ui.screens.CounterScreen
import ui.theme.Easing

sealed class Route {
    @Serializable
    data object Home : Route()

    @Serializable
    data class Counter(val id: Int) : Route()
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    SharedTransitionLayout {
        NavHost(navController = navController, startDestination = Route.Home) {
            composable<Route.Home>(
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None }) {
                CounterListScreen(
                    navigateCounter = { navController.navigate(Route.Counter(it.raw)) },
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this@composable,
                )
            }

            composable<Route.Counter>(
                enterTransition = {

                    fadeIn(
                        animationSpec = tween(
                            durationMillis = Easing.enterDuration,
                            easing = Easing.emphasizedDecelerate
                        )
                    ) + slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Start,
                        animationSpec = tween(
                            durationMillis = Easing.enterDuration,
                            easing = Easing.emphasizedDecelerate
                        )
                    )
                },
                exitTransition = {
                    fadeOut(
                        animationSpec = tween(
                            durationMillis = Easing.exitDuration,
                            easing = Easing.emphasizedAccelerate
                        )
                    ) + slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.End,
                        animationSpec = tween(
                            durationMillis = Easing.exitDuration,
                            easing = Easing.emphasizedAccelerate
                        )
                    )
                },
            ) { backStackEntry ->
                val route: Route.Counter = backStackEntry.toRoute()
                CounterScreen(
                    goUp = navController::navigateUp,
                    counterId = CounterId(route.id),
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this@composable,
                )
            }
        }
    }
}
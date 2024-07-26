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
import ui.screens.HomeScreen
import ui.screens.NumberScreen
import ui.theme.Easing

sealed class Route {
    @Serializable
    data object Home : Route()

    @Serializable
    data class Number(val value: Int) : Route()
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
                HomeScreen(
                    navigateNumber = { navController.navigate(Route.Number(it)) },
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this@composable,
                )
                HandleBack(navController)
            }

            composable<Route.Number>(
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
                val route: Route.Number = backStackEntry.toRoute()
                HandleBack(navController)
                NumberScreen(
                    goUp = {
                        if (!duringScreenTransition(navController)) {
                            navController.navigateUp()
                        }
                    },
                    number = route.value,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this@composable,
                )
            }
        }
    }
}
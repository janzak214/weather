package navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import model.CounterId
import ui.screens.CounterListScreen
import ui.screens.CounterScreen
import ui.screens.WebScreen
import ui.theme.Easing

sealed class Route {
    @Serializable
    data object Home : Route()

    @Serializable
    data object MainCounter : Route()

    @Serializable
    data object MainWeb : Route()

    @Serializable
    data object MainCake : Route()

    @Serializable
    data object CounterList : Route()

    @Serializable
    data class Counter(val id: Int) : Route()
}

fun destinationIncludesRoute(destination: NavDestination?, route: Route): Boolean =
    destination?.hierarchy?.any {
        it.route == route::class.qualifiedName
    } == true


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun NavigationBarNavHost(
    navController: NavHostController,
    transitionScope: SharedTransitionScope,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Route.MainCounter,
        modifier = modifier,
    ) {
        navigation<Route.MainCounter>(startDestination = Route.CounterList) {
            composable<Route.CounterList>(
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None }) C@{
                CounterListScreen(
                    navigateCounter = { navController.navigate(Route.Counter(it.raw)) },
                    sharedTransitionScope = transitionScope,
                    animatedVisibilityScope = this@C,
                )
            }

            composable<Route.Counter>(
                enterTransition = {
                    if (destinationIncludesRoute(initialState.destination, Route.MainCounter)) {
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
                    } else {
                        fadeIn()
                    }
                },
                exitTransition = {
                    if (destinationIncludesRoute(targetState.destination, Route.MainCounter)) {
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
                    } else {
                        fadeOut()
                    }
                },
            ) C@{ backStackEntry ->
                val route: Route.Counter = backStackEntry.toRoute()
                CounterScreen(
                    goUp = navController::navigateUp,
                    counterId = CounterId(route.id),
                    sharedTransitionScope = transitionScope,
                    animatedVisibilityScope = this@C,
                )
            }
        }

        composable<Route.MainWeb>(
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
        ) {
            WebScreen()
        }


        composable<Route.MainCake>(
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
        ) {
            Text("🍰🍰🍰")
        }


    }
}


private data class NavigationBarRoute(val name: String, val route: Route, val icon: ImageVector)

private val navigationBarRoutes = listOf(
    NavigationBarRoute(name = "Home", route = Route.MainCounter, icon = Icons.Default.Home),
    NavigationBarRoute(name = "Web", route = Route.MainWeb, icon = Icons.Default.Web),
    NavigationBarRoute(name = "Cake", route = Route.MainCake, icon = Icons.Default.Cake),
)

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val nestedNavController = rememberNavController()


    SharedTransitionLayout {
        NavHost(navController = navController, startDestination = Route.Home) {
            composable<Route.Home> {
                Scaffold(bottomBar = {
                    val navBackStackEntry by nestedNavController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination

                    NavigationBar {
                        SideEffect {
                            println(currentDestination)
                        }
                        for (route in navigationBarRoutes) {
                            NavigationBarItem(
                                icon = { Icon(route.icon, null) },
                                label = { Text(route.name) },
                                selected = destinationIncludesRoute(
                                    currentDestination,
                                    route.route
                                ),
                                onClick = {
                                    nestedNavController.navigate(route.route) {
                                        // Pop up to the start destination of the graph to
                                        // avoid building up a large stack of destinations
                                        // on the back stack as users select items
                                        popUpTo(nestedNavController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        // Avoid multiple copies of the same destination when
                                        // reselecting the same item
                                        launchSingleTop = true
                                        // Restore state when reselecting a previously selected item
                                        restoreState = true

                                    }
                                }
                            )
                        }
                    }
                }) { padding ->
                    NavigationBarNavHost(
                        navController = nestedNavController,
                        transitionScope = this@SharedTransitionLayout,
                        modifier = Modifier.padding(padding)
                    )
                }
            }
        }
    }
}
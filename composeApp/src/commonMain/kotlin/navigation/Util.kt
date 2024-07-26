package navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

fun duringScreenTransition(navHostController: NavHostController): Boolean {
    val visibleDestination = navHostController.visibleEntries.value.firstOrNull()?.destination
    val currentDestination = navHostController.currentDestination

    println("duringScreenTransition ${navHostController.visibleEntries.value}")
    println("duringScreenTransition visible: $visibleDestination")
    println("duringScreenTransition current: $currentDestination")

    return visibleDestination !== currentDestination
}

/**
 * Overrides back button handler so that it can't be triggered during screen transition.
 */
@Composable
expect fun HandleBack(navHostController: NavHostController)

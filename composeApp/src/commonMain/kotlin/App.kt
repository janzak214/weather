import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.PathEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable


import demo.composeapp.generated.resources.Res
import demo.composeapp.generated.resources.compose_multiplatform

sealed class Route {
    @Serializable
    data object Home : Route()

    @Serializable
    data class Number(val value: Int) : Route()
}


@Composable
@Preview
fun Test2() {
    var showContent by remember { mutableStateOf(false) }

    Button(onClick = { showContent = !showContent }) {
        Text("Click me!")
    }
    AnimatedVisibility(showContent) {
        val greeting = remember { Greeting().greet() }
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(painterResource(Res.drawable.compose_multiplatform), null)
            Text("Compose: $greeting")
        }
    }
}


object Easing {
    val emphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
    val emphasizedAccelerate = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)

    val enterDuration = 400
    val exitDuration = 200
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App() {

    val navController = rememberNavController()
    MaterialTheme {

        NavHost(navController = navController, startDestination = Route.Home) {
            composable<Route.Home>(
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None }) {
                Scaffold(topBar = {
                    TopAppBar(
                        title = { Text("Home") },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.primary,
                        ),
                    )
                }) { padding ->
                    Column(
                        Modifier.fillMaxWidth().padding(padding),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row {
                            for (i in 0..4) {
                                Button(onClick = {
                                    navController.navigate(Route.Number(i))
                                }) { Text("Route $i") }
                            }
                        }

                        Test2()
                    }
                }
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
                NumberScreen({ navController.popBackStack() }, route.value)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumberScreen(goBack: () -> Unit, number: Int) {
    val animationState =
        remember { MutableTransitionState(false).apply { targetState = true } }
    val stateName = when {
        animationState.isIdle && animationState.currentState -> "Visible"
        !animationState.isIdle && animationState.currentState -> "Disappearing"
        animationState.isIdle && !animationState.currentState -> "Invisible"
        else -> "Appearing"
    }

    AnimatedVisibility(
        visibleState = animationState,
        enter = EnterTransition.None,
        exit = ExitTransition.None
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Hello $stateName") },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (animationState.isIdle && animationState.currentState) {
                                println("Wtf"); goBack()
                            }
                        }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                "Go back"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                )
            }
        ) { padding ->

            Column(
                Modifier.fillMaxWidth().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Number: $number",
                    style = MaterialTheme.typography.displayLarge,
                    modifier = Modifier.animateEnterExit(
                        enter = fadeIn(
                            tween(
                                durationMillis = Easing.enterDuration,
                                easing = Easing.emphasizedDecelerate
                            )
                        )
                    )
                )
                Test2()
            }
        }
    }
}

@Preview
@Composable
fun Wtf() {

}

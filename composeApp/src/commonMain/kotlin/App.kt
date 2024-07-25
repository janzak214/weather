import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.core.stack.StackEvent
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.NavigatorDisposeBehavior
import cafe.adriel.voyager.transitions.ScreenTransition
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

class HomeRoute : Screen {
    override val key: ScreenKey = uniqueScreenKey

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        HomeScreen(navigateNumber = { navigator?.push(NumberRoute(it)) })
    }

}


@OptIn(ExperimentalVoyagerApi::class)
class SlideFadeTransition : ScreenTransition {
    override fun enter(lastEvent: StackEvent): EnterTransition {
        println("enter: $lastEvent")
        return fadeIn()

        return when (lastEvent) {
            StackEvent.Push -> slideInHorizontally { it } + fadeIn()
            StackEvent.Pop -> fadeIn(
                initialAlpha = 0.0f,
                animationSpec = tween(delayMillis = 0, durationMillis = 200)
            )

            else -> EnterTransition.None
//            StackEvent.Idle -> TODO()
        }


//        return slideIn { size ->
//            val x = if (lastEvent == StackEvent.Pop) -size.width else size.width
//            IntOffset(x = x, y = 0)
//        } + fadeIn()
    }

    override fun exit(lastEvent: StackEvent): ExitTransition {
        println("exit: $lastEvent")
//        if (lastEvent == StackEvent.Push) {
//            return ExitTransition.None
//        }
        return fadeOut()

        return when (lastEvent) {
            StackEvent.Push -> fadeOut(
                targetAlpha = 0.99f,
                animationSpec = tween(durationMillis = 300)
            )

            StackEvent.Pop -> slideOutHorizontally { it } + fadeOut()
            else -> ExitTransition.None
//            StackEvent.Idle -> TODO()
        }

//        return slideOut { size ->
//            val x = if (lastEvent == StackEvent.Pop) size.width else -size.width
//            IntOffset(x = x, y = 0)
//        } + fadeOut()
    }
}

@OptIn(ExperimentalVoyagerApi::class)
data class NumberRoute(val number: Int) : Screen { //}, ScreenTransition by SlideFadeTransition() {
    override val key: ScreenKey = uniqueScreenKey

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val visible = navigator?.lastItemOrNull is NumberRoute
        NumberScreen(goUp = { navigator?.pop() }, number = number, visible = visible)
    }


}


@Composable
fun ComposeNavigation() {
    val navController = rememberNavController()

//    BackHandler(backHandlingEnabled) {
    NavHost(navController = navController, startDestination = Route.Home) {
        composable<Route.Home>(
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }) {
            HomeScreen(navigateNumber = { navController.navigate(Route.Number(it)) })
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
                {
                    if (!duringScreenTransition(navController)) {
                        navController.navigateUp()
                    }
                },
                route.value
            )
        }
    }
}


@OptIn(ExperimentalVoyagerApi::class)
@Composable
fun VoyagerNavigation() {
    Navigator(
        HomeRoute(),
        disposeBehavior = NavigatorDisposeBehavior(disposeSteps = false)
    ) { navigator ->
        ScreenTransition(
            navigator = navigator,
            transition = {
                fadeIn(
                    initialAlpha = 0.99f,
                    animationSpec = tween()
                ) togetherWith fadeOut(targetAlpha = 0.99f)
            },
//                enterTransition = {ContentTransform()},
//                exitTransition = {ExitTransition.None},
            disposeScreenAfterTransitionEnd = true,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalVoyagerApi::class)
@Composable
@Preview
fun App() {

    MaterialTheme {
        ComposeNavigation()
//        VoyagerNavigation()
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navigateNumber: (number: Int) -> Unit) {
    Scaffold(topBar = {
        CenterAlignedTopAppBar(
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
                        navigateNumber(i)
                    }) { Text("Route $i") }
                }
            }

            Test2()
        }
    }
}

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumberScreen(goUp: () -> Unit, number: Int, visible: Boolean = true) {
    val animationState =
        remember { MutableTransitionState(true) }
    val stateName = when {
        animationState.isIdle && animationState.currentState -> "Visible"
        !animationState.isIdle && animationState.currentState -> "Disappearing"
        animationState.isIdle && !animationState.currentState -> "Invisible"
        else -> "Appearing"
    }

    LaunchedEffect(stateName) {
        println("State: $stateName")
    }

    LaunchedEffect(visible) {
        println("Visible: $visible")
        animationState.targetState = visible
    }

    AnimatedVisibility(
        visibleState = animationState,
        enter = EnterTransition.None,
        exit = ExitTransition.None,
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Number $number") },
                    navigationIcon = {
                        IconButton(onClick = {
                            goUp()
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
                    modifier = Modifier.animateEnterExit(enter = fadeIn(), exit = fadeOut())
                )
            }
        ) { padding ->

            Column(
                Modifier.fillMaxWidth().padding(padding)
                    .animateEnterExit(
                        enter = slideInHorizontally { it },
                        exit = slideOutHorizontally { it },
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Number: $number",
                    style = MaterialTheme.typography.displayLarge,
                    modifier = Modifier.animateEnterExit(
                        enter = scaleIn(
                            spring(stiffness = Spring.StiffnessLow)
                        ),
                        exit = scaleOut(spring())
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

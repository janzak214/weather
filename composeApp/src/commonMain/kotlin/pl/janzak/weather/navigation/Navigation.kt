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
import androidx.core.bundle.Bundle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import pl.janzak.weather.data.api.Coordinates
import pl.janzak.weather.model.CounterId
import pl.janzak.weather.model.LocationInfo
import pl.janzak.weather.model.LocationName
import pl.janzak.weather.ui.screens.DetailsScreen
import pl.janzak.weather.ui.screens.MainScreen
import ui.screens.CakeScreen
import ui.screens.CounterListScreen
import ui.screens.CounterScreen
import ui.screens.WebScreen
import ui.theme.Easing
import kotlin.reflect.typeOf

sealed class Route {
    @Serializable
    data object Main : Route()

    @Serializable
    data class Details(val location: String) : Route()
}

fun destinationIncludesRoute(destination: NavDestination?, route: Route): Boolean =
    destination?.hierarchy?.any {
        it.route == route::class.qualifiedName
    } == true


val locationInfoType = object : NavType<LocationInfo>(isNullableAllowed = false) {
    override fun get(bundle: Bundle, key: String): LocationInfo? {
        if (!bundle.containsKey("$key-name") or !bundle.containsKey("$key-region")) return null

        return LocationInfo(
            coordinates = Coordinates(
                bundle.getDouble("$key-latitude"),
                bundle.getDouble("$key-longitude"),
            ),
            name = LocationName(
                name = bundle.getString("$key-name")!!,
                region = bundle.getString("$key-region")!!,
            )
        )
    }

    override fun parseValue(value: String): LocationInfo = Json.decodeFromString(value)

    override fun serializeAsValue(value: LocationInfo): String = Json.encodeToString(value)

    override fun put(bundle: Bundle, key: String, value: LocationInfo) {
        bundle.putDouble("$key-latitude", value.coordinates.latitude)
        bundle.putDouble("$key-longitude", value.coordinates.longitude)
        bundle.putString("$key-name", value.name.name)
        bundle.putString("$key-region", value.name.region)
    }
}


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    SharedTransitionLayout {
        NavHost(navController = navController, startDestination = Route.Main) {
            composable<Route.Main>(
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None }
            ) {
                MainScreen(openLocation = {

//                    navController.navigate(Route.Details(it))
                    navController.navigate(Route.Details(Json.encodeToString(it)))
                })
            }
            composable<Route.Details>(
                typeMap = mapOf(typeOf<LocationInfo>() to locationInfoType),
                enterTransition = {
                    if (destinationIncludesRoute(initialState.destination, Route.Main)) {
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
                    if (destinationIncludesRoute(targetState.destination, Route.Main)) {
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
                }
            ) {
                val route: Route.Details = it.toRoute()
                DetailsScreen(
                    locationInfo = Json.decodeFromString(route.location),
                    goBack = {
                        navController.navigateUp()
                    })
            }
        }
    }
}

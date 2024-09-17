@file:OptIn(ExperimentalSharedTransitionApi::class)

package pl.janzak.weather.ui.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.dp
import pl.janzak.weather.model.FavoriteLocation
import pl.janzak.weather.model.LocationInfo
import pl.janzak.weather.ui.theme.Easing

context(AnimatedVisibilityScope, SharedTransitionScope)
@Composable
fun MainView(
    favorites: List<FavoriteLocation>?,
    locations: List<LocationInfo>,
    fetchLocations: (String) -> Unit,
    handleGeolocalization: (done: (LocationInfo) -> Unit) -> Unit,
    openLocation: (LocationInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).then(modifier)) {
            LocationSearch(
                locations,
                fetchLocations,
                handleGeolocalization,
                openLocation,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .renderInSharedTransitionScopeOverlay(zIndexInOverlay = 1f)
                    .animateEnterExit(
                        enter = slideInVertically(Easing.enterTween()) { -it },
                        exit = slideOutVertically(Easing.enterTween()) { -it }),
            )

            if (favorites == null) {
                Box(
                    modifier = Modifier.matchParentSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
                return@Box
            }

            LazyColumn(
                modifier = Modifier.semantics { traversalIndex = 1f }
                    .padding(16.dp)
                    .fillMaxSize(),
                userScrollEnabled = true,
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                item {
                    Spacer(modifier = Modifier.requiredHeight(110.dp))
                }

                items(favorites, key = { it.info.toString() }) {
                    WeatherOverviewCard(
                        it.info.name,
                        it.currentWeather,
                        it.forecast,
                        goToDetails = { openLocation(it.info) },
                        modifier = Modifier.padding(vertical = 8.dp)
                            .width(IntrinsicSize.Min)
                            .widthIn(400.dp)
                    )
                }
            }
        }
    }

}
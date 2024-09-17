@file:OptIn(ExperimentalSharedTransitionApi::class)

package pl.janzak.weather.ui.util

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp


context(AnimatedVisibilityScope, SharedTransitionScope)
@Composable
fun Modifier.containerTransform(key: SharedElementKey): Modifier =
    sharedBounds(
        rememberSharedContentState(key = key),
        animatedVisibilityScope = this@AnimatedVisibilityScope,
        resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds(ContentScale.FillWidth),
        clipInOverlayDuringTransition = OverlayClip(
            RoundedCornerShape(
                topStart = 10.dp,
                topEnd = 10.dp
            )
        )
    )

context(AnimatedVisibilityScope, SharedTransitionScope)
@Composable
fun Modifier.elementTransform(key: SharedElementKey): Modifier =
    this then sharedElement(
        rememberSharedContentState(key = key),
        animatedVisibilityScope = this@AnimatedVisibilityScope,
        clipInOverlayDuringTransition = OverlayClip(
            RoundedCornerShape(
                topStart = 10.dp,
                topEnd = 10.dp
            )
        )
    )


enum class SharedElementType {
    AppBar,
    Card,
    LocationInfo,
    CurrentWeather,
}

data class SharedElementKey(val id: Any, val type: SharedElementType, val subid: Int = 0) {
    fun sub(id: Int): SharedElementKey = copy(subid = id)
}

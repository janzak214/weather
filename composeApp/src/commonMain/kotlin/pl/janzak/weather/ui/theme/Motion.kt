package pl.janzak.weather.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween

object Easing {
    val emphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
    val emphasizedAccelerate = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)

    fun <T> exitTween(fast: Boolean = false): FiniteAnimationSpec<T> = tween(
        durationMillis = if (fast) {
            exitDurationFast
        } else {
            exitDuration
        },
        easing = emphasizedAccelerate
    )

    fun <T> enterTween(fast: Boolean = false): FiniteAnimationSpec<T> = tween(
        durationMillis = if (fast) {
            enterDurationFast
        } else {
            enterDuration
        },
        easing = emphasizedDecelerate
    )

    val enterDuration = 400
    val enterDurationFast = 350
    val exitDuration = 300
    val exitDurationFast = 260
}

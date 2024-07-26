package ui.theme

import androidx.compose.animation.core.CubicBezierEasing

object Easing {
    val emphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
    val emphasizedAccelerate = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)

    val enterDuration = 400
    val exitDuration = 200
}

package pl.janzak.weather.ui.util

import androidx.compose.ui.util.lerp
import kotlin.math.ceil
import kotlin.math.floor

data class Scale(
    val domain: ClosedFloatingPointRange<Float>,
    val range: ClosedFloatingPointRange<Float>,
    val mapDomain: Scale.(x: Float) -> Float = { it },
    val mapRange: Scale.(x: Float) -> Float = { it },
) {
    operator fun invoke(value: Float): Float =
        mapRange(
            lerp(
                range.start,
                range.endInclusive,
                (mapDomain(value) - domain.start) / (domain.endInclusive - domain.start)
            )
        )

    fun nice(stepSize: Float): Scale = copy(
        domain = floor(domain.start / stepSize) * stepSize
                ..ceil(domain.endInclusive / stepSize) * stepSize
    )

    fun ticks(stepSize: Float): List<Float> {
        val rangeSize = domain.endInclusive - domain.start
        val count = ceil(rangeSize / stepSize).toInt()

        return (0..count).map {
            domain.start + it * stepSize
        }
    }
}
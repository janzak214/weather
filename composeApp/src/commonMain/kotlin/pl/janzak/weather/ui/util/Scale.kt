package pl.janzak.weather.ui.util

import androidx.compose.ui.util.lerp
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

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

    fun nice(stepSize: Float, maxCount: Int = Int.MAX_VALUE): Scale {
        val (newStepSize, _) = calculateStepSize(stepSize, maxCount)
        return copy(
            domain = floor(domain.start / newStepSize) * newStepSize
                    ..ceil(domain.endInclusive / newStepSize) * newStepSize
        )
    }

    fun ticks(stepSize: Float, maxCount: Int = Int.MAX_VALUE): List<Float> {
        val (newStepSize, count) = calculateStepSize(stepSize, maxCount)

        return (0..count).map {
            domain.start + it * newStepSize
        }
    }

    private fun calculateStepSize(stepSize: Float, maxCount: Int): Pair<Float, Int> {
        val rangeSize = domain.endInclusive - domain.start

        return generateSequence(stepSize) { it * 2 }
            .map { it to ceil(rangeSize / it).roundToInt() }
            .first { it.second <= maxCount }
    }
}

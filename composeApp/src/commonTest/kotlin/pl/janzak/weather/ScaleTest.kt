package pl.janzak.weather

import pl.janzak.weather.ui.util.Scale
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals

class ScaleTest {
    @Test
    fun scaleMapsValuesCorrectly() {
        val scale = Scale(0f..10f, 0f..20f)
        assertEquals(0f, scale(0f), 1e-5f)
        assertEquals(10f, scale(5f), 1e-5f)
        assertEquals(20f, scale(10f), 1e-5f)
    }

    @Test
    fun scaleSupportsNegativeValues() {
        val scale = Scale(-1f..1f, 0f..1f)
        assertEquals(0f, scale(-1f), 1e-5f)
        assertEquals(0.5f, scale(0f), 1e-5f)
        assertEquals(1f, scale(1f), 1e-5f)
    }

    @Test
    fun scaleCanAlignDomainToTicks() {
        val scale = Scale(2f..19f, 0f..1f)
        val aligned = scale.nice(5f)
        assertEquals(0f, aligned.domain.start, 1e-5f)
        assertEquals(20f, aligned.domain.endInclusive, 1e-5f)
    }

    @Test
    fun scaleCanMapInput() {
        val scale = Scale(0f..1f, 0f..10f, mapDomain = { sqrt(it) })
        assertEquals(0f, scale(0f), 1e-5f)
        assertEquals(5f, scale(0.25f), 1e-5f)
        assertEquals(10f, scale(1f), 1e-5f)
    }

    @Test
    fun scaleCanMapOutput() {
        val scale = Scale(0f..1f, 0f..10f, mapRange = { range.endInclusive - it })
        assertEquals(10f, scale(0f), 1e-5f)
        assertEquals(5f, scale(0.5f), 1e-5f)
        assertEquals(0f, scale(1f), 1e-5f)
    }

    @Test
    fun scaleCanGenerateTicks() {
        val scale = Scale(0f..10f, 0f..1f)
        val ticks = scale.ticks(1f)
        assertEquals(listOf(0f, 1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 10f), ticks)
    }

    @Test
    fun scaleCanGenerateTicksAdjustingStep() {
        val scale = Scale(0f..10f, 0f..1f)
        val ticks = scale.ticks(1f, 6)
        assertEquals(listOf(0f, 2f, 4f, 6f, 8f, 10f), ticks)
    }

}

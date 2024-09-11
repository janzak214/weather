package ui.util

import androidx.compose.runtime.compositionLocalOf

data class Units(
    val temperature: String = "°C",
    val windSpeed: String = "km/h",
    val pressure: String = "hPa",
    val precipitation: String = "mm"
) {
    fun formatTemperature(value: Double) = "%.1f%s".format(value, temperature)
    fun formatWindSpeed(value: Double) = "%.0f %s".format(value, windSpeed)
    fun formatPressure(value: Double) = "%.0f %s".format(value, pressure)
    fun formatPrecipitation(value: Double) = "%.0f %s".format(value, precipitation)
    fun formatPercentage(value: Double) = "%.0f%%".format(value)
}

val LocalUnits = compositionLocalOf { Units() }
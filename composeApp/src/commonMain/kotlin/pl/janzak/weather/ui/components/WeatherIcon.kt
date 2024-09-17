package pl.janzak.weather.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import compose.icons.WeatherIcons
import compose.icons.weathericons.Cloudy
import compose.icons.weathericons.DayCloudy
import compose.icons.weathericons.DayFog
import compose.icons.weathericons.DayRain
import compose.icons.weathericons.DayRainMix
import compose.icons.weathericons.DayShowers
import compose.icons.weathericons.DaySleetStorm
import compose.icons.weathericons.DaySnow
import compose.icons.weathericons.DaySnowWind
import compose.icons.weathericons.DaySprinkle
import compose.icons.weathericons.DaySunny
import compose.icons.weathericons.DayThunderstorm
import compose.icons.weathericons.NightAltCloudy
import compose.icons.weathericons.NightAltRain
import compose.icons.weathericons.NightAltRainMix
import compose.icons.weathericons.NightAltShowers
import compose.icons.weathericons.NightAltSleetStorm
import compose.icons.weathericons.NightAltSnow
import compose.icons.weathericons.NightAltSnowWind
import compose.icons.weathericons.NightAltSprinkle
import compose.icons.weathericons.NightAltThunderstorm
import compose.icons.weathericons.NightClear
import compose.icons.weathericons.NightFog
import pl.janzak.weather.model.WeatherCode

private val codeToIconMap = buildMap {
    put(WeatherCode.CLEAR to true, WeatherIcons.DaySunny)
    put(WeatherCode.MAINLY_CLEAR to true, WeatherIcons.DayCloudy)
    put(WeatherCode.PARTLY_CLOUDY to true, WeatherIcons.DayCloudy)
    put(WeatherCode.OVERCAST to true, WeatherIcons.Cloudy)
    put(WeatherCode.FOG to true, WeatherIcons.DayFog)
    put(WeatherCode.DEPOSITING_RIME_FOG to true, WeatherIcons.DayFog)
    put(WeatherCode.DRIZZLE_LIGHT to true, WeatherIcons.DaySprinkle)
    put(WeatherCode.DRIZZLE_MODERATE to true, WeatherIcons.DaySprinkle)
    put(WeatherCode.DRIZZLE_DENSE to true, WeatherIcons.DaySprinkle)
    put(WeatherCode.FREEZING_DRIZZLE_LIGHT to true, WeatherIcons.DayRainMix)
    put(WeatherCode.FREEZING_DRIZZLE_DENSE to true, WeatherIcons.DayRainMix)
    put(WeatherCode.RAIN_SLIGHT to true, WeatherIcons.DayRain)
    put(WeatherCode.RAIN_MODERATE to true, WeatherIcons.DayRain)
    put(WeatherCode.RAIN_HEAVY to true, WeatherIcons.DayRain)
    put(WeatherCode.FREEZING_RAIN_LIGHT to true, WeatherIcons.DayRainMix)
    put(WeatherCode.FREEZING_RAIN_HEAVY to true, WeatherIcons.DayRainMix)
    put(WeatherCode.SNOW_FALL_SLIGHT to true, WeatherIcons.DaySnow)
    put(WeatherCode.SNOW_FALL_MODERATE to true, WeatherIcons.DaySnow)
    put(WeatherCode.SNOW_FALL_HEAVY to true, WeatherIcons.DaySnow)
    put(WeatherCode.SNOW_GRAINS to true, WeatherIcons.DaySnow)
    put(WeatherCode.RAIN_SHOWERS_SLIGHT to true, WeatherIcons.DayShowers)
    put(WeatherCode.RAIN_SHOWERS_MODERATE to true, WeatherIcons.DayShowers)
    put(WeatherCode.RAIN_SHOWERS_VIOLENT to true, WeatherIcons.DayShowers)
    put(WeatherCode.SNOW_SHOWERS_SLIGHT to true, WeatherIcons.DaySnowWind)
    put(WeatherCode.SNOW_SHOWERS_HEAVY to true, WeatherIcons.DaySnowWind)
    put(WeatherCode.THUNDERSTORM_SLIGHT to true, WeatherIcons.DayThunderstorm)
    put(WeatherCode.THUNDERSTORM_SLIGHT_HAIL to true, WeatherIcons.DaySleetStorm)
    put(WeatherCode.THUNDERSTORM_HEAVY_HAIL to true, WeatherIcons.DaySleetStorm)

    put(WeatherCode.CLEAR to false, WeatherIcons.NightClear)
    put(WeatherCode.MAINLY_CLEAR to false, WeatherIcons.NightAltCloudy)
    put(WeatherCode.PARTLY_CLOUDY to false, WeatherIcons.NightAltCloudy)
    put(WeatherCode.OVERCAST to false, WeatherIcons.Cloudy)
    put(WeatherCode.FOG to false, WeatherIcons.NightFog)
    put(WeatherCode.DEPOSITING_RIME_FOG to false, WeatherIcons.NightFog)
    put(WeatherCode.DRIZZLE_LIGHT to false, WeatherIcons.NightAltSprinkle)
    put(WeatherCode.DRIZZLE_MODERATE to false, WeatherIcons.NightAltSprinkle)
    put(WeatherCode.DRIZZLE_DENSE to false, WeatherIcons.NightAltSprinkle)
    put(WeatherCode.FREEZING_DRIZZLE_LIGHT to false, WeatherIcons.NightAltRainMix)
    put(WeatherCode.FREEZING_DRIZZLE_DENSE to false, WeatherIcons.NightAltRainMix)
    put(WeatherCode.RAIN_SLIGHT to false, WeatherIcons.NightAltRain)
    put(WeatherCode.RAIN_MODERATE to false, WeatherIcons.NightAltRain)
    put(WeatherCode.RAIN_HEAVY to false, WeatherIcons.NightAltRain)
    put(WeatherCode.FREEZING_RAIN_LIGHT to false, WeatherIcons.NightAltRainMix)
    put(WeatherCode.FREEZING_RAIN_HEAVY to false, WeatherIcons.NightAltRainMix)
    put(WeatherCode.SNOW_FALL_SLIGHT to false, WeatherIcons.NightAltSnow)
    put(WeatherCode.SNOW_FALL_MODERATE to false, WeatherIcons.NightAltSnow)
    put(WeatherCode.SNOW_FALL_HEAVY to false, WeatherIcons.NightAltSnow)
    put(WeatherCode.SNOW_GRAINS to false, WeatherIcons.NightAltSnow)
    put(WeatherCode.RAIN_SHOWERS_SLIGHT to false, WeatherIcons.NightAltShowers)
    put(WeatherCode.RAIN_SHOWERS_MODERATE to false, WeatherIcons.NightAltShowers)
    put(WeatherCode.RAIN_SHOWERS_VIOLENT to false, WeatherIcons.NightAltShowers)
    put(WeatherCode.SNOW_SHOWERS_SLIGHT to false, WeatherIcons.NightAltSnowWind)
    put(WeatherCode.SNOW_SHOWERS_HEAVY to false, WeatherIcons.NightAltSnowWind)
    put(WeatherCode.THUNDERSTORM_SLIGHT to false, WeatherIcons.NightAltThunderstorm)
    put(WeatherCode.THUNDERSTORM_SLIGHT_HAIL to false, WeatherIcons.NightAltSleetStorm)
    put(WeatherCode.THUNDERSTORM_HEAVY_HAIL to false, WeatherIcons.NightAltSleetStorm)
}

@Composable
fun WeatherIcon(
    code: WeatherCode,
    isDay: Boolean,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current
) {
    val icon = remember(code, isDay) { getWeatherIcon(code, isDay) }
    Icon(icon, null, modifier = modifier, tint = tint)
}

fun getWeatherIcon(code: WeatherCode, isDay: Boolean): ImageVector =
    codeToIconMap[code to isDay]!!

package pl.janzak.weather.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import pl.janzak.weather.model.WeatherCode
import pl.janzak.weather.ui.util.Scale
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.roundToInt
import kotlin.math.sqrt

private fun Offset.pixels(): Offset = Offset(round(x), round(y))
private fun Size.pixels(): Size = Size(round(width), round(height))

@Composable
fun WeatherPlot(
    temperatures: List<DoubleArray>,
    visibilities: List<Boolean>,
    weatherCode: List<WeatherCode>,
    precipitations: List<DoubleArray>,
    startDate: LocalDate,
    scrollPosition: Float,
    colors: List<Color>,
    aggregate: Boolean,
    modifier: Modifier = Modifier
) {
    val background = MaterialTheme.colorScheme.background
    val typography = MaterialTheme.typography
    val gridColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
    val lineColor = MaterialTheme.colorScheme.primary
    val lineColorLight = lineColor.copy(alpha = 0.2f)
    val textColor = MaterialTheme.colorScheme.onBackground
    val iconColor = MaterialTheme.colorScheme.secondary
    val textMeasurer = rememberTextMeasurer(cacheSize = 200)
    val minTemperature = remember(temperatures) { temperatures.minOf { it.min() } }.toFloat()
    val maxTemperature = remember(temperatures) { temperatures.maxOf { it.max() } }.toFloat()
    val minPrecipitation = remember(precipitations) { precipitations.minOf { it.min() } }.toFloat()
    val maxPrecipitation = remember(precipitations) { precipitations.maxOf { it.max() } }.toFloat()

    val dayLabelStyle = typography.labelLarge.copy(color = MaterialTheme.colorScheme.onSurface)
    val bigHourTickStyle = typography.labelMedium.copy(color = textColor)
    val smallHourTickStyle = typography.labelSmall.copy(color = textColor.copy(alpha = 0.5f))

    val density = LocalDensity.current.density

    val weatherIcons = remember(weatherCode) {
        weatherCode.windowed(3, 3) { items ->
            val counts = items.groupingBy { it }.eachCount()
            counts.entries.maxByOrNull { it.value }!!.value
        }
        weatherCode.withIndex().filter { (i, _) ->
            i % 6 == 0
        }.map { (i, code) ->
            code to (i % 24 == 6 || i % 24 == 12)
        }
    }
    val iconPainters = weatherIcons.distinct().associate { (code, isDay) ->
        (code to isDay) to rememberVectorPainter(getWeatherIcon(code, isDay))
    }

    Spacer(
        modifier = Modifier
            .padding(20.dp)
            .requiredHeight(290.dp)
            .requiredWidth(240.dp * 7)
            .then(modifier)

            .drawWithCache {
                val timeScale = Scale(0F..temperatures.first().size.toFloat(), 0F..size.width)
                val dayTicks = timeScale.ticks(24F)
                val hourTicks = timeScale.ticks(1F)

                val temperatureOffset = 40F * density
                val temperatureHeight = 120F * density
                val precipitationOffset = temperatureOffset + temperatureHeight + 20F * density
                val precipitationHeight = 60F * density


                val temperatureStep = 1F
                val temperatureScale = Scale(
                    minTemperature..maxTemperature,
                    0F..temperatureHeight,
                    mapRange = { range.endInclusive - it + temperatureOffset }
                ).nice(temperatureStep, maxCount = 8)
                val temperatureTicks = temperatureScale.ticks(temperatureStep, maxCount = 8)

                val precipitationStep = 1F
                val precipitationScale = Scale(
                    minPrecipitation..maxPrecipitation,
                    0F..precipitationHeight,
                    mapRange = { range.endInclusive - it + precipitationOffset }
                ).nice(precipitationStep, maxCount = 5)
                val precipitationTicks = precipitationScale.ticks(precipitationStep, maxCount = 5)

                onDrawBehind {
                    inset(left = 30f * density, right = 0f, top = 0f, bottom = 0f) {
                        for (tick in temperatureTicks) {
                            val yPosition = round(temperatureScale(tick))
                            drawLine(
                                color = gridColor,
                                start = Offset(0f, yPosition).pixels(),
                                end = Offset(this.size.width, yPosition).pixels(),
                            )
                        }

                        for (tick in hourTicks) {
                            val intTick = tick.roundToInt()
                            val xPosition = timeScale(tick)

                            if (intTick % 6 == 0) {
                                val strokeWidth = if (intTick % 24 == 0) {
                                    12f
                                } else {
                                    4f
                                } * density
                                drawLine(
                                    color = background,
                                    start = Offset(
                                        xPosition - strokeWidth / 4,
                                        -1f * density
                                    ).pixels(),
                                    end = Offset(
                                        xPosition - strokeWidth / 4,
                                        this.size.height
                                    ).pixels(),
                                    strokeWidth = strokeWidth
                                )

                                if (tick != hourTicks.last()) {
                                    val icon = weatherIcons[intTick / 6]
                                    val iconSize = 30 * density
                                    with(iconPainters[icon]!!) {
                                        translate(left = xPosition + iconSize / 2) {
                                            draw(
                                                Size(iconSize, iconSize),
                                                colorFilter = ColorFilter.tint(iconColor)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (aggregate) {
                            val mean = temperatures[0].mapIndexed { index, _ ->
                                temperatures
                                    .filterIndexed { t, _ -> visibilities[t] }
                                    .map { it[index] }.average()
                            }
                            val stddev = temperatures[0].mapIndexed { index, _ ->
                                temperatures
                                    .filterIndexed { t, _ -> visibilities[t] }
                                    .map { (it[index] - mean[index]).pow(2) }.average()
                                    .let { sqrt(it) }
                            }

                            val upperLine = Path().apply {
                                fillType = PathFillType.NonZero

                                mean.mapIndexed { hour, value ->
                                    val x = timeScale(hour.toFloat())
                                    val y =
                                        temperatureScale(value.toFloat() - stddev[hour].toFloat())
                                    if (hour == 0) {
                                        moveTo(x, y)
                                    } else {
                                        lineTo(x, y)
                                    }
                                }

                                mean.withIndex().reversed().map { (hour, value) ->
                                    val x = timeScale(hour.toFloat())
                                    val y =
                                        temperatureScale(value.toFloat() + stddev[hour].toFloat())
                                    lineTo(x, y)
                                }
                            }


                            drawPath(upperLine, color = lineColorLight)
                            drawPoints(
                                mean.mapIndexed { hour, value ->
                                    Offset(
                                        timeScale(hour.toFloat()),
                                        temperatureScale(value.toFloat())
                                    ).pixels()
                                },
                                pointMode = PointMode.Polygon,
                                cap = StrokeCap.Round,
                                color = lineColor,
                                strokeWidth = 2F * density,
                            )

                        } else {
                            temperatures.zip(visibilities)
                                .mapIndexed { index, (temperature, visible) ->
                                    if (visible) {
                                        drawPoints(
                                            temperature.mapIndexed { hour, value ->
                                                Offset(
                                                    timeScale(hour.toFloat()),
                                                    temperatureScale(value.toFloat())
                                                ).pixels()
                                            },
                                            pointMode = PointMode.Polygon,
                                            cap = StrokeCap.Round,
                                            color = colors[index],
                                            strokeWidth = 2F * density,
                                        )
                                    }
                                }
                        }


                        if (aggregate) {
                            val mean = precipitations[0].mapIndexed { index, _ ->
                                precipitations
                                    .filterIndexed { t, _ -> visibilities[t] }
                                    .map { it[index] }.average()
                            }
                            val stddev = precipitations[0].mapIndexed { index, _ ->
                                precipitations
                                    .filterIndexed { t, _ -> visibilities[t] }
                                    .map { (it[index] - mean[index]).pow(2) }.average()
                                    .let { sqrt(it) }
                            }

                            val width = timeScale(0.8f) - timeScale(0f)
                            val offset = -width / 2

                            mean.mapIndexed { index, precipitation ->
                                if (precipitation > 0.0) {
                                    val xPosition = timeScale(index.toFloat())
                                    val yPosition = precipitationScale(precipitation.toFloat())
                                    val yPosition2 =
                                        precipitationScale(precipitation.toFloat() + stddev[index].toFloat())


                                    drawRect(
                                        lineColorLight,
                                        topLeft = Offset(
                                            xPosition + offset,
                                            yPosition2
                                        ).pixels(),
                                        size = Size(
                                            width = width,
                                            height = precipitationHeight + precipitationOffset - yPosition2
                                        ).pixels()
                                    )

                                    drawRect(
                                        lineColor,
                                        topLeft = Offset(
                                            xPosition + offset,
                                            yPosition
                                        ).pixels(),
                                        size = Size(
                                            width = width,
                                            height = precipitationHeight + precipitationOffset - yPosition
                                        ).pixels()
                                    )
                                }
                            }
                        } else {
                            precipitations.withIndex().filter { (i, _) -> visibilities[i] }
                                .mapIndexed { i, (pi, p) ->
                                    val width = timeScale(0.8f) - timeScale(0f)
                                    val barWidth = width / visibilities.count { it }
                                    val offset = barWidth * i - width / 2

                                    p.mapIndexed { index, precipitation ->
                                        val xPosition = timeScale(index.toFloat())
                                        val yPosition = precipitationScale(precipitation.toFloat())

                                        drawRect(
                                            colors[pi],
                                            topLeft = Offset(
                                                xPosition + offset,
                                                yPosition
                                            ).pixels(),
                                            size = Size(
                                                width = barWidth,
                                                height = precipitationHeight + precipitationOffset - yPosition
                                            ).pixels()
                                        )
                                    }
                                }
                        }

                        inset {
                            for (tick in dayTicks) {
                                val xPosition = timeScale(tick + 12F)
                                val text =
                                    (startDate + DatePeriod(days = (tick / 24).roundToInt())).toString()
                                val layoutResult = textMeasurer.measure(text, dayLabelStyle)
                                drawText(
                                    layoutResult,
                                    topLeft = Offset(
                                        xPosition - layoutResult.size.width / 2,
                                        size.height - layoutResult.size.height
                                    ).pixels(),
                                )
                            }


                            for (tick in hourTicks) {
                                val intTick = tick.roundToInt()
                                if (intTick % 2 != 0) continue

                                val xPosition = timeScale(tick + 0.5F)

                                val text = "%02d".format(intTick % 24)
                                val layoutResult = textMeasurer.measure(
                                    text, if (intTick % 6 == 0) {
                                        bigHourTickStyle
                                    } else {
                                        smallHourTickStyle
                                    }
                                )
                                drawText(
                                    layoutResult,
                                    topLeft = Offset(
                                        xPosition - layoutResult.size.width,
                                        size.height - layoutResult.size.height - 30f * density
                                    ).pixels(),
                                )
                            }
                        }
                    }

                    translate(left = scrollPosition) {
                        drawRect(
                            brush = Brush.horizontalGradient(
                                0.4f to background,
                                1f to Color.Transparent,
                                endX = 30f * density
                            ),
                            topLeft = Offset(x = -30f * density, y = -15f * density).pixels(),
                            size = Size(
                                width = 60f * density,
                                height = size.height + 30f * density
                            ).pixels()
                        )

                        for (temperature in temperatureTicks) {
                            val xPosition = 0f
                            val yPosition = temperatureScale(temperature)
                            val tempText = temperature.roundToInt().toString() + "°C"
                            val tempLayoutResult =
                                textMeasurer.measure(tempText, smallHourTickStyle)
                            drawText(
                                tempLayoutResult,
                                topLeft = Offset(
                                    xPosition - tempLayoutResult.size.width / 2,
                                    yPosition - tempLayoutResult.size.height / 2,
                                ).pixels(),
                            )
                        }

                        for (precipitation in precipitationTicks) {
                            val xPosition = 0f
                            val yPosition = precipitationScale(precipitation)
                            val tempText = precipitation.roundToInt().toString() + "mm"
                            val tempLayoutResult =
                                textMeasurer.measure(tempText, smallHourTickStyle)
                            drawText(
                                tempLayoutResult,
                                topLeft = Offset(
                                    xPosition - tempLayoutResult.size.width / 2,
                                    yPosition - tempLayoutResult.size.height / 2,
                                ).pixels(),
                            )
                        }
                    }

                }
            }
    )
}
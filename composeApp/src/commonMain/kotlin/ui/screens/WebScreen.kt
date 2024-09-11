package ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import compose.icons.WeatherIcons
import compose.icons.weathericons.DayStormShowers
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.util.Identity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.float
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

val format = Json { ignoreUnknownKeys = true }

@Serializable
data class ApiResponse(
    val latitude: Float,
    val longitude: Float,
    val timezone: String,
    val elevation: Float,
    @SerialName("generationtime_ms") val generationTimeMs: Float,
    @SerialName("utc_offset_seconds") val utcOffsetSeconds: Float,
    @SerialName("timezone_abbreviation") val timezoneAbbreviation: String,
    @SerialName("hourly_units") val hourlyUnits: Map<String, String>,
    val hourly: Map<String, List<JsonPrimitive>>
)

suspend fun fetch(): ApiResponse {
    val response =
        HttpClient().get("https://api.open-meteo.com/v1/forecast?latitude=52.52&longitude=13.41&hourly=temperature_2m")
    val json = format.decodeFromString<ApiResponse>(response.bodyAsText())
    return json
}


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

@Composable
fun WebScreen() {
    var content: ApiResponse? by remember { mutableStateOf(null) }



    LaunchedEffect(Unit) {
        content = withContext(Dispatchers.IO) {
            fetch()
        }
    }

    val field = "temperature_2m"

    if (content != null) {

        val textMeasurer = rememberTextMeasurer()

        val values = remember(content, field) {
            content?.hourly?.get(field)?.map { it.float } ?: emptyList()
        }

        val count = remember(values) { values.size }
        val typography = MaterialTheme.typography
        val gridColor = MaterialTheme.colorScheme.tertiaryContainer
        val lineColor = MaterialTheme.colorScheme.tertiary
        val textColor = MaterialTheme.colorScheme.onTertiaryContainer

        Column(modifier = Modifier.fillMaxSize()) {
            Icon(WeatherIcons.DayStormShowers, null)
            Spacer(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
                    .height(300.dp)
                    .drawWithCache {
                        val xStep = 24F
                        val xScale = Scale(0F..count.toFloat(), 0F..size.width)//.nice(xStep)
                        val xTicks = xScale.ticks(xStep)

                        val yStep = 5F
                        val yScale = Scale(
                            values.min()..values.max(), 0F..size.height,
                            mapRange = { range.endInclusive - it }
                        ).nice(yStep)
                        val yTicks = yScale.ticks(yStep)

//                        val yTicksPositions = yTicks.map { yScale(it) }

                        val rescaled = values.mapIndexed { index, value ->
                            xScale(index.toFloat()) to yScale(value)
                        }

                        println(xScale)
                        println(values)
                        println(rescaled)
                        println(size.center)

                        val path = Path()
                        rescaled.firstOrNull()?.let {
                            path.moveTo(it.first, it.second)
                        }
                        for (point in rescaled) {
                            path.lineTo(point.first, point.second)
                        }
//                    path.lineTo(0F, 0F)
//                    path.lineTo(size.width, size.height)
//                    path.lineTo(0F, size.height)
//                    path.addRect(
//                        size.toRect().deflate(4F)
//                            .translate(size.width / 2F, size.height / 2F)
//                    )
                        onDrawBehind {
//                            drawPoints(
//                                rescaled.map { Offset(it.first, it.second) },
//                                pointMode = PointMode.Polygon,
//                                cap = StrokeCap.Round,
//                                color = Color.Red,
//                                strokeWidth = 2F,
//                            )
//                            drawPoints(
//                                rescaled.map { Offset(it.first, it.second) },
//                                pointMode = PointMode.Points,
//                                cap = StrokeCap.Round,
//                                color = Color.Magenta,
//                                strokeWidth = 10F,
//                            )

//                            for ((point, value) in rescaled.zip(values)) {
//                                drawText(
//                                    textMeasurer = textMeasurer,
//                                    text = value.toString(),
//                                    topLeft = Offset(point.first, point.second),
//                                    style = typography.labelSmall.copy(color = Color.Green)
//                                )
//                            }

                            for (xTick in yTicks) {
                                val xPosition = yScale(xTick)

                                for ((yTick, nextYTick) in xTicks.zipWithNext()) {
                                    val offset = 4F
                                    val yPosition = xScale(yTick)
                                    val nextYPosition = xScale(nextYTick)
                                    drawLine(
                                        gridColor,
                                        Offset(yPosition + offset, xPosition),
                                        Offset(nextYPosition - offset, xPosition),
                                    )

//                                    drawText(
//                                        textMeasurer = textMeasurer,
//                                        text = yTick.toString(),
//                                        topLeft = Offset(0F, yPosition),
//                                        style = typography.labelSmall.copy(color = textColor)
//                                    )
                                }
                            }


//                            drawPath(path, color = lineColor, style = Stroke(width = 2F))
                            drawPoints(
                                rescaled.map { Offset(it.first, it.second) },
                                pointMode = PointMode.Polygon,
                                cap = StrokeCap.Round,
                                color = lineColor,
                                strokeWidth = 2F,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10F, 20F), 25F)
                            )
                        }
                    }
            )
        }
    }

}

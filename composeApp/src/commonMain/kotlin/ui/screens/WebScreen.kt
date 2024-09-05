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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.float
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
    val range: ClosedFloatingPointRange<Float>
) {
    operator fun invoke(value: Float): Float =
        lerp(
            range.start,
            range.endInclusive,
            (value - domain.start) / (domain.endInclusive - domain.start)
        )

    fun nice(): Scale = copy(
        domain = floor(range.start * 10) / 10
                ..ceil(range.endInclusive * 10) / 10
    )
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
                        val xScale = Scale(0F..count.toFloat(), 0F..size.width)
                        val yTicks = listOf(-10, 0, 10, 20, 30, 40)
                        val yScale = Scale(
                            min(yTicks.min().toFloat(), values.min())..max(
                                yTicks.max().toFloat(), values.max()
                            ), size.height..0F
                        )

                        val yTicksPositions = yTicks.map { yScale(it.toFloat()) }.toList()

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

                            for ((tick, tickPosition) in yTicks.zip(yTicksPositions)) {
                                drawLine(
                                    gridColor,
                                    Offset(0F, tickPosition),
                                    Offset(size.width, tickPosition)
                                )

                                drawText(
                                    textMeasurer = textMeasurer,
                                    text = tick.toString(),
                                    topLeft = Offset(0F, tickPosition),
                                    style = typography.labelSmall.copy(color = textColor)
                                )
                            }

                            drawPath(path, color = lineColor, style = Stroke(width = 2F))
                        }
                    }
            )
        }
    }

}

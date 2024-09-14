package pl.janzak.weather.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.skydoves.sandwich.getOrThrow
import compose.icons.WeatherIcons
import compose.icons.weathericons.Barometer
import compose.icons.weathericons.Cloud
import compose.icons.weathericons.Humidity
import compose.icons.weathericons.Thermometer
import compose.icons.weathericons.WindDeg
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.format.DayOfWeekNames
import org.jetbrains.compose.resources.stringResource
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import pl.janzak.weather.data.api.HourlyForecast
import pl.janzak.weather.data.api.OpenMeteoWeatherApi
import pl.janzak.weather.database.Database
import pl.janzak.weather.database.Favorites
import pl.janzak.weather.model.CurrentWeather
import pl.janzak.weather.model.DayWeather
import pl.janzak.weather.model.LocationInfo
import pl.janzak.weather.ui.util.currentWeatherResponseToModel
import pl.janzak.weather.ui.util.dailyForecastResponseToModel
import resources.Res
import resources.counter_edit_button
import resources.label_apparent_temperature
import resources.weekday_abbreviation_1
import resources.weekday_abbreviation_2
import resources.weekday_abbreviation_3
import resources.weekday_abbreviation_4
import resources.weekday_abbreviation_5
import resources.weekday_abbreviation_6
import resources.weekday_abbreviation_7
import ui.components.WeatherIcon
import ui.screens.Scale
import ui.util.LocalUnits
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsView(
    goBack: () -> Unit,
    locationInfo: LocationInfo,
    isFavorite: Boolean,
    setFavorite: (Boolean) -> Unit,
    data: DetailsScreenData?,
) {

    Scaffold(topBar = {
        CenterAlignedTopAppBar(
            title = {
                Column(
                    verticalArrangement = Arrangement.SpaceAround,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        locationInfo.name.name,
                        style = MaterialTheme.typography.titleLarge.copy(lineHeight = 1.em)
                    )
                    Text(
                        locationInfo.name.region,
                        style = MaterialTheme.typography.titleSmall.copy(lineHeight = 1.em),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.alpha(0.7f)
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = {
                    goBack()
                }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        "Go back"
                    )
                }
            },
            actions = {
                IconButton({ setFavorite(!isFavorite) }) {
                    Icon(
                        if (isFavorite) {
                            Icons.Default.Star
                        } else {
                            Icons.Default.StarOutline
                        },
                        stringResource(Res.string.counter_edit_button)
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                navigationIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                actionIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        )
    }) { padding ->
        if (data == null) {
            Box(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            CurrentWeather(data)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (model in data.hourly.keys) {
                    FilterChip(
                        selected = false,
                        onClick = {},
                        label = { Text(model.modelName) },
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                }
            }


            val scrollState = rememberScrollState()
            var scrollbarTrackWidth by remember { mutableStateOf<Int?>(null) }
            val ratio = remember(scrollState, scrollState.viewportSize, scrollState.maxValue) {
                if (scrollbarTrackWidth != null) {
                    scrollbarTrackWidth!!.toFloat() / (scrollState.viewportSize + scrollState.maxValue)
                } else {
                    1f
                }
            }
            val boxWidth = remember(ratio, scrollState) {
                (scrollState.viewportSize * ratio).roundToInt()
            }

            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .height(IntrinsicSize.Min)
                    .width(IntrinsicSize.Min),
                contentAlignment = Alignment.CenterStart,
            ) {

                Box(
                    modifier = Modifier
                        .offset { IntOffset((scrollState.value * ratio).roundToInt(), 0) }
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .width(LocalDensity.current.run { boxWidth.toDp() })
                        .fillMaxHeight()
                        .onGloballyPositioned { layoutCoordinates ->
                            scrollbarTrackWidth =
                                layoutCoordinates.parentLayoutCoordinates?.size?.width
                        }
                )
                Row(
                    modifier = Modifier.draggable(
                        orientation = Orientation.Horizontal,
                        state = rememberDraggableState { delta ->
                            runBlocking { scrollState.scrollBy(delta / ratio) }
                        }),
                    horizontalArrangement = Arrangement.Center
                ) {
                    val weekdayNames = DayOfWeekNames(
                        stringResource(Res.string.weekday_abbreviation_1),
                        stringResource(Res.string.weekday_abbreviation_2),
                        stringResource(Res.string.weekday_abbreviation_3),
                        stringResource(Res.string.weekday_abbreviation_4),
                        stringResource(Res.string.weekday_abbreviation_5),
                        stringResource(Res.string.weekday_abbreviation_6),
                        stringResource(Res.string.weekday_abbreviation_7),
                    )

                    data.daily.mapIndexed { _, day ->
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(8.dp).weight(1f)
                        ) {
                            Text(
                                "%d".format(day.date.dayOfMonth),
                                style = MaterialTheme.typography.labelLarge
                            )
                            Text(
                                day.date.format(LocalDate.Format { dayOfWeek(weekdayNames) }),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.alpha(0.7f)
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                contentAlignment = Alignment.Center,
            ) {

                for ((_, values) in data.hourly) {
                    Plot(values.temperature2m)
                }

            }
        }
    }

}

@Composable
fun CurrentWeather(data: DetailsScreenData) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(
            16.dp,
            alignment = Alignment.CenterHorizontally
        ),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(top = 8.dp, bottom = 4.dp, start = 16.dp, end = 16.dp).widthIn(400.dp)
    ) {

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(WeatherIcons.Thermometer, null)
            Text(
                LocalUnits.current.formatTemperature(data.current.temperature),
                style = MaterialTheme.typography.headlineLarge
            )
        }
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.alpha(0.7f)
        ) {
            Text(
                LocalUnits.current.formatTemperature(data.current.apparentTemperature),
                style = MaterialTheme.typography.titleMedium.copy(lineHeight = 1.em),

                )
            Text(
                stringResource(Res.string.label_apparent_temperature),
                style = MaterialTheme.typography.labelSmall.copy(lineHeight = 1.em),
            )
        }
        Spacer(Modifier.weight(1f))
        WeatherIcon(
            data.current.weatherCode,
            data.current.isDay,
            modifier = Modifier.padding(4.dp).size(54.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(
            16.dp,
            alignment = Alignment.CenterHorizontally
        ),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                WeatherIcons.WindDeg,
                null,
                modifier = Modifier.size(24.dp)
                    .rotate(data.current.windDirection.toFloat())
                    .padding(horizontal = 2.dp)
            )
            Text(
                LocalUnits.current.formatWindSpeed(data.current.windSpeed),
                style = MaterialTheme.typography.labelMedium
            )
        }
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                WeatherIcons.Barometer,
                null,
                modifier = Modifier.size(24.dp)
            )
            Text(
                LocalUnits.current.formatPressure(data.current.surfacePressure),
                style = MaterialTheme.typography.labelMedium
            )
        }
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                WeatherIcons.Cloud,
                null,
                modifier = Modifier.size(24.dp)
            )
            Text(
                LocalUnits.current.formatPercentage(data.current.cloudCover),
                style = MaterialTheme.typography.labelMedium
            )
        }
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                WeatherIcons.Humidity,
                null,
                modifier = Modifier.size(24.dp)
            )
            Text(
                LocalUnits.current.formatPercentage(data.current.relativeHumidity),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

private val logger = KotlinLogging.logger {}

@Composable
fun Plot(yValues: DoubleArray, modifier: Modifier = Modifier) {
    val typography = MaterialTheme.typography
    val gridColor = MaterialTheme.colorScheme.tertiaryContainer
    val lineColor = MaterialTheme.colorScheme.tertiary
    val textColor = MaterialTheme.colorScheme.onTertiaryContainer
    val textMeasurer = rememberTextMeasurer(cacheSize = 100)

    Spacer(
        modifier = Modifier
            .padding(20.dp)
            .requiredHeight(120.dp * 4)
            .requiredWidth(240.dp * 7)
            .then(modifier)

            .drawWithCache {
                val xStep = 24F
                val xScale = Scale(0F..yValues.size.toFloat(), 0F..size.width)//.nice(xStep)
                val xTicks = xScale.ticks(xStep)

                val yStep = 5F
                val yScale = Scale(
                    -20F..40F, 0F..size.height,
                    mapRange = { range.endInclusive - it }
                ).nice(yStep)
                val yTicks = yScale.ticks(yStep)

                val rescaled = yValues.mapIndexed { index, value ->
                    xScale(index.toFloat()) to yScale(value.toFloat())
                }

                val path = Path()
                rescaled.firstOrNull()?.let {
                    path.moveTo(it.first, it.second)
                }
                for (point in rescaled) {
                    path.lineTo(point.first, point.second)
                }

                onDrawBehind {
                    for (yTick in yTicks) {
                        val yPosition = yScale(yTick)

                        for ((xTick, nextXTick) in xTicks.zipWithNext()) {
                            val offset = 4F
                            val xPosition = xScale(xTick)
                            val nextXPosition = xScale(nextXTick)
                            drawLine(
                                gridColor,
                                Offset(xPosition + offset, yPosition),
                                Offset(nextXPosition - offset, yPosition),
                            )

                            drawText(
                                textMeasurer = textMeasurer,
                                text = yTick.toString(),
                                topLeft = Offset(xPosition, yPosition),
                                style = typography.labelSmall.copy(color = textColor)
                            )
                        }
                    }


//                            drawPath(path, color = lineColor, style = Stroke(width = 2F))
                    drawPoints(
                        rescaled.map { Offset(it.first, it.second) },
                        pointMode = PointMode.Polygon,
                        cap = StrokeCap.Round,
                        color = lineColor,
                        strokeWidth = 2F,
//                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10F, 20F), 25F)
                    )
                }
            }
    )
}

enum class Model(val modelName: String, val key: String) {
    Hybrid("Hybrid", "best_match"),
    Icon("ICON", "icon_seamless"),
    Gfs("GFS", "gfs_seamless"),
    Ecmwf("ECMWF", "ecmwf_ifs025"),
}

data class DetailsScreenData(
    val current: CurrentWeather,
    val daily: List<DayWeather>,
    val hourly: Map<Model, HourlyForecast>,
)

class DetailsScreenViewModel(private val locationInfo: LocationInfo) : ViewModel(), KoinComponent {
    private val _db: Database by inject()
    private val _weatherApi: OpenMeteoWeatherApi by inject()

    val isFavorite =
        _db.favoritesQueries.loadFavorites(locationInfo.coordinates.toString()).asFlow()
            .mapToOneOrNull(Dispatchers.IO).map { it != null }

    private val _data = MutableStateFlow<DetailsScreenData?>(null)
    val data = _data.asStateFlow()

    private val _selectedModels = MutableStateFlow<Set<String>>(emptySet())

    init {
        fetchData()
    }

    fun setFavorite(state: Boolean) {
        if (state) {
            _db.favoritesQueries.insertFavorites(
                Favorites(
                    locationInfo.coordinates.toString(),
                    locationInfo.name.name,
                    locationInfo.name.region,
                )
            )
        } else {
            _db.favoritesQueries.deleteFavorites(locationInfo.coordinates.toString())
        }
    }

    fun fetchData() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val coordinates = locationInfo.coordinates
                val current = async {
                    _weatherApi.currentWeather(coordinates).getOrThrow().let { response ->
                        currentWeatherResponseToModel(response, coordinates)
                    }
                }
                val daily = async {
                    _weatherApi.dailyForecast(coordinates).getOrThrow().let { response ->
                        dailyForecastResponseToModel(response)
                    }
                }

                val models = Model.entries.map {
                    async {
                        it to _weatherApi.hourlyForecast(coordinates, it.key).getOrThrow().hourly
                    }
                }

                _data.emit(
                    DetailsScreenData(
                        current.await(),
                        daily.await(),
                        models.awaitAll().toMap()
                    )
                )
            }
        }
    }
}

@Composable
fun DetailsScreen(
    goBack: () -> Unit,
    locationInfo: LocationInfo,
    viewModel: DetailsScreenViewModel = viewModel { DetailsScreenViewModel(locationInfo) }
) {

    val isFavorite = viewModel.isFavorite.collectAsStateWithLifecycle(false)
    val data = viewModel.data.collectAsStateWithLifecycle()

    DetailsView(
        goBack = goBack,
        locationInfo = locationInfo,
        isFavorite = isFavorite.value,
        setFavorite = viewModel::setFavorite,
        data = data.value,
    )
}


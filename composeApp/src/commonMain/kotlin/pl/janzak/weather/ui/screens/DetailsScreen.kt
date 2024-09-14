package pl.janzak.weather.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material.icons.filled.StackedLineChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.graphics.drawscope.translate
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
import com.materialkolor.ktx.lighten
import com.materialkolor.ktx.toColor
import com.materialkolor.ktx.toHct
import com.skydoves.sandwich.getOrThrow
import compose.icons.WeatherIcons
import compose.icons.weathericons.Barometer
import compose.icons.weathericons.Cloud
import compose.icons.weathericons.Humidity
import compose.icons.weathericons.Thermometer
import compose.icons.weathericons.WindDeg
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.plus
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
import pl.janzak.weather.ui.util.Scale
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
import ui.util.LocalUnits
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsView(
    goBack: () -> Unit,
    locationInfo: LocationInfo,
    isFavorite: Boolean,
    setFavorite: (Boolean) -> Unit,
    data: DetailsScreenData?,
    state: DetailsScreenState,
    selectModel: (Model, Boolean) -> Unit,
    setAggregate: (Boolean) -> Unit,
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
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            val hues = listOf(45.0, 135.0, 225.0, 315.0)
            val modelLineColors = hues.map {
                if (isSystemInDarkTheme()) {
                    MaterialTheme.colorScheme.errorContainer.toHct().withHue(it).toColor()
                        .lighten(2f)
                } else {
                    MaterialTheme.colorScheme.error.toHct().withHue(it).toColor().lighten(1.8f)
                }
            }
            val modelTextColors = hues.map {
                MaterialTheme.colorScheme.onErrorContainer.toHct().withHue(it).toColor()
            }
            val modelBackgroundColors = hues.map {
                MaterialTheme.colorScheme.errorContainer.toHct().withHue(it).toColor()
            }

            CurrentWeather(data)

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(8.dp))
                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    data.hourly.keys.mapIndexed { index, model ->
                        val selected = state.selectedModels.contains(model)
                        FilterChip(
                            selected = selected,
                            onClick = { selectModel(model, !selected) },
                            label = { Text(model.modelName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = modelBackgroundColors[index],
                                selectedLabelColor = modelTextColors[index]
                            ),
                            modifier = Modifier.padding(horizontal = 2.dp)

                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconToggleButton(
                    checked = !state.aggregate,
                    onCheckedChange = { setAggregate(!state.aggregate) },
                    content = { Icon(Icons.Default.StackedLineChart, null) },
                    colors = IconButtonDefaults.outlinedIconToggleButtonColors(),
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))

            val scrollState = rememberScrollState()
            var scrollbarTrackWidth by remember { mutableStateOf<Int?>(null) }
            val ratio = if (scrollbarTrackWidth != null) {
                scrollbarTrackWidth!!.toFloat() / (scrollState.viewportSize + scrollState.maxValue)
            } else {
                1f
            }

            val boxWidth = (scrollState.viewportSize * ratio).roundToInt()

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                contentAlignment = Alignment.Center,
            ) {

                Plot(
                    data.hourly.values.map { it.temperature2m },
                    data.hourly.entries.map { state.selectedModels.contains(it.key) },
                    startDate = data.current.time.date,
                    scrollPosition = scrollState.value.toFloat(),
                    colors = modelLineColors,
                    aggregate = state.aggregate,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

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
            .padding(top = 8.dp, bottom = 4.dp, start = 16.dp, end = 16.dp).widthIn(max = 400.dp).fillMaxWidth()
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

@Composable
fun Plot(
    temperatures: List<DoubleArray>,
    visibilities: List<Boolean>,
    startDate: LocalDate,
    scrollPosition: Float,
    colors: List<Color>,
    aggregate: Boolean,
    modifier: Modifier = Modifier
) {
    val background = MaterialTheme.colorScheme.background
    val typography = MaterialTheme.typography
    val gridColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val lineColor = MaterialTheme.colorScheme.tertiary
    val textColor = MaterialTheme.colorScheme.onTertiaryContainer
    val textMeasurer = rememberTextMeasurer(cacheSize = 200)
    val minTemperature = remember(temperatures) { temperatures.minOf { it.min() } }.toFloat()
    val maxTemperature = remember(temperatures) { temperatures.maxOf { it.max() } }.toFloat()

    val dayLabelStyle = typography.labelLarge.copy(color = MaterialTheme.colorScheme.onSurface)
    val bigHourTickStyle = typography.labelMedium.copy(color = textColor)
    val smallHourTickStyle = typography.labelSmall.copy(color = textColor.copy(alpha = 0.5f))

    val density = LocalDensity.current.density

    Spacer(
        modifier = Modifier
            .padding(20.dp)
            .requiredHeight(120.dp * 2)
            .requiredWidth(240.dp * 7)
            .then(modifier)

            .drawWithCache {
                val timeScale = Scale(0F..temperatures.first().size.toFloat(), 0F..size.width)
                val dayTicks = timeScale.ticks(24F)
                val hourTicks = timeScale.ticks(1F)
                val bottomPlotOffset = 60F * density
                val yStep = 2F

                val temperatureScale = Scale(
                    minTemperature..maxTemperature, 0F..size.height - bottomPlotOffset,
                    mapRange = { range.endInclusive - it }
                ).nice(yStep)
                val temperatureTicks = temperatureScale.ticks(yStep)

                onDrawBehind {
                    inset(left = 30f * density, right = 0f, top = 0f, bottom = 0f) {
                        for (tick in temperatureTicks) {
                            val yPosition = temperatureScale(tick)
                            drawLine(
                                color = gridColor,
                                start = Offset(0f, yPosition),
                                Offset(this.size.width, yPosition)
                            )
                        }

                        for (tick in hourTicks.dropLast(1)) {
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
                                    start = Offset(xPosition - strokeWidth / 4, -1f * density),
                                    end = Offset(xPosition - strokeWidth / 4, this.size.height),
                                    strokeWidth = strokeWidth
                                )
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


                            drawPath(upperLine, color = lineColor.copy(alpha = 0.2f))
                            drawPoints(
                                mean.mapIndexed { hour, value ->
                                    Offset(
                                        timeScale(hour.toFloat()),
                                        temperatureScale(value.toFloat())
                                    )
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
                                                )
                                            },
                                            pointMode = PointMode.Polygon,
                                            cap = StrokeCap.Round,
                                            color = colors[index],
                                            strokeWidth = 2F * density,
                                        )
                                    }
                                }
                        }

                        inset {
                            for (tick in dayTicks.dropLast(1)) {
                                val xPosition = timeScale(tick + 12F)
                                val text =
                                    (startDate + DatePeriod(days = (tick / 24).roundToInt())).toString()
                                val layoutResult = textMeasurer.measure(text, dayLabelStyle)
                                drawText(
                                    layoutResult,
                                    topLeft = Offset(
                                        xPosition - layoutResult.size.width / 2,
                                        size.height - layoutResult.size.height
                                    ),
                                )
                            }


                            for (tick in hourTicks.dropLast(1)) {
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
                                    ),
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
                            topLeft = Offset(x = -30f * density, y = -15f * density),
                            size = Size(width = 60f * density, height = size.height + 30f * density)
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
                                ),
                            )
                        }
                    }

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

data class DetailsScreenState(
    val selectedModels: Set<Model> = setOf(Model.Hybrid, Model.Icon, Model.Gfs, Model.Ecmwf),
    val aggregate: Boolean = true,
)

class DetailsScreenViewModel(private val locationInfo: LocationInfo) : ViewModel(), KoinComponent {
    private val _db: Database by inject()
    private val _weatherApi: OpenMeteoWeatherApi by inject()

    val isFavorite =
        _db.favoritesQueries.loadFavorites(locationInfo.coordinates.toString()).asFlow()
            .mapToOneOrNull(Dispatchers.IO).map { it != null }

    private val _data = MutableStateFlow<DetailsScreenData?>(null)
    val data = _data.asStateFlow()

    private val _state = MutableStateFlow(DetailsScreenState())
    val state = _state.asStateFlow()

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

    fun setAggregate(state: Boolean) {
        _state.update { it.copy(aggregate = state) }
    }

    fun selectModel(model: Model, state: Boolean) {
        _state.update {
            it.copy(
                selectedModels = if (state) {
                    it.selectedModels.plusElement(model)
                } else if (it.selectedModels.contains(model) && (it.selectedModels.size == 1)) {
                    it.selectedModels
                } else {
                    it.selectedModels.minusElement(model)
                }
            )
        }
    }

    private fun fetchData() {
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
    val state = viewModel.state.collectAsStateWithLifecycle()

    DetailsView(
        goBack = goBack,
        locationInfo = locationInfo,
        isFavorite = isFavorite.value,
        setFavorite = viewModel::setFavorite,
        data = data.value,
        state = state.value,
        selectModel = viewModel::selectModel,
        setAggregate = viewModel::setAggregate,
    )
}


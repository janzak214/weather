@file:OptIn(ExperimentalSharedTransitionApi::class)

package pl.janzak.weather.ui.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconToggleButton
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.materialkolor.ktx.lighten
import com.materialkolor.ktx.toColor
import com.materialkolor.ktx.toHct
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.format.DayOfWeekNames
import org.jetbrains.compose.resources.stringArrayResource
import pl.janzak.weather.model.LocationInfo
import pl.janzak.weather.model.WeatherCode
import pl.janzak.weather.ui.screens.DetailsScreenData
import pl.janzak.weather.ui.screens.DetailsScreenState
import pl.janzak.weather.ui.screens.Model
import pl.janzak.weather.ui.util.SharedElementKey
import pl.janzak.weather.ui.util.SharedElementType
import pl.janzak.weather.ui.util.containerTransform
import resources.Res
import resources.weekday_abbreviations
import pl.janzak.weather.ui.theme.Easing
import kotlin.math.roundToInt

context(AnimatedVisibilityScope, SharedTransitionScope)
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

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(
                        verticalArrangement = Arrangement.SpaceAround,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        val location = locationInfo.name
                        val key = SharedElementKey(
                            location,
                            SharedElementType.LocationInfo
                        )
                        Text(
                            location.name,
                            style = MaterialTheme.typography.titleLarge.copy(lineHeight = 1.em),
                            modifier = Modifier.containerTransform(key.sub(1))
                        )
                        Text(
                            location.region,
                            style = MaterialTheme.typography.titleSmall.copy(lineHeight = 1.em),
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.alpha(0.7f).containerTransform(key.sub(2))
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        goBack()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            null
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
                            null
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
                modifier = Modifier.containerTransform(
                    SharedElementKey(
                        locationInfo.name,
                        SharedElementType.AppBar
                    )
                )
            )
        },
        modifier = Modifier.containerTransform(
            SharedElementKey(
                locationInfo.name,
                SharedElementType.Card
            )
        )
    ) { padding ->
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
                .verticalScroll(rememberScrollState())
                .skipToLookaheadSize()
                .animateEnterExit(),
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

            CurrentWeather(data.current, locationInfo.name)

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateEnterExit(
                        enter = fadeIn(Easing.enterTween()),
                        exit = fadeOut(Easing.exitTween())
                    ),
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
                OutlinedIconToggleButton(
                    checked = !state.aggregate,
                    onCheckedChange = { setAggregate(!state.aggregate) },
                    content = { Icon(Icons.Default.StackedLineChart, null) },
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

                WeatherPlot(
                    data.hourly.values.map { it.temperature2m },
                    data.hourly.entries.map { state.selectedModels.contains(it.key) },
                    data.hourly[Model.Hybrid]!!.weatherCode.map { WeatherCode.get(it.roundToInt())!! },
                    data.hourly.values.map { it.precipitation },
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
                    .width(IntrinsicSize.Min)
                    .animateEnterExit(
                        enter = fadeIn(Easing.enterTween()),
                        exit = fadeOut(Easing.exitTween()),
                    ),
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
                        stringArrayResource(Res.array.weekday_abbreviations)
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
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

}
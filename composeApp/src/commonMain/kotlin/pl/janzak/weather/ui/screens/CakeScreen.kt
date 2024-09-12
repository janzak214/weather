package ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skydoves.sandwich.message
import com.skydoves.sandwich.onError
import com.skydoves.sandwich.onFailure
import com.skydoves.sandwich.onSuccess
import dev.jordond.compass.geolocation.Geolocator
import dev.jordond.compass.geolocation.GeolocatorResult
import dev.jordond.compass.geolocation.Locator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import model.CurrentWeather
import model.DayWeather
import pl.janzak.weather.model.LocationName
import model.WeatherCode
import org.koin.compose.koinInject
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.impl.extensions.get
import pl.janzak.weather.data.api.Coordinates
import pl.janzak.weather.data.api.GeocodingEntry
import pl.janzak.weather.data.api.NominatimApi
import pl.janzak.weather.data.api.OpenMeteoGeocodingApi
import pl.janzak.weather.data.api.OpenMeteoWeatherApi
import pl.janzak.weather.data.store.WeatherStoreProvider
import ui.components.WeatherOverviewCard


@Serializable
sealed class LocationResult {
    @Serializable
    @SerialName("success")
    data class Success(
        val result: Coordinates
    ) : LocationResult()

    @Serializable
    @SerialName("error")
    data class Error(
        val message: String
    ) : LocationResult()
}


expect fun getLocator(): Locator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CakeScreen(
    modifier: Modifier = Modifier,
    nominatimApi: NominatimApi = koinInject(),
    openMeteoGeocodingApi: OpenMeteoGeocodingApi = koinInject(),
    openMeteoWeatherApi: OpenMeteoWeatherApi = koinInject(),
    weatherStoreProvider: WeatherStoreProvider = koinInject()
) {
    var location: Coordinates? by remember { mutableStateOf(null) }

    var text by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var entries: List<GeocodingEntry> by remember { mutableStateOf(emptyList()) }
    var searchingForLocation by remember { mutableStateOf(false) }

    val geolocator = remember {
        val locator = getLocator()
        Geolocator(locator)
    }

    val language = Locale.current.toLanguageTag()

    LaunchedEffect(text) {
        withContext(Dispatchers.IO) {
            if (text.isNotEmpty() && expanded) {
                println(language)
                openMeteoGeocodingApi.geocode(text, language = language)
                    .onSuccess { entries = data.results ?: emptyList() }
            }
        }
    }

    val weatherStore = weatherStoreProvider.current()

    LaunchedEffect(location) {
        location?.let {
            println(openMeteoWeatherApi.currentWeather(it))
            println(openMeteoWeatherApi.dailyForecast(it))
            println(openMeteoWeatherApi.hourlyForecast(it))
            println(weatherStore.get(it))
        }
    }

    val coroutineScope = rememberCoroutineScope()

    val getPosition = remember {
        {
            coroutineScope.launch {
                withContext(Dispatchers.IO) {
                    searchingForLocation = true
                    when (val locationResponse = geolocator.current()) {
                        is GeolocatorResult.Error -> Unit
                        is GeolocatorResult.Success -> {
                            location = Coordinates(
                                locationResponse.data.coordinates.latitude,
                                locationResponse.data.coordinates.longitude
                            )
                            nominatimApi.reverseGeocode(location!!, language = language)
                                .onSuccess { text = data.name }
                                .onError { println(payload) }
                                .onFailure { println(message()) }
                        }
                    }
                    searchingForLocation = false
                }
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        SearchBar(
            modifier = Modifier.align(Alignment.TopCenter),
            inputField = {
                SearchBarDefaults.InputField(
                    query = text,
                    onQueryChange = { text = it },
                    onSearch = { expanded = false },
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    placeholder = { Text("Search") },
                    leadingIcon = {
                        if (expanded) {
                            IconButton(onClick = {
                                text = ""
                                expanded = false
                            }) {
                                Icon(
                                    Icons.AutoMirrored.Default.ArrowBack,
                                    contentDescription = "Go back"
                                )
                            }
                        } else {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                    },
                    trailingIcon = {
                        if (expanded) {
                            if (text.isNotEmpty()) {
                                IconButton(onClick = {
                                    text = ""
                                }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Close"
                                    )
                                }
                            }
                        } else {
                            var buttonOn by remember { mutableStateOf(false) }
                            val color by animateColorAsState(
                                targetValue = if (buttonOn) {
                                    MaterialTheme.colorScheme.tertiary
                                } else {
                                    IconButtonDefaults.iconButtonColors().contentColor
                                },
                                animationSpec = tween(400, easing = LinearEasing),
                                finishedListener = {
                                    if (searchingForLocation || buttonOn)
                                        buttonOn = !buttonOn
                                }
                            )
                            IconButton(onClick = {
                                buttonOn = true
                                getPosition()
                            }, colors = IconButtonDefaults.iconButtonColors(contentColor = color)) {
                                Icon(
                                    Icons.Default.MyLocation,
                                    contentDescription = "My location",
//                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            }
                        }
                    },
                )
            },
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                for (entry in entries) {

                    val resultText = entry.name
                    ListItem(
                        headlineContent = { Text(resultText) },
                        supportingContent = { Text(entry.country) },
                        leadingContent = {
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = null
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable {
                            text = resultText
                            location =
                                Coordinates(latitude = entry.latitude, longitude = entry.longitude)
                            expanded = false
                        }.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
        }


//        Column(

//        ) {

//
//            repeat(40) {
//
//            }
//        }
//

        val cw = location?.let { weatherStore.stream(StoreReadRequest.cached(it, true)) }
        val x = cw?.map { println(it); it }?.collectAsStateWithLifecycle(null)

        LazyColumn(
            modifier = Modifier.semantics { traversalIndex = 1f }
                .padding(16.dp)
//                .verticalScroll(
//                    rememberScrollState()
//                )
                .fillMaxSize(),
            userScrollEnabled = true,
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                Spacer(modifier = Modifier.requiredHeight(72.dp))
            }
            if (location != null) {
                item {
                    Text(
                        "${location!!.latitude} ${location!!.longitude}"
                    )
                }
            }

            x?.value?.dataOrNull()?.let { w->

            items(40) {
                    WeatherOverviewCard(
                        locationm,
                        w,
                        forecast,
                        goToDetails = {},
                        modifier = Modifier.padding(vertical = 4.dp).width(IntrinsicSize.Min)
                            .widthIn(388.dp)
                    )
                }
            }
        }


    }
}


val locationm = LocationName(
    name = "Bronowice Małe",
    region = "Kraków, Polska"
)

val currentWeather = CurrentWeather(
    coordinates = Coordinates(0.0, 0.0),
    time = LocalDateTime(2024, 9, 11, 14, 0, 0),
    temperature = 29.42,
    apparentTemperature = 20.24,
    relativeHumidity = 42.0,
    isDay = true,
    surfacePressure = 982.4,
    windSpeed = 6.1,
    windDirection = 17.0,
    cloudCover = 80.0,
    weatherCode = WeatherCode.RAIN_SLIGHT
)


val forecast = listOf(
    DayWeather(
        date = LocalDate(2024, 9, 11),
        temperatureMax = 30.5,
        temperatureMin = 16.5,
        precipitation = 20.0,
        precipitationProbability = 90.0,
        weatherCode = WeatherCode.RAIN_SLIGHT
    ),
    DayWeather(
        date = LocalDate(2024, 9, 12),
        temperatureMax = 30.5,
        temperatureMin = 16.5,
        precipitation = 20.0,
        precipitationProbability = 90.0,
        weatherCode = WeatherCode.FOG
    ),
    DayWeather(
        date = LocalDate(2024, 9, 13),
        temperatureMax = 30.5,
        temperatureMin = 16.5,
        precipitation = 20.0,
        precipitationProbability = 90.0,
        weatherCode = WeatherCode.DRIZZLE_MODERATE
    ),
    DayWeather(
        date = LocalDate(2024, 9, 14),
        temperatureMax = 30.5,
        temperatureMin = 16.5,
        precipitation = 20.0,
        precipitationProbability = 90.0,
        weatherCode = WeatherCode.SNOW_FALL_MODERATE
    ),
    DayWeather(
        date = LocalDate(2024, 9, 15),
        temperatureMax = 30.5,
        temperatureMin = 16.5,
        precipitation = 20.0,
        precipitationProbability = 90.0,
        weatherCode = WeatherCode.SNOW_FALL_MODERATE
    ),
    DayWeather(
        date = LocalDate(2024, 9, 16),
        temperatureMax = 30.5,
        temperatureMin = 16.5,
        precipitation = 20.0,
        precipitationProbability = 90.0,
        weatherCode = WeatherCode.SNOW_FALL_MODERATE
    ),
    DayWeather(
        date = LocalDate(2024, 9, 17),
        temperatureMax = 30.5,
        temperatureMin = 16.5,
        precipitation = 20.0,
        precipitationProbability = 90.0,
        weatherCode = WeatherCode.SNOW_FALL_MODERATE
    )
)

//@Composable
//fun WeatherOverviewCardPreview() {
//    WeatherOverviewCard(
//        locationn,
//        currentWeather,
//        forecast,
//        goToDetails = {},
//        modifier = Modifier.padding(vertical = 4.dp).width(IntrinsicSize.Min).widthIn(388.dp)
//    )
//}
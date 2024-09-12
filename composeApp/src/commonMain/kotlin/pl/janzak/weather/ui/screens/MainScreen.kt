package pl.janzak.weather.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.materialkolor.ktx.toColor
import com.materialkolor.ktx.toHct
import com.skydoves.sandwich.getOrThrow
import com.skydoves.sandwich.message
import com.skydoves.sandwich.onError
import com.skydoves.sandwich.onFailure
import com.skydoves.sandwich.suspendOnSuccess
import dev.jordond.compass.geolocation.Geolocator
import dev.jordond.compass.geolocation.GeolocatorResult
import dev.jordond.compass.geolocation.Locator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.janzak.weather.model.LocationName
import org.jetbrains.compose.resources.stringResource
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import pl.janzak.weather.model.Coordinates
import pl.janzak.weather.data.api.NominatimApi
import pl.janzak.weather.data.api.OpenMeteoGeocodingApi
import pl.janzak.weather.data.api.OpenMeteoWeatherApi
import pl.janzak.weather.database.Database
import pl.janzak.weather.model.FavoriteLocation
import pl.janzak.weather.model.LocationInfo
import pl.janzak.weather.ui.util.currentWeatherResponseToModel
import pl.janzak.weather.ui.util.dailyForecastResponseToModel
import resources.Res
import resources.search_field_label
import ui.components.WeatherOverviewCard


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Search(
    locations: List<LocationInfo>,
    fetchLocations: (String) -> Unit,
    handleGeolocalization: (done: (LocationInfo) -> Unit) -> Unit,
    openLocation: (LocationInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    var text by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var searchingForLocation by remember { mutableStateOf(false) }

    LaunchedEffect(text) { fetchLocations(text) }

    SearchBar(
        modifier = modifier,
        inputField = {
            SearchBarDefaults.InputField(
                query = text,
                onQueryChange = { text = it },
                onSearch = { expanded = false },
                expanded = expanded,
                onExpandedChange = { expanded = it },
                placeholder = { Text(stringResource(Res.string.search_field_label)) },
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
                                MaterialTheme.colorScheme.tertiary.toHct().withChroma(120.0)
                                    .withTone(60.0).toColor()
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
                            searchingForLocation = true
                            handleGeolocalization { searchingForLocation = false; openLocation(it) }
                        }, colors = IconButtonDefaults.iconButtonColors(contentColor = color)) {
                            Icon(
                                Icons.Default.MyLocation,
                                contentDescription = "My location",
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
            for (entry in locations) {
                ListItem(
                    headlineContent = { Text(entry.name.name) },
                    supportingContent = { Text(entry.name.region) },
                    leadingContent = {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = null
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clickable {
                        expanded = false
                        openLocation(entry)
                    }.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun MainView(
    favorites: List<FavoriteLocation>?,
    locations: List<LocationInfo>,
    fetchLocations: (String) -> Unit,
    handleGeolocalization: (done: (LocationInfo) -> Unit) -> Unit,
    openLocation: (LocationInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).then(modifier)) {
            Search(
                locations,
                fetchLocations,
                handleGeolocalization,
                openLocation,
                modifier = Modifier.align(Alignment.TopCenter),
            )

            if (favorites == null) {
                Box(
                    modifier = Modifier.matchParentSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
                return@Box
            }

            LazyColumn(
                modifier = Modifier.semantics { traversalIndex = 1f }
                    .padding(16.dp)
                    .fillMaxSize(),
                userScrollEnabled = true,
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                item {
                    Spacer(modifier = Modifier.requiredHeight(110.dp))
                }

                items(favorites, key = { it.info.toString() }) {
                    WeatherOverviewCard(
                        it.info.name,
                        it.currentWeather,
                        it.forecast,
                        goToDetails = { openLocation(it.info) },
                        modifier = Modifier.padding(vertical = 8.dp)
                            .width(IntrinsicSize.Min)
                            .widthIn(400.dp)
                    )
                }
            }
        }
    }

}

class MainScreenViewModel : ViewModel(), KoinComponent {
    private val _db: Database by inject()
    private val _weatherApi: OpenMeteoWeatherApi by inject()
    private val _geocodingApi: OpenMeteoGeocodingApi by inject()
    private val _nominatimApi: NominatimApi by inject()
    private val _locator: Locator by inject()

    private val _geolocator = Geolocator(_locator)
    private var _language = "en-US"
    private val _locations = MutableStateFlow(emptyList<LocationInfo>())

    val favorites =
        _db.favoritesQueries.loadAllFavorites().asFlow().mapToList(Dispatchers.IO).map { dbData ->
            withContext(Dispatchers.IO) {
                dbData.map {
                    val coordinates = Coordinates.parse(it.coordinates)
                    val currentDeferred = async { _weatherApi.currentWeather(coordinates) }
                    val dailyDeferred = async { _weatherApi.dailyForecast(coordinates) }


                    async {
                        FavoriteLocation(
                            info = LocationInfo(
                                coordinates = coordinates,
                                name = LocationName(
                                    name = it.name,
                                    region = it.region,
                                ),
                            ),
                            currentWeather = currentDeferred.await().getOrThrow().let { response ->
                                currentWeatherResponseToModel(response, coordinates)
                            },
                            forecast = dailyDeferred.await().getOrThrow().let { response ->
                                dailyForecastResponseToModel(response)
                            }
                        )
                    }
                }.awaitAll()
            }
        }

    val locations = _locations.asStateFlow()

    fun setLanguage(language: String) {
        _language = language
    }

    fun fetchLocations(query: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _geocodingApi.geocode(query, language = _language)
                    .suspendOnSuccess {
                        _locations.emit(this.data.results?.map {
                            LocationInfo(
                                coordinates = Coordinates(it.latitude, it.longitude),
                                name = LocationName(
                                    name = it.name,
                                    region = it.country,
                                )
                            )
                        } ?: emptyList())
                    }
            }
        }
    }

    fun handleGeolocalization(done: (LocationInfo) -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                when (val locationResponse = _geolocator.current()) {
                    is GeolocatorResult.Error -> Unit
                    is GeolocatorResult.Success -> {
                        val location = Coordinates(
                            locationResponse.data.coordinates.latitude,
                            locationResponse.data.coordinates.longitude
                        )
                        _nominatimApi.reverseGeocode(location, language = _language)
                            .suspendOnSuccess {
                                withContext(Dispatchers.Main) {
                                    done(
                                        LocationInfo(
                                            coordinates = location, name = LocationName(
                                                name = data.name,
                                                region = data.displayName.split(", ").run {
                                                    "${component2()}, ${last()}"
                                                },
                                            )
                                        )
                                    )
                                }
                            }
                            .onError { println(payload) }
                            .onFailure { println(message()) }
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    viewModel: MainScreenViewModel = viewModel { MainScreenViewModel() },
    openLocation: (LocationInfo) -> Unit,
    modifier: Modifier = Modifier,
) {

    val favorites = viewModel.favorites.collectAsStateWithLifecycle(null)
    val locations = viewModel.locations.collectAsStateWithLifecycle()
    val language = Locale.current.toLanguageTag()
    LaunchedEffect(language) { viewModel.setLanguage(language) }

    MainView(
        favorites.value,
        locations.value,
        viewModel::fetchLocations,
        viewModel::handleGeolocalization,
        openLocation,
        modifier = modifier,
    )
}
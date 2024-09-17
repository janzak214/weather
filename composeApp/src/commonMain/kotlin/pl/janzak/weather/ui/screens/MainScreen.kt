@file:OptIn(ExperimentalSharedTransitionApi::class)

package pl.janzak.weather.ui.screens

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.skydoves.sandwich.getOrThrow
import com.skydoves.sandwich.onFailure
import com.skydoves.sandwich.suspendOnSuccess
import dev.jordond.compass.geolocation.Geolocator
import dev.jordond.compass.geolocation.GeolocatorResult
import dev.jordond.compass.geolocation.Locator
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.janzak.weather.model.LocationName
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import pl.janzak.weather.model.Coordinates
import pl.janzak.weather.data.api.NominatimApi
import pl.janzak.weather.data.api.OpenMeteoGeocodingApi
import pl.janzak.weather.data.api.OpenMeteoWeatherApi
import pl.janzak.weather.database.Database
import pl.janzak.weather.model.FavoriteLocation
import pl.janzak.weather.model.LocationInfo
import pl.janzak.weather.ui.components.MainView
import pl.janzak.weather.ui.util.currentWeatherResponseToModel
import pl.janzak.weather.ui.util.dailyForecastResponseToModel

private val logger = KotlinLogging.logger {}

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
                    .onFailure {
                        logger.atError {
                            message = "geocoding failed"
                            payload = mapOf("error" to this)
                        }
                    }
            }
        }
    }

    fun handleGeolocalization(done: (LocationInfo) -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                logger.info { "started geolocation" }

                when (val locationResponse = _geolocator.current()) {
                    is GeolocatorResult.Error -> {
                        logger.atError {
                            logger.atError {
                                message = "geolocation failed"
                                payload = mapOf("error" to locationResponse.message)
                            }
                        }
                    }

                    is GeolocatorResult.Success -> {
                        logger.info { "geolocation success" }
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
                            .onFailure {
                                logger.atError {
                                    message = "reverse geocoding failed"
                                    payload = mapOf("error" to this)
                                }
                            }
                    }
                }
            }
        }
    }
}

context(AnimatedVisibilityScope, SharedTransitionScope)
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
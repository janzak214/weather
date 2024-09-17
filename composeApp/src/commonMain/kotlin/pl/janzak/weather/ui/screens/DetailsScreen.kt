@file:OptIn(ExperimentalSharedTransitionApi::class)

package pl.janzak.weather.ui.screens

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.skydoves.sandwich.getOrThrow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import pl.janzak.weather.data.api.HourlyForecast
import pl.janzak.weather.data.api.OpenMeteoWeatherApi
import pl.janzak.weather.database.Database
import pl.janzak.weather.database.Favorites
import pl.janzak.weather.model.CurrentWeather
import pl.janzak.weather.model.DayWeather
import pl.janzak.weather.model.LocationInfo
import pl.janzak.weather.ui.components.DetailsView
import pl.janzak.weather.ui.util.currentWeatherResponseToModel
import pl.janzak.weather.ui.util.dailyForecastResponseToModel

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

class DetailsScreenViewModel(private val locationInfo: LocationInfo) : ViewModel(),
    KoinComponent {
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
                        it to _weatherApi.hourlyForecast(coordinates, it.key)
                            .getOrThrow().hourly
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

context(AnimatedVisibilityScope, SharedTransitionScope)
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

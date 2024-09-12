package pl.janzak.weather.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.em
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.skydoves.sandwich.getOrThrow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
        Column(modifier = Modifier.padding(padding)) {
            Text(data.current.isDay.toString())
        }
    }

}

data class DetailsScreenData(
    val current: CurrentWeather,
    val daily: List<DayWeather>,
    val hourly: HourlyForecast,
)

class DetailsScreenViewModel(private val locationInfo: LocationInfo) : ViewModel(), KoinComponent {
    private val _db: Database by inject()
    private val _weatherApi: OpenMeteoWeatherApi by inject()

    val isFavorite =
        _db.favoritesQueries.loadFavorites(locationInfo.coordinates.toString()).asFlow()
            .mapToOneOrNull(Dispatchers.IO).map { it != null }

    private val _data = MutableStateFlow<DetailsScreenData?>(null)
    val data = _data.asStateFlow()

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
        println(
            _db.favoritesQueries.loadFavorites(locationInfo.coordinates.toString())
                .executeAsOneOrNull()
        )
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
                val hourly = async {
                    _weatherApi.hourlyForecast(coordinates).getOrThrow().hourly
                }

                _data.emit(
                    DetailsScreenData(
                        current.await(), daily.await(), hourly.await()
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


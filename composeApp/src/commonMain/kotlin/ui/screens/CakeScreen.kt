package ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import dev.jordond.compass.Location
import dev.jordond.compass.Priority
import dev.jordond.compass.geolocation.Geolocator
import dev.jordond.compass.geolocation.GeolocatorResult
import dev.jordond.compass.geolocation.LocationRequest
import dev.jordond.compass.geolocation.Locator
import dev.jordond.compass.geolocation.exception.GeolocationException
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.util.date.getTimeMillis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


@Serializable
data class Coordinates(
    val latitude: Float,
    val longitude: Float
)

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

suspend fun reverse(coordinates: Coordinates): ReverseGeocodingResponse {
    val response = HttpClient().get("https://nominatim.openstreetmap.org/reverse") {
        parameter("lat", coordinates.latitude)
        parameter("lon", coordinates.longitude)
        parameter("format", "jsonv2")
        parameter("zoom", 12)
        parameter("layer", "address")
        parameter("accept-language", "en-US")
    }

    return format.decodeFromString(response.bodyAsText())
}


@Serializable
data class ReverseGeocodingResponse(val name: String)

@Serializable
data class GeocodingResponse(val results: List<GeocodingEntry>? = null)

@Serializable
data class GeocodingEntry(
    val name: String,
    val latitude: Float,
    val longitude: Float,
    val country: String = "",
)

suspend fun geocode(query: String, count: Int = 10, language: String = "en"): GeocodingResponse {
    val response =
        HttpClient().get("https://geocoding-api.open-meteo.com/v1/search") {
            parameter("name", query)
            parameter("count", count)
            parameter("language", language)
            parameter("format", "json")
        }

    println(response.bodyAsText())

    return format.decodeFromString<GeocodingResponse>(response.bodyAsText())
}

expect fun getLocator(): Locator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CakeScreen(modifier: Modifier = Modifier) {
    var location: Coordinates? by remember { mutableStateOf(null) }

    var text by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var entries: List<GeocodingEntry> by remember { mutableStateOf(emptyList()) }
    var searchingForLocation by remember { mutableStateOf(false) }

    val geolocator = remember {
        val locator = getLocator()
        Geolocator(locator)
    }


    LaunchedEffect(text) {
        withContext(Dispatchers.IO) {
            if (text.isNotEmpty() && expanded)
                entries = geocode(text).results ?: emptyList()
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
                                locationResponse.data.coordinates.latitude.toFloat(),
                                locationResponse.data.coordinates.longitude.toFloat()
                            )
                            text = reverse(location!!).name
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
                    placeholder = { Text("Hinted search text") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (expanded) {
                            IconButton(onClick = {
                                expanded = false
                                text = ""
                            }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Close"
                                )
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


        Column(
            modifier = Modifier.semantics { traversalIndex = 1f }
                .padding(start = 16.dp, top = 2 * 72.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (location != null) {
                Text(
                    "${location!!.latitude} ${location!!.longitude}"
                )
            }
        }
    }
}

package pl.janzak.weather.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.materialkolor.ktx.toColor
import com.materialkolor.ktx.toHct
import org.jetbrains.compose.resources.stringResource
import pl.janzak.weather.model.LocationInfo
import resources.Res
import resources.search_field_label

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSearch(
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
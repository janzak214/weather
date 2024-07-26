package ui.screens

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun HomeScreen(
    navigateNumber: (number: Int) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    Scaffold(topBar = {
        CenterAlignedTopAppBar(
            title = { Text("Counters") },
            colors = TopAppBarDefaults.topAppBarColors(),
        )
    },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {},
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            ) {
                Icon(
                    Icons.Default.Add,
                    "Create a new counter"
                )
            }
        }
    ) { padding ->
        val counters = remember { (1..10).toList() }

        with(sharedTransitionScope) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 128.dp),
                modifier = Modifier.padding(padding).padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(counters) {
                    CounterCard(
                        "Card $it",
                        value = it,
                        onClick = { navigateNumber(it) },
                        modifier = Modifier.Companion.sharedElement(
                            sharedTransitionScope.rememberSharedContentState(
                                key = "counter-card-$it"
                            ),
                            animatedVisibilityScope = animatedVisibilityScope,
                        ),
                        textModifier = Modifier.Companion.sharedElement(
                            sharedTransitionScope.rememberSharedContentState(
                                key = "counter-text-$it"
                            ),
                            animatedVisibilityScope = animatedVisibilityScope,
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun CounterCard(
    name: String,
    value: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textModifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.elevatedCardColors()
            .copy(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
        elevation = CardDefaults.elevatedCardElevation(),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(8.dp).fillMaxSize()) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                "$value",
                style = MaterialTheme.typography.displayMedium,
                textAlign = TextAlign.Center,
                modifier = textModifier.align(Alignment.CenterHorizontally).alpha(0.7f)
                    .padding(vertical = 4.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                name,
                style = MaterialTheme.typography.titleSmall.copy(
                ),
            )

        }
    }
}

package ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import model.CounterId
import model.HomeViewModel
import org.jetbrains.compose.resources.stringResource
import resources.Res
import resources.counter_dialog_create_cancel
import resources.counter_dialog_create_confirm
import resources.counter_dialog_create_text_field_label
import resources.counter_dialog_create_title
import resources.main_screen_create_button
import resources.main_screen_title
import ui.theme.Easing

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun HomeScreen(
    navigateCounter: (counterId: CounterId) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    viewModel: HomeViewModel = viewModel { HomeViewModel() }
) {
    var createDialogVisible by remember { mutableStateOf(false) }


    Scaffold(topBar = {
        CenterAlignedTopAppBar(
            title = { Text(stringResource(Res.string.main_screen_title)) },
            colors = TopAppBarDefaults.topAppBarColors(),
        )
    },
        floatingActionButton = {

            FloatingActionButton(
                onClick = { createDialogVisible = true },
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            ) {
                Icon(
                    Icons.Default.Add,
                    stringResource(Res.string.main_screen_create_button)
                )
            }


        }
    ) { padding ->

        val dialogModifier: AnimatedVisibilityScope.() -> Modifier = {
            Modifier.animateEnterExit(
                enter = slideInVertically(animationSpec = Easing.enterTween()) { -it } + fadeIn(
                    animationSpec = Easing.enterTween()
                ),
                exit = slideOutVertically(animationSpec = Easing.exitTween()) { -it } + fadeOut(
                    animationSpec = Easing.exitTween()
                )
            )
        }

        AnimatedVisibility(createDialogVisible) {

            CreateCounterDialog(
                onConfirm = {
                    viewModel.create(it)
                    createDialogVisible = false
                },
                onDismiss = { createDialogVisible = false },
                modifier = dialogModifier()
            )
        }


        val counters by viewModel.counters.collectAsStateWithLifecycle()

        with(sharedTransitionScope) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 128.dp),
                modifier = Modifier.padding(padding).padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(counters) { (_, counter) ->
                    CounterCard(
                        name = counter.name,
                        value = counter.value,
                        onClick = { navigateCounter(counter.id) },
                        modifier = Modifier.Companion.sharedElement(
                            sharedTransitionScope.rememberSharedContentState(
                                key = "counter-card-${counter.id}"
                            ),
                            animatedVisibilityScope = animatedVisibilityScope,
                        ),
                        textModifier = Modifier.Companion.sharedElement(
                            sharedTransitionScope.rememberSharedContentState(
                                key = "counter-text-${counter.id}"
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


@Composable
fun CreateCounterDialog(
    onConfirm: (newName: String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                { onConfirm(currentName) },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                enabled = currentName != ""
            ) { Text(stringResource(Res.string.counter_dialog_create_confirm)) }
        },
        dismissButton = {
            TextButton(onDismiss) { Text(stringResource(Res.string.counter_dialog_create_cancel)) }
        },
        icon = {
            Icon(Icons.Default.Edit, null)
        },
        title = {
            Text(stringResource(Res.string.counter_dialog_create_title))
        },
        text = {
            TextField(
                currentName,
                { currentName = it },
                label = { Text(stringResource(Res.string.counter_dialog_create_text_field_label)) })
        },
        modifier = modifier,
    )
}
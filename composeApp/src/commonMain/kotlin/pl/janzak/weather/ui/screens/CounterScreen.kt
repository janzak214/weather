package ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import pl.janzak.weather.model.CounterId
import model.CounterViewModel
import model.CounterViewModelImpl
import org.jetbrains.compose.resources.stringResource
import resources.Res
import resources.counter_dialog_delete_cancel
import resources.counter_dialog_delete_confirm
import resources.counter_dialog_delete_text
import resources.counter_dialog_delete_title
import resources.counter_dialog_edit_cancel
import resources.counter_dialog_edit_confirm
import resources.counter_dialog_edit_text_field_label
import resources.counter_dialog_edit_title
import resources.counter_edit_button
import resources.counter_menu_delete
import resources.counter_menu_more
import ui.theme.Easing

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class,
)
@Composable
fun CounterScreen(
    goUp: () -> Unit,
    counterId: CounterId,
    visible: Boolean = true,

    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    viewModel: CounterViewModel = viewModel {
        CounterViewModelImpl(
            counterId
        )
    }
) {
    val nullableCounter by viewModel.counter.collectAsStateWithLifecycle(initialValue = null)
    val animationState =
        remember { MutableTransitionState(nullableCounter != null) }

    LaunchedEffect(nullableCounter != null) {
        animationState.targetState = nullableCounter != null
    }

    LaunchedEffect(visible) {
        println("Visible: $visible")
        animationState.targetState = visible
    }

    var deleteDialogVisible by remember { mutableStateOf(false) }
    var editDialogVisible by remember { mutableStateOf(false) }


    AnimatedVisibility(
        visibleState = animationState,
        enter = EnterTransition.None,
        exit = ExitTransition.None,
    ) {
        val counter = nullableCounter!!

        with(sharedTransitionScope) {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text(counter.name) },
                        navigationIcon = {
                            IconButton(onClick = {
                                goUp()
                            }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    "Go back"
                                )
                            }
                        },
                        actions = {
                            IconButton({ editDialogVisible = true }) {
                                Icon(
                                    Icons.Default.Edit,
                                    stringResource(Res.string.counter_edit_button)
                                )
                            }

                            Box(
                                modifier = Modifier.wrapContentSize(Alignment.TopStart)
                            ) {
                                var expanded by remember { mutableStateOf(false) }
                                IconButton({ expanded = true }) {
                                    Icon(
                                        Icons.Default.MoreVert,
                                        stringResource(Res.string.counter_menu_more)
                                    )
                                }
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }) {

                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                stringResource(Res.string.counter_menu_delete),
                                                style = MaterialTheme.typography.labelLarge
                                            )
                                        },
                                        onClick = { deleteDialogVisible = true },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = null
                                            )
                                        }
                                    )
                                }
                            }
                        },
                        modifier = Modifier.animateEnterExit(enter = fadeIn(), exit = fadeOut())
                    )
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

                AnimatedVisibility(deleteDialogVisible) {
                    DeleteCounterDialog(
                        onConfirm = {
                            deleteDialogVisible = false
                            goUp()
                            viewModel.delete()
                        },
                        onDismiss = { deleteDialogVisible = false },
                        name = counter.name,
                        modifier = dialogModifier()
                    )
                }

                AnimatedVisibility(editDialogVisible) {
                    EditCounterDialog(
                        onConfirm = {
                            viewModel.rename(it)
                            editDialogVisible = false
                        },
                        onDismiss = { editDialogVisible = false },
                        name = counter.name,
                        modifier = dialogModifier()
                    )
                }

                Column(
                    Modifier.fillMaxSize().padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Card(
                        modifier = Modifier.padding(8.dp).heightIn(0.dp, 200.dp).then(
                            Modifier.Companion.sharedElement(
                                sharedTransitionScope.rememberSharedContentState(
                                    key = "counter-card-$counterId"
                                ),
                                animatedVisibilityScope = animatedVisibilityScope,
                            )
                        ),
                        colors = CardDefaults.elevatedCardColors(),
                        elevation = CardDefaults.elevatedCardElevation(2.dp),
                        shape = CardDefaults.elevatedShape,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceAround,
                            modifier = Modifier.widthIn(0.dp, 400.dp).fillMaxSize().padding(32.dp)
                        ) {
                            val buttonModifier = Modifier.size(48.dp)
                            val buttonIconModifier = Modifier

                            IconButton(
                                { viewModel.decrement() },
                                modifier = buttonModifier,
                                colors = IconButtonDefaults.filledIconButtonColors()
                            ) {
                                Icon(
                                    Icons.Default.Remove,
                                    contentDescription = "Decrement",
                                    modifier = buttonIconModifier,
                                )
                            }
                            AnimatedContent(
                                targetState = counter.value,
                                transitionSpec = {
                                    if (targetState > initialState) {
                                        slideInVertically { height -> height } + fadeIn() togetherWith
                                                slideOutVertically { height -> -height } + fadeOut()
                                    } else {
                                        slideInVertically { height -> -height } + fadeIn() togetherWith
                                                slideOutVertically { height -> height } + fadeOut()
                                    }
                                },
                            ) { targetState ->
                                Text(
                                    "$targetState",
                                    style = MaterialTheme.typography.displayLarge,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            IconButton(
                                { viewModel.increment() },
                                modifier = buttonModifier,
                                colors = IconButtonDefaults.filledIconButtonColors()
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Increment",
                                    modifier = buttonIconModifier,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteCounterDialog(
    name: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onError,
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) { Text(stringResource(Res.string.counter_dialog_delete_confirm)) }
        },
        dismissButton = {
            TextButton(onDismiss) { Text(stringResource(Res.string.counter_dialog_delete_cancel)) }
        },
        icon = {
            Icon(Icons.Default.Delete, null)
        },
        title = {
            Text(stringResource(Res.string.counter_dialog_delete_title))
        },
        text = {
            Text(stringResource(Res.string.counter_dialog_delete_text, name))
        },
        modifier = modifier,
    )
}


@Composable
fun EditCounterDialog(
    name: String,
    onConfirm: (newName: String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentName by remember { mutableStateOf(name) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                { onConfirm(currentName) },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) { Text(stringResource(Res.string.counter_dialog_edit_confirm)) }
        },
        dismissButton = {
            TextButton(onDismiss) { Text(stringResource(Res.string.counter_dialog_edit_cancel)) }
        },
        icon = {
            Icon(Icons.Default.Edit, null)
        },
        title = {
            Text(stringResource(Res.string.counter_dialog_edit_title))
        },
        text = {
            TextField(
                currentName,
                { currentName = it },
                label = { Text(stringResource(Res.string.counter_dialog_edit_text_field_label)) })
        },
        modifier = modifier,
    )
}

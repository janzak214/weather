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
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class,
)
@Composable
fun NumberScreen(
    goUp: () -> Unit,
    number: Int,
    visible: Boolean = true,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val animationState =
        remember { MutableTransitionState(true) }
    val stateName = when {
        animationState.isIdle && animationState.currentState -> "Visible"
        !animationState.isIdle && animationState.currentState -> "Disappearing"
        animationState.isIdle && !animationState.currentState -> "Invisible"
        else -> "Appearing"
    }

    LaunchedEffect(stateName) {
        println("State: $stateName")
    }

    LaunchedEffect(visible) {
        println("Visible: $visible")
        animationState.targetState = visible
    }

    AnimatedVisibility(
        visibleState = animationState,
        enter = EnterTransition.None,
        exit = ExitTransition.None,
    ) {

        with(sharedTransitionScope) {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text("Card $number") },
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
                            IconButton({}) {
                                Icon(Icons.Default.Edit, "Edit")
                            }

                            Box(
                                modifier = Modifier.wrapContentSize(Alignment.TopStart)
                            ) {
                                var expanded by remember { mutableStateOf(false) }
                                IconButton({ expanded = true }) {
                                    Icon(Icons.Default.MoreVert, "More")
                                }
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }) {

                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                "Delete",
                                                style = MaterialTheme.typography.labelLarge
                                            )
                                        },
                                        onClick = goUp,
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

                Column(
                    Modifier.fillMaxSize().padding(padding)
                        .animateEnterExit(
                            enter = slideInHorizontally { it },
                            exit = slideOutHorizontally { it },
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Card(
                        modifier = Modifier.padding(8.dp).heightIn(0.dp, 200.dp).then(
                            Modifier.Companion.sharedElement(
                                sharedTransitionScope.rememberSharedContentState(
                                    key = "counter-card-$number"
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
                            var counterValue by remember { mutableIntStateOf((0)) }

                            IconButton(
                                { counterValue-- },
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
                                targetState = counterValue,
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
                                    modifier = Modifier
//                                        .Companion.sharedElement(
//                                            sharedTransitionScope.rememberSharedContentState(
//                                                key = "counter-text-$number"
//                                            ), animatedVisibilityScope = animatedVisibilityScope
//                                        )
                                        .weight(1f)
                                )
                            }
                            IconButton(
                                { counterValue++ },
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
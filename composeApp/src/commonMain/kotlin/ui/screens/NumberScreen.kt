package ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumberScreen(goUp: () -> Unit, number: Int, visible: Boolean = true) {
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
                        IconButton({}) {
                            Icon(Icons.Default.MoreVert, "More")
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
                    modifier = Modifier.padding(8.dp).heightIn(0.dp, 200.dp),
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
                            {},
                            modifier = buttonModifier,
                            colors = IconButtonDefaults.filledIconButtonColors()
                        ) {
                            Icon(
                                Icons.Default.Remove,
                                contentDescription = "Decrement",
                                modifier = buttonIconModifier,
                            )
                        }
                        Text(
                            "$number",
                            style = MaterialTheme.typography.displayLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.animateEnterExit(
                                enter = scaleIn(
                                    spring(stiffness = Spring.StiffnessLow)
                                ),
                                exit = scaleOut(spring())
                            ).weight(1f)
                        )
                        IconButton(
                            {},
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
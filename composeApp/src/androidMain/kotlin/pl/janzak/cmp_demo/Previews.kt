package pl.janzak.cmp_demo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import model.CounterId
import ui.screens.CreateCounterDialog
import ui.screens.DeleteCounterDialog
import ui.screens.EditCounterDialog
import ui.screens.HomeScreen
import ui.screens.NumberScreen

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
fun HomeScreenPreview() {
    SharedTransitionScope {
        AnimatedVisibility(true, modifier = it) {
            HomeScreen(
                navigateCounter = {},
                sharedTransitionScope = this@SharedTransitionScope,
                animatedVisibilityScope = this@AnimatedVisibility,
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
fun NumberScreenPreview() {
    SharedTransitionScope {
        AnimatedVisibility(true, modifier = it) {
            NumberScreen(
                goUp = {},
                counterId = CounterId(0),
                sharedTransitionScope = this@SharedTransitionScope,
                animatedVisibilityScope = this@AnimatedVisibility,

                )
        }
    }
}


@Preview
@Composable
fun DeleteCounterDialogPreview() {
    DeleteCounterDialog(name = "Test counter", onConfirm = {}, onDismiss = {})
}

@Preview
@Composable
fun EditCounterDialogPreview() {
    EditCounterDialog(name = "Test counter", onConfirm = {}, onDismiss = {})
}

@Preview
@Composable
fun CreateCounterDialogPreview() {
    CreateCounterDialog(onConfirm = {}, onDismiss = {})
}



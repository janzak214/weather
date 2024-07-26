package pl.janzak.cmp_demo

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ui.screens.HomeScreen
import ui.screens.NumberScreen

@Preview
@Composable
fun HomeScreenPreview() {
    HomeScreen(navigateNumber = {})
}

@Preview
@Composable
fun NumberScreenPreview() {
    NumberScreen(goUp = {}, number = 42)
}

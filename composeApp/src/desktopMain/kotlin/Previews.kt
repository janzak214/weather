import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
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

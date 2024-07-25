import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController

@Composable
actual fun HandleBack(navHostController: NavHostController) {
    androidx.activity.compose.BackHandler(enabled = true) {
        if (!duringScreenTransition(navHostController)) {
            navHostController.popBackStack()
        }
    }
}
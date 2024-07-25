package pl.janzak.cmp_demo

import App
import NumberScreen
import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat

internal val DefaultLightScrim = Color.argb(0xe6, 0xFF, 0xFF, 0xFF)

// The dark scrim color used in the platform.
// https://cs.android.com/android/platform/superproject/+/master:frameworks/base/core/res/res/color/system_bar_background_semi_transparent.xml
// https://cs.android.com/android/platform/superproject/+/master:frameworks/base/core/res/remote_color_resources_res/values/colors.xml;l=67
@VisibleForTesting
internal val DefaultDarkScrim = Color.argb(0x80, 0x1b, 0x1b, 0x1b)


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(statusBarStyle =  SystemBarStyle.auto(DefaultLightScrim, DefaultDarkScrim))
        setContent {
            val view = LocalView.current
            SideEffect {
                val window = (view.context as Activity).window
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
            }
            App()
        }
    }
}

@Preview(showSystemUi = false, name = "Preview", group = "A")
@Composable
fun Screen1() {
    NumberScreen(goBack = { /*TODO*/ }, number = 42)
}


@Preview(showSystemUi = false, name = "Preview", group = "A")
@Composable
fun Screen2() {
    NumberScreen(goBack = { /*TODO*/ }, number = 23)
}


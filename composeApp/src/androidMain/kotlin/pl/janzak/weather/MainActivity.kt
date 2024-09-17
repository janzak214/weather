package pl.janzak.weather

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import io.github.oshai.kotlinlogging.KotlinLogging
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

private val logger = KotlinLogging.logger { }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logger.info { "created activity" }

        startKoin {
            androidLogger()
            androidContext(this@MainActivity)
            modules(appModule)
        }

        enableEdgeToEdge()
        installSplashScreen()

        setContent {
            val view = LocalView.current
            val isInDarkTheme = isSystemInDarkTheme()

            LaunchedEffect(isInDarkTheme) {
                val window = (view.context as Activity).window
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                    !isInDarkTheme
            }
            App()
        }
    }
}

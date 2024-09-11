package ui.screens

import dev.jordond.compass.geolocation.Locator
import dev.jordond.compass.geolocation.mobile.MobileLocator

actual fun getLocator(): Locator = MobileLocator()

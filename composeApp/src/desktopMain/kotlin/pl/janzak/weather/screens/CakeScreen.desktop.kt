package ui.screens

import dev.jordond.compass.Location
import dev.jordond.compass.Priority
import dev.jordond.compass.geolocation.LocationRequest
import dev.jordond.compass.geolocation.Locator
import dev.jordond.compass.geolocation.exception.GeolocationException
import io.ktor.util.date.getTimeMillis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.serialization.json.Json


fun getLocation(): LocationResult {
    try {
        val script = """
    Set-StrictMode -Version 3.0

    try {
        Add-Type -AssemblyName System.Device
        ${'$'}GeoWatcher = New-Object System.Device.Location.GeoCoordinateWatcher
        ${'$'}GeoWatcher.Start()
        while ((${'$'}GeoWatcher.Status -ne 'Ready') -and (${'$'}GeoWatcher.Permission -ne 'Denied')) {
             Start-Sleep -Milliseconds 100
        }
        ${'$'}location = @{
            latitude=${'$'}GeoWatcher.Position.Location.Latitude; 
            longitude=${'$'}GeoWatcher.Position.Location.Longitude
        }
        
        Write-Output @{type="success"; result=${'$'}location} | ConvertTo-Json -Compress
    } catch {
        Write-Output @{type="error"; message=${'$'}_.Exception.Message} | ConvertTo-Json -Compress  
    }


""".trimIndent()

        val process = ProcessBuilder("powershell.exe", "-NonInteractive", "-Command", "-").start()
        process.outputWriter().write(script)
        process.outputWriter().flush()
        val result = process.inputReader().readLine()
        process.destroy()
        return Json.decodeFromString(result)
    } catch (e: Exception) {
        return LocationResult.Error(message = e.message ?: "Unknown error")
    }
}

class WindowsLocator : Locator {
    override val locationUpdates: Flow<Location>
        get() = emptyFlow()

    override suspend fun current(priority: Priority): Location =
        when (val location = getLocation()) {
            is LocationResult.Error -> throw GeolocationException(location.message)
            is LocationResult.Success -> Location(
                coordinates = dev.jordond.compass.Coordinates(
                    location.result.latitude.toDouble(), location.result.longitude.toDouble(),
                ),
                accuracy = Double.NaN,
                altitude = null,
                azimuth = null,
                speed = null,
                timestampMillis = getTimeMillis(),
            )
        }

    override fun isAvailable(): Boolean = true
    override fun stopTracking() = throw NotImplementedError()
    override suspend fun track(request: LocationRequest): Flow<Location> =
        throw NotImplementedError()
}


actual fun getLocator(): Locator = WindowsLocator()

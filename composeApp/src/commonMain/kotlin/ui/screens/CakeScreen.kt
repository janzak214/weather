package ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


@Serializable
data class Coordinates(
    val latitude: Float,
    val longitude: Float
)

@Serializable
sealed class LocationResult {
    @Serializable
    @SerialName("success")
    data class Success(
        val result: Coordinates
    ) : LocationResult()

    @Serializable
    @SerialName("error")
    data class Error(
        val message: String
    ) : LocationResult()
}

suspend fun reverse(coordinates: Coordinates): String {
    val response = HttpClient().get("https://nominatim.openstreetmap.org/reverse") {
        parameter("lat", coordinates.latitude)
        parameter("lon", coordinates.longitude)
        parameter("format", "jsonv2")
        parameter("zoom", 12)
        parameter("layer", "address")
        parameter("accept-language", "en-US")
    }



    return response.bodyAsText()
}

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

@Composable
fun CakeScreen(modifier: Modifier = Modifier) {
    var location: LocationResult? by remember { mutableStateOf(null) }
//    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            launch {
                location = getLocation()
                println(location)
                if (location is LocationResult.Success) {
                    val currentLocation = location as LocationResult.Success

                    println(reverse(currentLocation.result))
                }
            }
        }
    }



    if (location != null && location is LocationResult.Success) {
        val currentLocation = location as LocationResult.Success
        Text("${currentLocation.result.latitude}")
    }
}

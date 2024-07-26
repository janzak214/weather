package ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navigateNumber: (number: Int) -> Unit) {
    Scaffold(topBar = {
        CenterAlignedTopAppBar(
            title = { Text("Home") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.primary,
            ),
        )
    }) { padding ->
        Column(
            Modifier.fillMaxWidth().padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row {
                for (i in 0..4) {
                    Button(onClick = {
                        navigateNumber(i)
                    }) { Text("Route $i") }
                }
            }
        }
    }
}
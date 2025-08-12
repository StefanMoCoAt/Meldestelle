package at.mocode.client.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import at.mocode.client.ui.components.PingTestComponent

@Composable
fun App(baseUrl: String = "http://localhost:8080") {
    MaterialTheme {
        PingScreen(baseUrl)
    }
}

@Composable
fun PingScreen(baseUrl: String) {
    val pingComponent = remember { PingTestComponent() }
    var pingState by remember { mutableStateOf(pingComponent.state) }

    LaunchedEffect(pingComponent) {
        pingComponent.onStateChanged = { newState ->
            pingState = newState
        }
    }

    DisposableEffect(pingComponent) {
        onDispose {
            pingComponent.dispose()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Ping Backend Service",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        when {
            pingState.isLoading -> {
                CircularProgressIndicator()
                Text(
                    text = "Testing connection...",
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            pingState.error != null -> {
                Text(
                    text = "Error: ${pingState.error}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            pingState.response != null -> {
                Text(
                    text = "Response: ${pingState.response?.status ?: "Unknown"}",
                    color = if (pingState.isConnected) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = if (pingState.isConnected) "✓ Connected" else "✗ Not Connected",
                    color = if (pingState.isConnected) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { pingComponent.testConnection() },
            enabled = !pingState.isLoading
        ) {
            Text("Test Connection")
        }
    }
}

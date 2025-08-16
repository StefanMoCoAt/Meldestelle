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
import at.mocode.client.data.service.PingService
import at.mocode.client.ui.viewmodel.PingViewModel
import at.mocode.client.ui.viewmodel.PingUiState

@Composable
fun App(baseUrl: String = "http://localhost:8080") {
    MaterialTheme {
        PingScreen(baseUrl)
    }
}

@Composable
fun PingScreen(baseUrl: String) {
    val pingService = remember { PingService(baseUrl) }
    val viewModel = remember { PingViewModel(pingService) }

    DisposableEffect(viewModel) {
        onDispose {
            viewModel.dispose()
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
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Status display area with fixed height for consistent layout
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            contentAlignment = Alignment.Center
        ) {
            when (val state = viewModel.uiState) {
                is PingUiState.Initial -> {
                    Text(
                        text = "Klicke auf den Button, um das Backend zu testen",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                is PingUiState.Loading -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Text(
                            text = "Pinge Backend ...",
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
                is PingUiState.Success -> {
                    Text(
                        text = "Antwort vom Backend: ${state.response.status}",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                is PingUiState.Error -> {
                    Text(
                        text = "Fehler: ${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.pingBackend() },
            enabled = viewModel.uiState !is PingUiState.Loading
        ) {
            Text("Ping Backend")
        }
    }
}

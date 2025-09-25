package at.mocode.clients.pingfeature

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PingScreen(viewModel: PingViewModel) {
    val uiState = viewModel.uiState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Ping Service",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.performSimplePing() },
                enabled = !uiState.isLoading,
                modifier = Modifier.weight(1f)
            ) {
                Text("Simple Ping")
            }

            Button(
                onClick = { viewModel.performEnhancedPing() },
                enabled = !uiState.isLoading,
                modifier = Modifier.weight(1f)
            ) {
                Text("Enhanced Ping")
            }

            Button(
                onClick = { viewModel.performHealthCheck() },
                enabled = !uiState.isLoading,
                modifier = Modifier.weight(1f)
            ) {
                Text("Health Check")
            }
        }

        // Loading indicator
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        // Error message
        uiState.errorMessage?.let { error ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Error",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.clearError() }
                    ) {
                        Text("Dismiss")
                    }
                }
            }
        }

        // Simple Ping Response
        uiState.simplePingResponse?.let { response ->
            ResponseCard(
                title = "Simple Ping Response",
                status = response.status,
                timestamp = response.timestamp,
                service = response.service
            )
        }

        // Enhanced Ping Response
        uiState.enhancedPingResponse?.let { response ->
            ResponseCard(
                title = "Enhanced Ping Response",
                status = response.status,
                timestamp = response.timestamp,
                service = response.service,
                additionalInfo = mapOf(
                    "Circuit Breaker State" to response.circuitBreakerState,
                    "Response Time" to "${response.responseTime}ms"
                )
            )
        }

        // Health Response
        uiState.healthResponse?.let { response ->
            ResponseCard(
                title = "Health Check Response",
                status = response.status,
                timestamp = response.timestamp,
                service = response.service,
                additionalInfo = mapOf(
                    "Healthy" to response.healthy.toString()
                )
            )
        }
    }
}

@Composable
private fun ResponseCard(
    title: String,
    status: String,
    timestamp: String,
    service: String,
    additionalInfo: Map<String, String> = emptyMap()
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            InfoRow("Status", status)
            InfoRow("Timestamp", timestamp)
            InfoRow("Service", service)

            additionalInfo.forEach { (key, value) ->
                InfoRow(key, value)
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            fontWeight = FontWeight.Medium
        )
        Text(text = value)
    }
}

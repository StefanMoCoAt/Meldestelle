package at.mocode.ping.feature.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PingScreen(viewModel: PingViewModel) {
  val uiState = viewModel.uiState
  val scrollState = rememberScrollState()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
      .verticalScroll(scrollState),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Text(
      text = "Ping Service",
      style = MaterialTheme.typography.headlineMedium,
      fontWeight = FontWeight.Bold
    )

    if (uiState.isLoading || uiState.isSyncing) {
      CircularProgressIndicator()
    }

    if (uiState.errorMessage != null) {
      Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Error",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error
          )
          Text(text = uiState.errorMessage)
          Button(onClick = { viewModel.clearError() }) {
            Text("Clear")
          }
        }
      }
    }

    if (uiState.lastSyncResult != null) {
      Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Sync Status",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
          )
          Text(text = uiState.lastSyncResult)
        }
      }
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      Button(onClick = { viewModel.performSimplePing() }) {
        Text("Simple Ping")
      }
      Button(onClick = { viewModel.performEnhancedPing() }) {
        Text("Enhanced Ping")
      }
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      Button(onClick = { viewModel.performHealthCheck() }) {
        Text("Health Check")
      }
      Button(onClick = { viewModel.performSecurePing() }) {
        Text("Secure Ping")
      }
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      Button(onClick = { viewModel.triggerSync() }) {
        Text("Sync Now")
      }
    }

    if (uiState.simplePingResponse != null) {
      Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text("Simple / Secure Ping Response:", style = MaterialTheme.typography.titleMedium)
          Text("Status: ${uiState.simplePingResponse.status}")
          Text("Service: ${uiState.simplePingResponse.service}")
          Text("Timestamp: ${uiState.simplePingResponse.timestamp}")
        }
      }
    }

    if (uiState.enhancedPingResponse != null) {
      Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text("Enhanced Ping Response:", style = MaterialTheme.typography.titleMedium)
          Text("Status: ${uiState.enhancedPingResponse.status}")
          Text("Timestamp: ${uiState.enhancedPingResponse.timestamp}")
          Text("Circuit Breaker: ${uiState.enhancedPingResponse.circuitBreakerState}")
          Text("Response Time: ${uiState.enhancedPingResponse.responseTime}ms")
        }
      }
    }

    if (uiState.healthResponse != null) {
      Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text("Health Response:", style = MaterialTheme.typography.titleMedium)
          Text("Status: ${uiState.healthResponse.status}")
          Text("Healthy: ${uiState.healthResponse.healthy}")
          Text("Service: ${uiState.healthResponse.service}")
        }
      }
    }
  }
}

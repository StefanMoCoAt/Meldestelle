package at.mocode.clients.pingfeature

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.mocode.clients.pingfeature.model.ReitsportRole
import at.mocode.clients.pingfeature.model.ReitsportRoles
import at.mocode.clients.pingfeature.model.RoleCategory

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

        // Neue Reitsport-Authentication-Sektion
        Spacer(modifier = Modifier.height(24.dp))

        ReitsportTestingSection(
            viewModel = viewModel,
            uiState = uiState
        )
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

@Composable
private fun ReitsportTestingSection(
    viewModel: PingViewModel,
    uiState: PingUiState
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🐎",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Reitsport-Authentication-Testing",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "Teste verschiedene Benutzerrollen und ihre Berechtigungen im Meldestelle_Pro System",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
            )

            // Rollen-Grid
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 120.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(200.dp) // Feste Höhe für 2 Reihen
            ) {
                items(ReitsportRoles.ALL_ROLES) { role ->
                    RoleTestButton(
                        role = role,
                        onClick = { viewModel.testReitsportRole(role) },
                        isLoading = uiState.isLoading
                    )
                }
            }
        }
    }
}

@Composable
private fun RoleTestButton(
    role: ReitsportRole,
    onClick: () -> Unit,
    isLoading: Boolean
) {
    OutlinedButton(
        onClick = onClick,
        enabled = !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = when (role.category) {
                RoleCategory.SYSTEM -> Color(0xFFFF5722)
                RoleCategory.OFFICIAL -> Color(0xFF3F51B5)
                RoleCategory.ACTIVE -> Color(0xFF4CAF50)
                RoleCategory.PASSIVE -> Color(0xFF9E9E9E)
            }
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = role.icon,
                fontSize = 20.sp
            )
            Text(
                text = role.displayName.split(" ").first(), // Erstes Wort nur
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Text(
                text = "${role.permissions.size} Rechte",
                fontSize = 8.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}

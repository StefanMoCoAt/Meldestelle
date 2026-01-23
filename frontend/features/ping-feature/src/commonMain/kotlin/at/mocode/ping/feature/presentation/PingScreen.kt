package at.mocode.ping.feature.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.mocode.frontend.core.designsystem.components.DashboardCard
import at.mocode.frontend.core.designsystem.components.DenseButton
import at.mocode.frontend.core.designsystem.theme.Dimens

// --- Refactored PingScreen using Design System ---

@Composable
fun PingScreen(
    viewModel: PingViewModel,
    onBack: () -> Unit = {}
) {
    val uiState = viewModel.uiState

    // Wir nutzen jetzt das globale Theme (Hintergrund kommt vom Theme)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(Dimens.SpacingS) // Globales Spacing
    ) {
        // 1. Header
        PingHeader(
            onBack = onBack,
            isSyncing = uiState.isSyncing,
            isLoading = uiState.isLoading
        )

        Spacer(Modifier.height(Dimens.SpacingS))

        // 2. Main Dashboard Area (Split View)
        Row(modifier = Modifier.weight(1f)) {
            // Left Panel: Controls & Status Grid (60%)
            Column(
                modifier = Modifier
                    .weight(0.6f)
                    .fillMaxHeight()
                    .padding(end = Dimens.SpacingS)
            ) {
                ActionToolbar(viewModel)
                Spacer(Modifier.height(Dimens.SpacingS))
                StatusGrid(uiState)
            }

            // Right Panel: Terminal Log (40%)
            // Hier nutzen wir bewusst einen dunklen "Terminal"-Look, unabhängig vom Theme
            DashboardCard(
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxHeight()
            ) {
                LogHeader(onClear = { viewModel.clearLogs() })
                LogConsole(uiState.logs)
            }
        }

        Spacer(Modifier.height(Dimens.SpacingXS))

        // 3. Footer
        PingStatusBar(uiState.lastSyncResult)
    }
}

@Composable
private fun PingHeader(
    onBack: () -> Unit,
    isSyncing: Boolean,
    isLoading: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().height(40.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onBackground)
        }
        Text(
            "PING SERVICE // DASHBOARD",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f).padding(start = Dimens.SpacingS)
        )

        if (isLoading) {
            StatusBadge("BUSY", Color(0xFFFFA000)) // Amber
            Spacer(Modifier.width(Dimens.SpacingS))
        }

        if (isSyncing) {
            StatusBadge("SYNCING", MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(Dimens.SpacingS))
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            StatusBadge("IDLE", Color(0xFF388E3C)) // Green
        }
    }
}

@Composable
private fun StatusBadge(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.1f),
        contentColor = color,
        shape = MaterialTheme.shapes.small,
        border = androidx.compose.foundation.BorderStroke(1.dp, color)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun ActionToolbar(viewModel: PingViewModel) {
    // Wrap buttons to avoid overflow on small screens
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingXS)
    ) {
        DenseButton(text = "Simple", onClick = { viewModel.performSimplePing() })
        DenseButton(text = "Enhanced", onClick = { viewModel.performEnhancedPing() })
        DenseButton(text = "Secure", onClick = { viewModel.performSecurePing() })
        DenseButton(text = "Health", onClick = { viewModel.performHealthCheck() })
        DenseButton(
            text = "Sync",
            onClick = { viewModel.triggerSync() },
            containerColor = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
private fun StatusGrid(uiState: PingUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpacingS)) {
        // Row 1
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingS)) {
            DashboardCard(modifier = Modifier.weight(1f)) {
                StatusHeader("SIMPLE / SECURE PING")
                if (uiState.simplePingResponse != null) {
                    KeyValueRow("Status", uiState.simplePingResponse.status)
                    KeyValueRow("Service", uiState.simplePingResponse.service)
                    KeyValueRow("Time", uiState.simplePingResponse.timestamp)
                } else {
                    EmptyStateText()
                }
            }

            DashboardCard(modifier = Modifier.weight(1f)) {
                StatusHeader("HEALTH CHECK")
                if (uiState.healthResponse != null) {
                    KeyValueRow("Status", uiState.healthResponse.status)
                    KeyValueRow("Healthy", uiState.healthResponse.healthy.toString())
                    KeyValueRow("Service", uiState.healthResponse.service)
                } else {
                    EmptyStateText()
                }
            }
        }

        // Row 2
        DashboardCard(modifier = Modifier.fillMaxWidth()) {
            StatusHeader("ENHANCED PING (RESILIENCE)")
            if (uiState.enhancedPingResponse != null) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        KeyValueRow("Status", uiState.enhancedPingResponse.status)
                        KeyValueRow("Timestamp", uiState.enhancedPingResponse.timestamp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        KeyValueRow("Circuit Breaker", uiState.enhancedPingResponse.circuitBreakerState)
                        KeyValueRow("Latency", "${uiState.enhancedPingResponse.responseTime}ms")
                    }
                }
            } else {
                EmptyStateText()
            }
        }
    }
}

@Composable
private fun StatusHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = Dimens.SpacingXS)
    )
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
    Spacer(Modifier.height(Dimens.SpacingXS))
}

@Composable
private fun EmptyStateText() {
    Text("No Data", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun KeyValueRow(key: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(
            text = "$key:",
            modifier = Modifier.width(100.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// --- Log Components (Terminal Style - intentionally distinct) ---

@Composable
private fun LogHeader(onClear: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = Dimens.SpacingXS),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("EVENT LOG", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        TextButton(
            onClick = onClear,
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.height(24.dp)
        ) {
            Text("CLEAR", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun LogConsole(logs: List<LogEntry>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E)) // Always dark for terminal
            .padding(Dimens.SpacingXS),
        reverseLayout = false
    ) {
        items(logs) { log ->
            val color = if (log.isError) Color(0xFFFF5555) else Color(0xFF55FF55)
            Text(
                text = "[${log.timestamp}] [${log.source}] ${log.message}",
                color = color,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
private fun PingStatusBar(lastSync: String?) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = lastSync ?: "Ready",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = Dimens.SpacingS, vertical = 2.dp)
        )
    }
}

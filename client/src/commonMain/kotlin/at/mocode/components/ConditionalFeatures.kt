package at.mocode.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import at.mocode.getPlatform

/**
 * Conditional Feature Loading Manager
 * Lädt Features nur bei Bedarf um Bundle-Größe zu reduzieren
 */
object ConditionalFeatures {

    // Feature Flags für conditional loading
    private var debugModeEnabled by mutableStateOf(false)
    private var adminModeEnabled by mutableStateOf(false)
    private var advancedFeaturesEnabled by mutableStateOf(false)

    fun enableDebugMode() { debugModeEnabled = true }
    fun disableDebugMode() { debugModeEnabled = false }
    fun isDebugModeEnabled() = debugModeEnabled

    fun enableAdminMode() { adminModeEnabled = true }
    fun disableAdminMode() { adminModeEnabled = false }
    fun isAdminModeEnabled() = adminModeEnabled

    fun enableAdvancedFeatures() { advancedFeaturesEnabled = true }
    fun disableAdvancedFeatures() { advancedFeaturesEnabled = false }
    fun areAdvancedFeaturesEnabled() = advancedFeaturesEnabled

    // Platform-spezifische Feature-Detection
    fun isDesktopFeatureAvailable(): Boolean = getPlatform().name.contains("JVM", ignoreCase = true)
    fun isWebFeatureAvailable(): Boolean = getPlatform().name.contains("JavaScript", ignoreCase = true) ||
                                                getPlatform().name.contains("WASM", ignoreCase = true)
}

/**
 * Debug Panel - nur laden wenn Debug-Mode aktiviert
 */
@Composable
fun ConditionalDebugPanel() {
    // Nur rendern wenn Debug-Mode aktiv ist
    if (ConditionalFeatures.isDebugModeEnabled()) {
        LazyDebugPanel()
    }
}

@Composable
private fun LazyDebugPanel() {
    val platform = remember { getPlatform() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFECB3))
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "🐛 Debug Panel",
                style = MaterialTheme.typography.titleSmall,
                color = Color(0xFF6B5B00)
            )
            Text(
                text = "Platform: ${platform.name}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = "Bundle: WASM optimiert",
                style = MaterialTheme.typography.bodySmall
            )
            if (ConditionalFeatures.isDesktopFeatureAvailable()) {
                Text(
                    text = "Desktop-Features: Verfügbar",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF2E7D32)
                )
            }
            if (ConditionalFeatures.isWebFeatureAvailable()) {
                Text(
                    text = "Web-Features: Verfügbar",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF1976D2)
                )
            }
        }
    }
}

/**
 * Admin Panel - nur laden wenn Admin-Mode aktiviert
 */
@Composable
fun ConditionalAdminPanel() {
    if (ConditionalFeatures.isAdminModeEnabled()) {
        LazyAdminPanel()
    }
}

@Composable
private fun LazyAdminPanel() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "⚙️ Admin Panel",
                style = MaterialTheme.typography.titleSmall,
                color = Color(0xFFC62828)
            )

            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { ConditionalFeatures.enableAdvancedFeatures() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) {
                    Text("Erweiterte Features", style = MaterialTheme.typography.labelSmall)
                }

                Button(
                    onClick = { ConditionalFeatures.enableDebugMode() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                ) {
                    Text("Debug Mode", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

/**
 * Advanced Features - nur laden wenn explizit aktiviert
 */
@Composable
fun ConditionalAdvancedFeatures() {
    if (ConditionalFeatures.areAdvancedFeaturesEnabled()) {
        LazyAdvancedFeatures()
    }
}

@Composable
private fun LazyAdvancedFeatures() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🚀 Erweiterte Features",
                style = MaterialTheme.typography.titleSmall,
                color = Color(0xFF7B1FA2)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Erweiterte Ping-Statistiken (nur bei Bedarf geladen)
            LazyPingStatistics()

            Spacer(modifier = Modifier.height(8.dp))

            // Platform-spezifische Features
            if (ConditionalFeatures.isDesktopFeatureAvailable()) {
                LazyDesktopOnlyFeatures()
            }

            if (ConditionalFeatures.isWebFeatureAvailable()) {
                LazyWebOnlyFeatures()
            }
        }
    }
}

@Composable
private fun LazyPingStatistics() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8))
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = "📊 Ping-Statistiken",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF388E3C)
            )
            Text(
                text = "Letzter Ping: Erfolgreich",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = "Durchschnitt: ~200ms",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun LazyDesktopOnlyFeatures() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE1F5FE))
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = "🖥️ Desktop Features",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF0277BD)
            )
            Text(
                text = "• Datei-Export verfügbar",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = "• System-Integration aktiv",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun LazyWebOnlyFeatures() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = "🌐 Web Features",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFFF57C00)
            )
            Text(
                text = "• PWA-Support verfügbar",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = "• Browser-API Integration",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

/**
 * Feature Control Panel - für Benutzer-Kontrolle über conditional loading
 */
@Composable
fun FeatureControlPanel() {
    var showControls by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = { showControls = !showControls },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF424242))
        ) {
            Text(
                if (showControls) "Feature-Kontrollen ausblenden" else "Feature-Kontrollen anzeigen",
                style = MaterialTheme.typography.labelMedium
            )
        }

        if (showControls) {
            LazyFeatureControls()
        }
    }
}

@Composable
private fun LazyFeatureControls() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "🎛️ Feature Controls",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        if (ConditionalFeatures.isDebugModeEnabled()) {
                            ConditionalFeatures.disableDebugMode()
                        } else {
                            ConditionalFeatures.enableDebugMode()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (ConditionalFeatures.isDebugModeEnabled())
                            Color(0xFF4CAF50) else Color(0xFF9E9E9E)
                    )
                ) {
                    Text("Debug", style = MaterialTheme.typography.labelSmall)
                }

                Button(
                    onClick = {
                        if (ConditionalFeatures.isAdminModeEnabled()) {
                            ConditionalFeatures.disableAdminMode()
                        } else {
                            ConditionalFeatures.enableAdminMode()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (ConditionalFeatures.isAdminModeEnabled())
                            Color(0xFF4CAF50) else Color(0xFF9E9E9E)
                    )
                ) {
                    Text("Admin", style = MaterialTheme.typography.labelSmall)
                }

                Button(
                    onClick = {
                        if (ConditionalFeatures.areAdvancedFeaturesEnabled()) {
                            ConditionalFeatures.disableAdvancedFeatures()
                        } else {
                            ConditionalFeatures.enableAdvancedFeatures()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (ConditionalFeatures.areAdvancedFeaturesEnabled())
                            Color(0xFF4CAF50) else Color(0xFF9E9E9E)
                    )
                ) {
                    Text("Erweitert", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

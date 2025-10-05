package at.mocode.clients.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import at.mocode.clients.authfeature.AuthTokenManager
import at.mocode.clients.authfeature.Permission

@Composable
fun LandingScreen(
    authTokenManager: AuthTokenManager? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Willkommen bei Meldestelle",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Eine moderne, skalierbare Frontend-Architektur",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Diese Anwendung demonstriert eine \"Shell + Feature-Module\"-Architektur " +
                          "basierend auf Kotlin Multiplatform. Sie spiegelt die DDD-Struktur des Backends " +
                          "wider und ist als native Desktop-Anwendung (JVM) und Web-Anwendung (JS/Wasm) lauffähig.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "🚀 Technologien:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TechItem("Kotlin Multiplatform")
                    TechItem("Jetpack Compose Multiplatform")
                    TechItem("Material Design 3")
                    TechItem("Ktor Client")
                    TechItem("Domain-Driven Design")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Verwenden Sie das Ping Service Menü oben, um die API-Funktionalität zu testen.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Permission-based UI demonstration
        authTokenManager?.let { tokenManager ->
            val authState by tokenManager.authState.collectAsState()

            if (authState.isAuthenticated && authState.permissions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🔐 Verfügbare Funktionen",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Admin features (visible only to users with delete permissions)
                        if (tokenManager.isAdmin()) {
                            PermissionCard(
                                title = "👑 Administrator-Bereich",
                                description = "Vollzugriff auf alle System-Funktionen",
                                permissions = listOf("Alle Berechtigungen", "System-Verwaltung", "Benutzer-Management"),
                                backgroundColor = MaterialTheme.colorScheme.errorContainer,
                                textColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }

                        // Management features (visible to users with create/update permissions)
                        if (tokenManager.canCreate() || tokenManager.canUpdate()) {
                            PermissionCard(
                                title = "✏️ Verwaltung",
                                description = "Erstellen und bearbeiten von Daten",
                                permissions = buildList {
                                    if (tokenManager.hasPermission(Permission.PERSON_CREATE)) add("Personen erstellen")
                                    if (tokenManager.hasPermission(Permission.PERSON_UPDATE)) add("Personen bearbeiten")
                                    if (tokenManager.hasPermission(Permission.VEREIN_CREATE)) add("Vereine erstellen")
                                    if (tokenManager.hasPermission(Permission.VEREIN_UPDATE)) add("Vereine bearbeiten")
                                    if (tokenManager.hasPermission(Permission.PFERD_CREATE)) add("Pferde erstellen")
                                    if (tokenManager.hasPermission(Permission.PFERD_UPDATE)) add("Pferde bearbeiten")
                                    if (tokenManager.hasPermission(Permission.VERANSTALTUNG_CREATE)) add("Veranstaltungen erstellen")
                                    if (tokenManager.hasPermission(Permission.VERANSTALTUNG_UPDATE)) add("Veranstaltungen bearbeiten")
                                },
                                backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                                textColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        // Read-only features (visible to all authenticated users)
                        if (tokenManager.canRead()) {
                            PermissionCard(
                                title = "👁️ Ansicht",
                                description = "Nur-Lese-Zugriff auf Daten",
                                permissions = buildList {
                                    if (tokenManager.hasPermission(Permission.PERSON_READ)) add("Personen anzeigen")
                                    if (tokenManager.hasPermission(Permission.VEREIN_READ)) add("Vereine anzeigen")
                                    if (tokenManager.hasPermission(Permission.PFERD_READ)) add("Pferde anzeigen")
                                    if (tokenManager.hasPermission(Permission.VERANSTALTUNG_READ)) add("Veranstaltungen anzeigen")
                                },
                                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                                textColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TechItem(text: String) {
    Text(
        text = "• $text",
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}

@Composable
private fun PermissionCard(
    title: String,
    description: String,
    permissions: List<String>,
    backgroundColor: androidx.compose.ui.graphics.Color,
    textColor: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor
            )

            if (permissions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                permissions.forEach { permission ->
                    Text(
                        text = "✓ $permission",
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}

package at.mocode

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.mocode.config.AppServiceConfiguration
import at.mocode.config.ThemeService
import at.mocode.di.ServiceRegistry
import at.mocode.di.resolve
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    // State to track if services are initialized
    var servicesInitialized by remember { mutableStateOf(false) }

    // Initialize services when the app starts
    LaunchedEffect(Unit) {
        AppServiceConfiguration.configureAppServices()
        servicesInitialized = true
    }

    // Only show the app content after services are initialized
    if (servicesInitialized) {
        // Get theme service to demonstrate ServiceLocator usage
        val themeService: ThemeService = ServiceRegistry.serviceLocator.resolve()
        val currentTheme by remember { mutableStateOf(themeService.getCurrentTheme()) }

        MaterialTheme(
            colors = lightColors(
                primary = Color(0xFF2E7D32),
                primaryVariant = Color(0xFF1B5E20),
                secondary = Color(0xFF8BC34A),
                background = Color(0xFFF1F8E9)
            )
        ) {
            HomePage()
        }
    } else {
        // Show loading state while services are being initialized
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun HomePage() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Header Section
            HeaderSection()
        }

        item {
            // Welcome, Card
            WelcomeCard()
        }

        item {
            // Quick Actions
            QuickActionsSection()
        }

        item {
            // Features Overview
            FeaturesSection()
        }

        item {
            // Footer
            FooterSection()
        }
    }
}

@Composable
fun HeaderSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = MaterialTheme.colors.primary
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🏆",
                style = MaterialTheme.typography.h2,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Meldestelle",
                style = MaterialTheme.typography.h3.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Turnierverwaltungssystem",
                style = MaterialTheme.typography.subtitle1.copy(
                    color = Color.White.copy(alpha = 0.9f)
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun WelcomeCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Willkommen bei der Meldestelle",
                style = MaterialTheme.typography.h5.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.primary
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Ihr zentrales System für die Verwaltung von Reitturnieren. " +
                        "Verwalten Sie Turniere, Anmeldungen, Teilnehmer und alle " +
                        "wichtigen Informationen rund um Ihre Veranstaltungen.",
                style = MaterialTheme.typography.body1,
                lineHeight = 24.sp
            )
        }
    }
}

@Composable
fun QuickActionsSection() {
    Column {
        Text(
            text = "Schnellzugriff",
            style = MaterialTheme.typography.h6.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.primary
            ),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                title = "Neues Turnier",
                emoji = "➕",
                modifier = Modifier.weight(1f)
            )
            QuickActionCard(
                title = "Turniere anzeigen",
                emoji = "📋",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                title = "Anmeldungen",
                emoji = "👥",
                modifier = Modifier.weight(1f)
            )
            QuickActionCard(
                title = "Berichte",
                emoji = "📊",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    emoji: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = 2.dp,
        backgroundColor = MaterialTheme.colors.secondary.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.h4,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.body2.copy(
                    fontWeight = FontWeight.Medium
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun FeaturesSection() {
    Column {
        Text(
            text = "Funktionen",
            style = MaterialTheme.typography.h6.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.primary
            ),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        val features = listOf(
            FeatureItem("Turnierverwaltung", "Erstellen und verwalten Sie Reitturniere mit allen Details", "🏇"),
            FeatureItem("Teilnehmerverwaltung", "Verwalten Sie Reiter, Pferde und Vereine", "👥"),
            FeatureItem("Anmeldungen", "Bearbeiten Sie Turnieranmeldungen und Nennungen", "📝"),
            FeatureItem("Plätze & Richter", "Verwalten Sie Austragungsorte und Richter", "📍"),
            FeatureItem("Ergebnisse", "Erfassen und verwalten Sie Turnierergebnisse", "🏆"),
            FeatureItem("Berichte", "Erstellen Sie detaillierte Berichte und Statistiken", "📊")
        )

        features.forEach { feature ->
            FeatureCard(feature)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun FeatureCard(feature: FeatureItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = feature.emoji,
                style = MaterialTheme.typography.h5,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = feature.title,
                    style = MaterialTheme.typography.subtitle2.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
                Text(
                    text = feature.description,
                    style = MaterialTheme.typography.body2.copy(
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                )
            }
        }
    }
}

@Composable
fun FooterSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 1.dp,
        backgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.05f)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Meldestelle v1.0.0",
                style = MaterialTheme.typography.body2.copy(
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colors.primary
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Turnierverwaltungssystem für Reitsport",
                style = MaterialTheme.typography.caption.copy(
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            )
        }
    }
}

data class FeatureItem(
    val title: String,
    val description: String,
    val emoji: String
)

package at.mocode.client.common.components.horses

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import at.mocode.horses.domain.model.DomPferd

/**
 * Compose component that displays a list of horses (Pferde).
 * This is a Compose-based replacement for the React-based PferdeListe component.
 */
@Composable
fun PferdeListe(
    horses: List<DomPferd> = emptyList(),
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onHorseClick: (DomPferd) -> Unit = {}
) {
    // UI rendering with Compose
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Pferde-Register",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            errorMessage != null -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = errorMessage,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            horses.isEmpty() -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = "Keine Pferde verfügbar",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(horses) { horse ->
                        HorseCard(horse = horse, onClick = { onHorseClick(horse) })
                    }
                }
            }
        }
    }
}

@Composable
private fun HorseCard(
    horse: DomPferd,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = horse.getDisplayName(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Basic information
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🐎",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Geschlecht: ${horse.geschlecht.name}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            horse.geburtsdatum?.let { birthDate ->
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📅",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = buildString {
                            append("Geburtsdatum: $birthDate")
                            horse.getAge()?.let { age ->
                                append(" (${age} Jahre alt)")
                            }
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Breed and color
            val breedAndColor = mutableListOf<String>()
            horse.rasse?.let { breedAndColor.add("Rasse: $it") }
            horse.farbe?.let { breedAndColor.add("Farbe: $it") }

            if (breedAndColor.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🏇",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = breedAndColor.joinToString(" | "),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Identification numbers (show only the most important ones in the card)
            val identificationNumbers = mutableListOf<String>()
            horse.lebensnummer?.let { identificationNumbers.add("Lebensnummer: $it") }
            horse.oepsNummer?.let { identificationNumbers.add("OEPS: $it") }

            if (identificationNumbers.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🆔",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = identificationNumbers.joinToString(" | "),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Status indicators
            val statusList = mutableListOf<String>()
            if (horse.istAktiv) statusList.add("Aktiv") else statusList.add("Inaktiv")
            if (horse.isOepsRegistered()) statusList.add("OEPS registriert")
            if (horse.isFeiRegistered()) statusList.add("FEI registriert")

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Status: ${statusList.joinToString(", ")}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Data source
            Text(
                text = "Datenquelle: ${horse.datenQuelle.name}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * A badge that displays the horse's registration status
 */
@Composable
fun HorseStatusBadge(horse: DomPferd) {
    val status = when {
        horse.isFeiRegistered() -> "FEI"
        horse.isOepsRegistered() -> "OEPS"
        else -> null
    }

    status?.let {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = MaterialTheme.shapes.small
        ) {
            Text(
                text = it,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

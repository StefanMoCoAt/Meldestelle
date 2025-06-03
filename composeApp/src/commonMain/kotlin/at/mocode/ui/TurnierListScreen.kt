package at.mocode.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.mocode.model.Turnier
import at.mocode.network.httpClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.launch
import at.mocode.SERVER_PORT
import at.mocode.PlatformInfo

/**
 * Screen that displays a list of all tournaments
 *
 * @param onTurnierSelected Callback function called when a tournament is selected
 * @param onAdminClicked Callback function called when the admin button is clicked
 */
@Composable
fun TurnierListScreen(
    onTurnierSelected: (Turnier) -> Unit,
    onAdminClicked: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var turniere by remember { mutableStateOf<List<Turnier>>(emptyList()) }

    // Sample data for development - will be replaced with API call
    val sampleTurniere = remember {
        listOf(
            Turnier(
                name = "CSN-C NEU CSNP-C NEU NEUMARKT/M., OÖ",
                datum = "7.JUNI 2025",
                number = 25319,
                bewerbe = listOf(
                    at.mocode.model.Bewerb(1, "Pony Stilspringprüfung", "60 cm", null),
                    at.mocode.model.Bewerb(2, "Stilspringprüfung", "60 cm", null),
                    at.mocode.model.Bewerb(3, "Pony Stilspringprüfung", "70 cm", null),
                    at.mocode.model.Bewerb(4, "Stilspringprüfung", "80 cm", null),
                    at.mocode.model.Bewerb(5, "Pony Stilspringprüfung", "95 cm", null),
                    at.mocode.model.Bewerb(6, "Stilspringprüfung", "95 cm", null),
                    at.mocode.model.Bewerb(7, "Einlaufspringprüfung", "95cm", null),
                    at.mocode.model.Bewerb(8, "Springpferdeprüfung", "105 cm", null),
                    at.mocode.model.Bewerb(9, "Stilspringprüfung", "105 cm", null),
                    at.mocode.model.Bewerb(10, "Standardspringprüfung", "105cm", null)
                )
            ),
            Turnier(
                name = "CSN-B LAMBACH, OÖ",
                datum = "14.JUNI 2025",
                number = 25320,
                bewerbe = listOf(
                    at.mocode.model.Bewerb(1, "Stilspringprüfung", "80 cm", null),
                    at.mocode.model.Bewerb(2, "Stilspringprüfung", "95 cm", null),
                    at.mocode.model.Bewerb(3, "Standardspringprüfung", "105 cm", null)
                )
            )
        )
    }

    // Load tournaments from API
    LaunchedEffect(Unit) {
        try {
            val url = "http://${at.mocode.PlatformInfo.apiHost}:${at.mocode.SERVER_PORT}/api/turniere"
            val response = httpClient.get(url)
            turniere = response.body<List<Turnier>>()
            isLoading = false
        } catch (e: Exception) {
            // Fallback to sample data if API fails
            turniere = sampleTurniere
            errorMessage = "Fehler beim Laden der Turniere vom Server. Zeige lokale Daten an."
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .widthIn(max = 800.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Aktuelle Turniere",
                fontSize = 24.sp,
                textAlign = TextAlign.Center
            )

            Button(
                onClick = onAdminClicked,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Admin")
            }
        }

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(16.dp)
                    .size(48.dp)
            )
        } else if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center
            )
        } else if (turniere.isEmpty()) {
            Text(
                text = "Keine Turniere verfügbar",
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(turniere) { turnier ->
                    TurnierCard(
                        turnier = turnier,
                        onNennungClick = { onTurnierSelected(turnier) }
                    )
                }
            }
        }
    }
}

/**
 * Card that displays a tournament
 *
 * @param turnier The tournament to display
 * @param onNennungClick Callback function called when the "Nennung" button is clicked
 */
@Composable
fun TurnierCard(
    turnier: Turnier,
    onNennungClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            SelectionContainer {
                Column {
                    Text(
                        text = turnier.name,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = turnier.datum,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "Turnier-Nr.: ${turnier.number}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Display number of competitions
                    Text(
                        text = "${turnier.bewerbe.size} Bewerbe",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "Verfügbare Wettbewerbe für Ihre Teilnahme",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            }

            // Nennung button
            Button(
                onClick = onNennungClick,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Nennung")
            }
        }
    }
}

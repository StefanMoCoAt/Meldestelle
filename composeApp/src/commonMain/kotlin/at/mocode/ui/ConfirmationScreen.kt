package at.mocode.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.mocode.model.Nennung

/**
 * Confirmation screen displayed after a successful form submission
 *
 * @param submittedData The data that was submitted in the form
 * @param onNewSubmission Callback function called when the user wants to submit a new entry
 */
@Composable
fun ConfirmationScreen(
    submittedData: Nennung,
    onNewSubmission: () -> Unit
) {
    Column(
        modifier = Modifier
            .widthIn(max = 800.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Vielen Dank für Ihre Nennung für das Turnier",
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Tournament information
        Surface(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "CSN-C NEU CSNP-C NEU NEUMARKT/M., OÖ",
                    textAlign = TextAlign.Center
                )
                Text("7.JUNI 2025", textAlign = TextAlign.Center)
                Text("Turnier-Nr.: 25319", textAlign = TextAlign.Center)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Summary of the sent data
        Surface(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            color = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Ihre gesendeten Daten:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    "Reiter: ${submittedData.riderName}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Text(
                    "Pferd: ${submittedData.horseName}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                if (submittedData.email.isNotBlank()) {
                    Text(
                        "E-Mail: ${submittedData.email}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                if (submittedData.phone.isNotBlank()) {
                    Text(
                        "Telefon: ${submittedData.phone}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                Text(
                    "Bewerbe:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 4.dp, top = 8.dp)
                )

                submittedData.selectedEvents.forEach { event ->
                    Text(
                        "• $event",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 16.dp, bottom = 2.dp)
                    )
                }

                if (submittedData.comments.isNotBlank()) {
                    Text(
                        "Bemerkungen:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 4.dp, top = 8.dp)
                    )
                    Text(
                        submittedData.comments,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Button for another submission
        Button(
            onClick = onNewSubmission,
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
        ) {
            Text("Weitere Nennung abgeben")
        }
    }
}

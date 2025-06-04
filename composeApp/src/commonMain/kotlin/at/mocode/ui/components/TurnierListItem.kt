package at.mocode.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import at.mocode.model.Turnier

/**
 * List item for a tournament
 */
@Composable
fun TurnierListItem(
    turnier: Turnier,
    isSelected: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                // Make the whole card clickable instead of just the column
                SelectionContainer {
                    Column {
                        Text(
                            text = turnier.name,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = turnier.datum,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Turnier-Nr.: ${turnier.number}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "${turnier.bewerbe.size} Bewerbe",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Simple URL display
                SelectionContainer {
                    Text("Nennformular: http://localhost:3000/form/" + turnier.number.toString())
                }
            }

            Button(onClick = onEdit) {
                Text("Bearbeiten")
            }

            Button(
                onClick = { showDeleteConfirmation = true },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Löschen")
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Turnier löschen") },
            text = { Text("Möchten Sie das Turnier '${turnier.name}' wirklich löschen?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirmation = false
                    }
                ) {
                    Text("Löschen")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }
}

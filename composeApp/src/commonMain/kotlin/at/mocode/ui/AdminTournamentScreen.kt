package at.mocode.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.mocode.model.Turnier
import at.mocode.ui.components.TurnierForm
import at.mocode.ui.components.TurnierListItem
import at.mocode.viewmodel.TurnierViewModel
import kotlinx.coroutines.launch

/**
 * Screen for managing tournaments (CRUD operations)
 *
 * @param onBackClicked Callback function called when the back button is clicked
 */
@Composable
fun AdminTournamentScreen(
    onBackClicked: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val viewModel = remember { TurnierViewModel(coroutineScope) }

    // Initial load
    LaunchedEffect(Unit) {
        viewModel.loadTurniere()
    }

    // Clear success message after 3 seconds
    LaunchedEffect(viewModel.successMessage) {
        if (viewModel.successMessage != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearSuccessMessage()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Centered content
        Column(
            modifier = Modifier
                .widthIn(max = 800.dp)
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            // Title, back button, and create button
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onBackClicked,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Text("← Zurück")
                    }

                    Text(
                        text = "Turniere verwalten",
                        fontSize = 24.sp,
                        textAlign = TextAlign.Start
                    )
                }

                Button(
                    onClick = {
                        viewModel.isCreating = true
                        viewModel.isEditing = false
                        viewModel.selectedTurnier = null
                    },
                    enabled = !viewModel.isLoading && !viewModel.isCreating && !viewModel.isEditing
                ) {
                    Text("+")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Neues Turnier")
                }
            }

            // Messages
            if (viewModel.errorMessage != null) {
                Text(
                    text = viewModel.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
            }
            if (viewModel.successMessage != null) {
                Text(
                    text = viewModel.successMessage!!,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
            }

            // Loading indicator
            if (viewModel.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // Tournament creation/editing form
            if (viewModel.isCreating || viewModel.isEditing) {
                TurnierForm(
                    turnier = if (viewModel.isEditing) viewModel.selectedTurnier else null,
                    onSave = { turnier ->
                        if (viewModel.isEditing) {
                            viewModel.updateTurnier(turnier)
                        } else {
                            viewModel.createTurnier(turnier)
                        }
                    },
                    onCancel = {
                        viewModel.isCreating = false
                        viewModel.isEditing = false
                    }
                )
            }

            // Tournament list
            if (!viewModel.isLoading && !viewModel.isCreating && viewModel.turniere.isNotEmpty()) {
                Text(
                    text = "Vorhandene Turniere",
                    fontSize = 18.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(viewModel.turniere) { turnier ->
                        TurnierListItem(
                            turnier = turnier,
                            isSelected = viewModel.selectedTurnier?.number == turnier.number,
                            onClick = {
                                viewModel.selectedTurnier = turnier
                                viewModel.isEditing = false
                                viewModel.isCreating = false
                            },
                            onEdit = {
                                viewModel.selectedTurnier = turnier
                                viewModel.isEditing = true
                                viewModel.isCreating = false
                            },
                            onDelete = {
                                viewModel.deleteTurnier(turnier.number)
                            }
                        )
                    }
                }
            } else if (!viewModel.isLoading && !viewModel.isCreating && viewModel.turniere.isEmpty()) {
                Text(
                    text = "Keine Turniere vorhanden",
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        }
    }
}

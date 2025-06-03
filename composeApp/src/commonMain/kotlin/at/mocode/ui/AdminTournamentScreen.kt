package at.mocode.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.mocode.model.ApiResponse
import at.mocode.model.Bewerb
import at.mocode.model.Turnier
import at.mocode.network.httpClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
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
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var turniere by remember { mutableStateOf<List<Turnier>>(emptyList()) }
    var selectedTurnier by remember { mutableStateOf<Turnier?>(null) }
    var isEditing by remember { mutableStateOf(false) }
    var isCreating by remember { mutableStateOf(false) }

    // Load tournaments from API
    fun loadTurniere() {
        coroutineScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val url = "http://${at.mocode.PlatformInfo.apiHost}:${at.mocode.SERVER_PORT}/api/turniere"
                val response = httpClient.get(url)
                turniere = response.body<List<Turnier>>()
                isLoading = false
            } catch (e: Exception) {
                errorMessage = "Fehler beim Laden der Turniere vom Server: ${e.message}"
                isLoading = false
            }
        }
    }

    // Initial load
    LaunchedEffect(Unit) {
        loadTurniere()
    }

    // Create a new tournament
    fun createTurnier(turnier: Turnier) {
        coroutineScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val url = "http://${at.mocode.PlatformInfo.apiHost}:${at.mocode.SERVER_PORT}/api/turniere"
                val response = httpClient.post(url) {
                    contentType(ContentType.Application.Json)
                    setBody(turnier)
                }
                if (response.status.isSuccess()) {
                    successMessage = "Turnier erfolgreich erstellt"
                    loadTurniere()
                    isCreating = false
                } else {
                    val errorResponse = response.body<ApiResponse>()
                    errorMessage = errorResponse.message ?: "Fehler beim Erstellen des Turniers"
                }
            } catch (e: Exception) {
                errorMessage = "Fehler beim Erstellen des Turniers: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    // Update an existing tournament
    fun updateTurnier(turnier: Turnier) {
        coroutineScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val url =
                    "http://${at.mocode.PlatformInfo.apiHost}:${at.mocode.SERVER_PORT}/api/turniere/${turnier.number}"
                val response = httpClient.put(url) {
                    contentType(ContentType.Application.Json)
                    setBody(turnier)
                }
                if (response.status.isSuccess()) {
                    successMessage = "Turnier erfolgreich aktualisiert"
                    loadTurniere()
                    isEditing = false
                } else {
                    val errorResponse = response.body<ApiResponse>()
                    errorMessage = errorResponse.message ?: "Fehler beim Aktualisieren des Turniers"
                }
            } catch (e: Exception) {
                errorMessage = "Fehler beim Aktualisieren des Turniers: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    // Delete a tournament
    fun deleteTurnier(turnierNumber: Int) {
        coroutineScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val url =
                    "http://${at.mocode.PlatformInfo.apiHost}:${at.mocode.SERVER_PORT}/api/turniere/$turnierNumber"
                val response = httpClient.delete(url)
                if (response.status.isSuccess()) {
                    successMessage = "Turnier erfolgreich gelöscht"
                    loadTurniere()
                    if (selectedTurnier?.number == turnierNumber) {
                        selectedTurnier = null
                    }
                } else {
                    val errorResponse = response.body<ApiResponse>()
                    errorMessage = errorResponse.message ?: "Fehler beim Löschen des Turniers"
                }
            } catch (e: Exception) {
                errorMessage = "Fehler beim Löschen des Turniers: ${e.message}"
            } finally {
                isLoading = false
            }
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
                        isCreating = true
                        isEditing = false
                        selectedTurnier = null
                    },
                    enabled = !isLoading && !isCreating && !isEditing
                ) {
                    Text("+")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Neues Turnier")
                }
            }

            // Messages
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
            }
            if (successMessage != null) {
                Text(
                    text = successMessage!!,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                // Clear success message after 3 seconds
                LaunchedEffect(successMessage) {
                    kotlinx.coroutines.delay(3000)
                    successMessage = null
                }
            }

            // Loading indicator
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // Tournament creation/editing form
            if (isCreating || isEditing) {
                TurnierForm(
                    turnier = if (isEditing) selectedTurnier else null,
                    onSave = { turnier ->
                        if (isEditing) {
                            updateTurnier(turnier)
                        } else {
                            createTurnier(turnier)
                        }
                    },
                    onCancel = {
                        isCreating = false
                        isEditing = false
                    }
                )
            }

            // Tournament list
            if (!isLoading && !isCreating && turniere.isNotEmpty()) {
                Text(
                    text = "Vorhandene Turniere",
                    fontSize = 18.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(turniere) { turnier ->
                        TurnierListItem(
                            turnier = turnier,
                            isSelected = selectedTurnier?.number == turnier.number,
                            onClick = {
                                selectedTurnier = turnier
                                isEditing = false
                                isCreating = false
                            },
                            onEdit = {
                                selectedTurnier = turnier
                                isEditing = true
                                isCreating = false
                            },
                            onDelete = {
                                deleteTurnier(turnier.number)
                            }
                        )
                    }
                }
            } else if (!isLoading && !isCreating && turniere.isEmpty()) {
                Text(
                    text = "Keine Turniere vorhanden",
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        }
    }
}

/**
 * Form for creating or editing a tournament
 */
@Composable
fun TurnierForm(
    turnier: Turnier?,
    onSave: (Turnier) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(turnier?.name ?: "") }
    var datum by remember { mutableStateOf(turnier?.datum ?: "") }
    var number by remember { mutableStateOf(turnier?.number?.toString() ?: "") }
    var bewerbe by remember { mutableStateOf(turnier?.bewerbe ?: emptyList()) }

    // For adding new competitions
    // Calculate next sequential number for new Bewerb
    val nextBewerbNummer = remember(bewerbe) {
        if (bewerbe.isEmpty()) "1" else (bewerbe.maxOf { it.nummer } + 1).toString()
    }

    var newBewerbNummer by remember(nextBewerbNummer) { mutableStateOf(nextBewerbNummer) }
    var newBewerbTitel by remember { mutableStateOf("") }
    var newBewerbKlasse by remember { mutableStateOf("") }
    var newBewerbTask by remember { mutableStateOf("") }

    // For editing existing Bewerbe
    var editingBewerbIndex by remember { mutableStateOf<Int?>(null) }
    var isEditingBewerb by remember { mutableStateOf(false) }

    // Validation for Bewerb form
    var showBewerbNummerError by remember { mutableStateOf(false) }
    var showBewerbTitelError by remember { mutableStateOf(false) }

    // Validation
    val isNameValid = name.isNotBlank()
    val isDatumValid = datum.isNotBlank()
    val isNumberValid = number.isNotBlank() && number.toIntOrNull() != null

    var showNameError by remember { mutableStateOf(false) }
    var showDatumError by remember { mutableStateOf(false) }
    var showNumberError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = if (turnier == null) "Neues Turnier erstellen" else "Turnier bearbeiten",
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Tournament form fields
        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                showNameError = false
            },
            label = { Text("Turnier-Name *") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            isError = showNameError,
            supportingText = {
                if (showNameError) {
                    Text("Bitte geben Sie einen Namen ein")
                }
            }
        )

        OutlinedTextField(
            value = datum,
            onValueChange = {
                datum = it
                showDatumError = false
            },
            label = { Text("Datum *") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            isError = showDatumError,
            supportingText = {
                if (showDatumError) {
                    Text("Bitte geben Sie ein Datum ein")
                } else {
                    Text("Format: DD.MONAT YYYY (z.B. 7.JUNI 2025)")
                }
            }
        )

        OutlinedTextField(
            value = number,
            onValueChange = {
                number = it
                showNumberError = false
            },
            label = { Text("Turnier-Nummer *") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            isError = showNumberError,
            supportingText = {
                if (showNumberError) {
                    Text("Bitte geben Sie eine gültige Nummer ein")
                }
            }
        )

        // Competitions section
        Text(
            text = "Bewerbe",
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Hier können Sie die Bewerbe des Turniers verwalten",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // List of existing competitions
        if (bewerbe.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                bewerbe.forEachIndexed { index, bewerb ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SelectionContainer(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "${bewerb.nummer}. ${bewerb.titel} ${bewerb.klasse}${bewerb.task?.let { " (Aufgabe: $it)" } ?: ""}"
                            )
                        }
                        Button(
                            onClick = {
                                // Set up editing mode for this Bewerb
                                editingBewerbIndex = index
                                isEditingBewerb = true
                                newBewerbNummer = bewerb.nummer.toString()
                                newBewerbTitel = bewerb.titel
                                newBewerbKlasse = bewerb.klasse
                                newBewerbTask = bewerb.task ?: ""
                            },
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text("Bearbeiten")
                        }
                        Button(
                            onClick = {
                                bewerbe = bewerbe.toMutableList().apply {
                                    removeAt(index)
                                }
                            },
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text("Löschen")
                        }
                    }
                    if (index < bewerbe.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            thickness = DividerDefaults.Thickness,
                            color = DividerDefaults.color
                        )
                    }
                }
            }
        } else {
            Text(
                text = "Keine Bewerbe vorhanden",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Add/Edit competition form
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = if (isEditingBewerb) "Bewerb bearbeiten" else "Neuen Bewerb hinzufügen",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = newBewerbNummer,
                    onValueChange = {
                        newBewerbNummer = it
                        showBewerbNummerError = false
                    },
                    label = { Text("Nummer *") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    isError = showBewerbNummerError,
                    supportingText = {
                        if (showBewerbNummerError) {
                            Text("Bitte geben Sie eine gültige Nummer ein")
                        }
                    }
                )

                OutlinedTextField(
                    value = newBewerbTitel,
                    onValueChange = {
                        newBewerbTitel = it
                        showBewerbTitelError = false
                    },
                    label = { Text("Titel *") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    isError = showBewerbTitelError,
                    supportingText = {
                        if (showBewerbTitelError) {
                            Text("Bitte geben Sie einen Titel ein")
                        }
                    }
                )

                OutlinedTextField(
                    value = newBewerbKlasse,
                    onValueChange = { newBewerbKlasse = it },
                    label = { Text("Klasse/Höhe (optional)") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = newBewerbTask,
                    onValueChange = { newBewerbTask = it },
                    label = { Text("Aufgabe (optional)") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (isEditingBewerb) {
                        TextButton(
                            onClick = {
                                // Cancel editing
                                isEditingBewerb = false
                                editingBewerbIndex = null

                                // Reset form with next number
                                newBewerbNummer = nextBewerbNummer
                                newBewerbTitel = ""
                                newBewerbKlasse = ""
                                newBewerbTask = ""
                            },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Abbrechen")
                        }
                    }

                    Button(
                        onClick = {
                            val nummer = newBewerbNummer.toIntOrNull() ?: 0
                            val isNummerValid = nummer > 0
                            val isTitelValid = newBewerbTitel.isNotBlank()

                            showBewerbNummerError = !isNummerValid
                            showBewerbTitelError = !isTitelValid

                            if (isNummerValid && isTitelValid) {
                                val newBewerb = Bewerb(
                                    nummer = nummer,
                                    titel = newBewerbTitel,
                                    klasse = newBewerbKlasse,
                                    task = if (newBewerbTask.isBlank()) null else newBewerbTask
                                )

                                if (isEditingBewerb && editingBewerbIndex != null) {
                                    // Update existing Bewerb
                                    bewerbe = bewerbe.toMutableList().apply {
                                        set(editingBewerbIndex!!, newBewerb)
                                    }
                                    isEditingBewerb = false
                                    editingBewerbIndex = null
                                } else {
                                    // Add new Bewerb
                                    bewerbe = bewerbe + newBewerb
                                }

                                // Reset form with next number for next Bewerb
                                newBewerbNummer = (nummer + 1).toString()
                                newBewerbTitel = ""
                                newBewerbKlasse = ""
                                newBewerbTask = ""
                            }
                        },
                        enabled = newBewerbNummer.toIntOrNull() != null &&
                            newBewerbNummer.toIntOrNull()!! > 0 &&
                            newBewerbTitel.isNotBlank()
                    ) {
                        Text(if (isEditingBewerb) "Speichern" else "Hinzufügen")
                    }
                }
            }
        }

        // Form buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = onCancel,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text("Abbrechen")
            }

            Button(
                onClick = {
                    showNameError = !isNameValid
                    showDatumError = !isDatumValid
                    showNumberError = !isNumberValid

                    if (isNameValid && isDatumValid && isNumberValid) {
                        val turnierToSave = Turnier(
                            name = name,
                            datum = datum,
                            number = number.toInt(),
                            bewerbe = bewerbe
                        )
                        onSave(turnierToSave)
                    }
                }
            ) {
                Text("Speichern")
            }
        }
    }
}

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

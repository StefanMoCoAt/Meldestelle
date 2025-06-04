package at.mocode.ui.components

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.mocode.model.Bewerb
import at.mocode.model.Turnier

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

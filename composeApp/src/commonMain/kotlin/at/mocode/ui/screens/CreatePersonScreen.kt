package at.mocode.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import at.mocode.enums.GeschlechtE
import at.mocode.ui.viewmodel.CreatePersonViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePersonScreen(
    viewModel: CreatePersonViewModel,
    onNavigateBack: () -> Unit
) {
    var showGeschlechtDropdown by remember { mutableStateOf(false) }

    // Handle success navigation
    LaunchedEffect(viewModel.isSuccess) {
        if (viewModel.isSuccess) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Person erstellen") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Error message
            viewModel.errorMessage?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            // Basic Information Section
            Text(
                text = "Grunddaten",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = viewModel.nachname,
                onValueChange = viewModel::updateNachname,
                label = { Text("Nachname *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = viewModel.vorname,
                onValueChange = viewModel::updateVorname,
                label = { Text("Vorname *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = viewModel.titel,
                onValueChange = viewModel::updateTitel,
                label = { Text("Titel") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("z.B. Dr., Ing.") }
            )

            OutlinedTextField(
                value = viewModel.oepsSatzNr,
                onValueChange = viewModel::updateOepsSatzNr,
                label = { Text("OEPS Satznummer") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("6-stellige Nummer") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = viewModel.geburtsdatum,
                onValueChange = viewModel::updateGeburtsdatum,
                label = { Text("Geburtsdatum") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("YYYY-MM-DD") }
            )

            // Gender Dropdown
            ExposedDropdownMenuBox(
                expanded = showGeschlechtDropdown,
                onExpandedChange = { showGeschlechtDropdown = !showGeschlechtDropdown }
            ) {
                OutlinedTextField(
                    value = viewModel.geschlecht?.let {
                        when(it) {
                            GeschlechtE.M -> "Männlich"
                            GeschlechtE.W -> "Weiblich"
                            GeschlechtE.D -> "Divers"
                            GeschlechtE.UNBEKANNT -> "Unbekannt"
                        }
                    } ?: "",
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Geschlecht") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showGeschlechtDropdown) },
                    modifier = Modifier
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = showGeschlechtDropdown,
                    onDismissRequest = { showGeschlechtDropdown = false }
                ) {
                    GeschlechtE.entries.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(when(option) {
                                    GeschlechtE.M -> "Männlich"
                                    GeschlechtE.W -> "Weiblich"
                                    GeschlechtE.D -> "Divers"
                                    GeschlechtE.UNBEKANNT -> "Unbekannt"
                                })
                            },
                            onClick = {
                                viewModel.updateGeschlecht(option)
                                showGeschlechtDropdown = false
                            }
                        )
                    }
                }
            }

            // Contact Information Section
            Text(
                text = "Kontaktdaten",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = viewModel.telefon,
                onValueChange = viewModel::updateTelefon,
                label = { Text("Telefon") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            OutlinedTextField(
                value = viewModel.email,
                onValueChange = viewModel::updateEmail,
                label = { Text("E-Mail") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            // Address Section
            Text(
                text = "Adresse",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = viewModel.strasse,
                onValueChange = viewModel::updateStrasse,
                label = { Text("Straße und Hausnummer") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = viewModel.plz,
                    onValueChange = viewModel::updatePlz,
                    label = { Text("PLZ") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = viewModel.ort,
                    onValueChange = viewModel::updateOrt,
                    label = { Text("Ort") },
                    modifier = Modifier.weight(2f),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = viewModel.adresszusatz,
                onValueChange = viewModel::updateAdresszusatz,
                label = { Text("Adresszusatz") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Additional Information Section
            Text(
                text = "Weitere Informationen",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = viewModel.feiId,
                onValueChange = viewModel::updateFeiId,
                label = { Text("FEI ID") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = viewModel.mitgliedsNummer,
                onValueChange = viewModel::updateMitgliedsNummer,
                label = { Text("Mitgliedsnummer beim Stammverein") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = viewModel.istGesperrt,
                    onCheckedChange = viewModel::updateIstGesperrt
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Person ist gesperrt")
            }

            if (viewModel.istGesperrt) {
                OutlinedTextField(
                    value = viewModel.sperrGrund,
                    onValueChange = viewModel::updateSperrGrund,
                    label = { Text("Sperrgrund") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }

            OutlinedTextField(
                value = viewModel.notizen,
                onValueChange = viewModel::updateNotizen,
                label = { Text("Interne Notizen") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 4
            )

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.weight(1f),
                    enabled = !viewModel.isLoading
                ) {
                    Text("Abbrechen")
                }

                Button(
                    onClick = {
                        viewModel.createPerson()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !viewModel.isLoading
                ) {
                    if (viewModel.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Erstellen")
                    }
                }
            }
        }
    }
}

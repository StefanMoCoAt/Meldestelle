package at.mocode

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.mocode.model.Nennung
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    val focusManager = LocalFocusManager.current
    val riderNameFocus = remember { FocusRequester() }
    val horseNameFocus = remember { FocusRequester() }
    val emailFocus = remember { FocusRequester() }
    val phoneFocus = remember { FocusRequester() }
    val commentsFocus = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()

    // Function to handle focus navigation to the next field
    fun navigateToNextField(nextFocus: FocusRequester?) {
        nextFocus?.let {
            coroutineScope.launch {
                it.requestFocus()
            }
        }
    }


    MaterialTheme {
        var formSubmitted by remember { mutableStateOf(false) }
        var riderName by remember { mutableStateOf("") }
        var horseName by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var comments by remember { mutableStateOf("") }
        var submittedData by remember { mutableStateOf<Nennung?>(null) }
        var isLoading by remember { mutableStateOf(false) } // Für Ladezustand
        var errorMessage by remember { mutableStateOf<String?>(null) } // Für Fehlermeldungen
        var successMessage by remember { mutableStateOf<String?>(null) } // Für Erfolgsmeldungen

        val events = listOf(
            "1 Pony Stilspringprüfung 60 cm",
            "2 Stilspringprüfung 60 cm",
            "3 Pony Stilspringprüfung 70 cm",
            "4 Stilspringprüfung 80 cm",
            "5 Pony Stilspringprüfung 95 cm",
            "6 Stilspringprüfung 95 cm",
            "7 Einlaufspringprüfung 95cm",
            "8 Springpferdeprüfung 105 cm",
            "9 Stilspringprüfung 105 cm",
            "10 Standardspringprüfung 105cm"
        )
        val selectedEvents = remember { mutableStateListOf<String>() }

        val isRiderNameValid = riderName.isNotBlank()
        val isHorseNameValid = horseName.isNotBlank()
        val isEmailValid = email.isBlank() || isValidEmail(email)
        val isContactValid = email.isNotBlank() || phone.isNotBlank()
        val isEventsValid = selectedEvents.isNotEmpty()
        val isFormValid = isRiderNameValid && isHorseNameValid && isContactValid && isEventsValid && isEmailValid

        var showRiderNameError by remember { mutableStateOf(false) }
        var showHorseNameError by remember { mutableStateOf(false) }
        var showContactError by remember { mutableStateOf(false) }
        var showEmailError by remember { mutableStateOf(false) }
        var showEventsError by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .safeContentPadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 800.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!formSubmitted) {
                    Text(
                        text = "Online-Nennen",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "CSN-C NEU CSNP-C NEU NEUMARKT/M., OÖ",
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Text("7.JUNI 2025", textAlign = TextAlign.Center)
                            Text("Turnier-Nr.: 25319", textAlign = TextAlign.Center)
                        }
                    }

                    errorMessage?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    successMessage?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    OutlinedTextField(
                        value = riderName,
                        onValueChange = {
                            riderName = it
                            if (showRiderNameError) showRiderNameError = false
                            errorMessage = null
                        },
                        label = { Text("Reiter-Name *") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .focusRequester(riderNameFocus),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(
                            onNext = {
                                navigateToNextField(horseNameFocus)
                            }
                        ),
                        isError = showRiderNameError,
                        supportingText = {
                            if (showRiderNameError) {
                                Text("Bitte geben Sie einen Namen ein.")
                            }
                        }
                    )
                    OutlinedTextField(
                        value = horseName,
                        onValueChange = {
                            horseName = it
                            if (showHorseNameError) showHorseNameError = false
                            errorMessage = null
                        },
                        label = { Text("Pferde-Name/Kopf-Nummer *") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .focusRequester(horseNameFocus),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(
                            onNext = {
                                navigateToNextField(emailFocus)
                            }
                        ),
                        isError = showHorseNameError,
                        supportingText = {
                            if (showHorseNameError) {
                                Text("Bitte geben Sie den Namen oder die Kopf-Nummer des Pferdes ein.")
                            }
                        }
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            if (email.isNotBlank() || phone.isNotBlank()) showContactError = false
                            if (email.isBlank() || isValidEmail(email)) showEmailError = false
                            errorMessage = null
                        },
                        label = { Text("E-Mail *") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .focusRequester(emailFocus),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(
                            onNext = {
                                navigateToNextField(phoneFocus)
                            }
                        ),
                        isError = showContactError || showEmailError,
                        supportingText = {
                            when {
                                showEmailError -> Text("Bitte geben Sie eine gültige E-Mail-Adresse ein")
                                showContactError -> Text("Bitte geben Sie entweder eine E-Mail-Adresse oder eine Telefonnummer ein")
                            }
                        }
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = {
                            phone = it
                            if (email.isNotBlank() || phone.isNotBlank()) showContactError = false
                            if (email.isBlank() || isValidEmail(email)) showEmailError = false
                            errorMessage = null
                        },
                        label = { Text("Telefon-Nummer") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .focusRequester(phoneFocus),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(
                            onNext = {
                                navigateToNextField(commentsFocus)
                            }
                        ),
                        isError = showContactError,
                        supportingText = {
                            if (showContactError) {
                                Text("Bitte geben Sie entweder eine E-Mail-Adresse oder eine Telefonnummer ein")
                            }
                        }
                    )

                    Text(
                        "Bewerbe (mindestens einen auswählen):",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectableGroup()
                            .padding(bottom = 8.dp)
                    ) {
                        events.forEach { event ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .selectable(
                                        selected = selectedEvents.contains(event),
                                        onClick = {
                                            if (selectedEvents.contains(event)) selectedEvents.remove(event) else selectedEvents.add(
                                                event
                                            )
                                            showEventsError = false
                                            errorMessage = null
                                        }
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selectedEvents.contains(event),
                                    onCheckedChange = { checked ->
                                        if (checked) selectedEvents.add(event) else selectedEvents.remove(event)
                                        showEventsError = false
                                        errorMessage = null
                                    }
                                )
                                Text(
                                    text = event,
                                    modifier = Modifier
                                        .padding(start = 8.dp)
                                )
                            }
                        }
                        if (showEventsError) Text(
                            "Bitte wählen Sie mindestens einen Bewerb aus",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )

                    }

                    OutlinedTextField(
                        value = comments,
                        onValueChange = {
                            comments = it
                            errorMessage = null
                        },
                        label = { Text("Wünsche/Bemerkungen") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .padding(bottom = 16.dp)
                            .focusRequester(commentsFocus),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                coroutineScope.launch {
                                    focusManager.clearFocus()
                                }
                            }
                        ),
                        minLines = 3
                    )

                    Button(
                        onClick = {
                            // Validiere alle Felder und zeige Fehler an
                            showRiderNameError = !isRiderNameValid
                            showHorseNameError = !isHorseNameValid
                            showContactError = !isContactValid
                            showEventsError = !isEventsValid
                            showEmailError = email.isNotBlank() && !isValidEmail(email)

                            errorMessage = null // Reset error message on new submission attempt
                            successMessage = null

                            // Prüfe, ob das Formular gültig ist
                            val isFormValid = isRiderNameValid && isHorseNameValid && isContactValid &&
                                isEventsValid && (email.isBlank() || isValidEmail(email))

                            if (isFormValid) {
                                isLoading = true // Start loading
                                val formData = Nennung(
                                    riderName = riderName,
                                    horseName = horseName,
                                    email = email,
                                    phone = phone,
                                    selectedEvents = selectedEvents.toList(),
                                    comments = comments
                                )

                                // Speichere die Daten für die Bestätigungsseite
                                submittedData = formData

                                coroutineScope.launch {
                                    try {
                                        // Verwende den vollständigen Pfad für die API-Anfrage
                                        val response = httpClient.post("http://localhost:8081/api/nennung") {
                                            contentType(ContentType.Application.Json)
                                            setBody(formData)
                                        }
                                        if (response.status.isSuccess()) {
                                            // Setze formSubmitted auf true, um zur Bestätigungsseite zu wechseln
                                            formSubmitted = true
                                        } else {
                                            // Fehlerbehandlung für Server-Fehler
                                            val errorBody = response.toString()
                                            println("Fehler vom Server: ${response.status}, $errorBody")
                                            errorMessage =
                                                "Fehler beim Senden der Nennung (Server: ${response.status}). Bitte versuchen Sie es später erneut."
                                        }
                                    } catch (e: Exception) {
                                        // Fehlerbehandlung für Netzwerk- oder Client-Fehler
                                        println("Netzwerk- oder Client-Fehler: ${e.message}")
                                        errorMessage =
                                            "Netzwerkfehler: ${e.message}. Bitte überprüfen Sie Ihre Internetverbindung."
                                    } finally {
                                        isLoading = false // Stop loading
                                    }
                                }
                            } else {
                                // Zeige eine allgemeine Fehlermeldung an
                                errorMessage =
                                    "Bitte füllen Sie alle Pflichtfelder (*) korrekt aus und wählen Sie mindestens einen Bewerb."
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        enabled = !isLoading // Button deaktivieren während Ladevorgang
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Jetzt Nennen")
                        }
                    }

                } else {
                    // formSubmitted == true
                    // Bestätigungsseite
                    Text(
                        "Vielen Dank für Ihre Nennung für das Turnier",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Turnier-Informationen
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "CSN-C NEU CSNP-C NEU NEUMARKT/M., OÖ",
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Text("7.JUNI 2025", textAlign = TextAlign.Center)
                            Text("Turnier-Nr.: 25319", textAlign = TextAlign.Center)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Zusammenfassung der gesendeten Daten
                    submittedData?.let { data ->
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
                                    "Reiter: ${data.riderName}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )

                                Text(
                                    "Pferd: ${data.horseName}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )

                                if (data.email.isNotBlank()) {
                                    Text(
                                        "E-Mail: ${data.email}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }

                                if (data.phone.isNotBlank()) {
                                    Text(
                                        "Telefon: ${data.phone}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }

                                Text(
                                    "Bewerbe:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(bottom = 4.dp, top = 8.dp)
                                )

                                data.selectedEvents.forEach { event ->
                                    Text(
                                        "• $event",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(start = 16.dp, bottom = 2.dp)
                                    )
                                }

                                if (data.comments.isNotBlank()) {
                                    Text(
                                        "Bemerkungen:",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(bottom = 4.dp, top = 8.dp)
                                    )
                                    Text(
                                        data.comments,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(start = 16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Button für weitere Nennung
                    Button(
                        onClick = {
                            // Zurück zum Formular
                            formSubmitted = false

                            // Formular zurücksetzen
                            riderName = ""
                            horseName = ""
                            email = ""
                            phone = ""
                            selectedEvents.clear()
                            comments = ""

                            // Fehlermeldungen zurücksetzen
                            errorMessage = null
                            successMessage = null
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                    ) {
                        Text("Weitere Nennung abgeben")
                    }
                }

            }

        }

    }
}

fun isValidEmail(email: String): Boolean {
    if (email.isBlank()) return false
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    return email.matches(emailRegex.toRegex())
}

// Ktor HTTP Client Instanz (global für diese Datei oder per DI bereitstellen)
val httpClient = HttpClient {
    install(ContentNegotiation) {
        json() // Verwendet kotlinx.serialization
    }

    // Konfiguriere die Engine für absolute URLs
    engine {
        // Für WASM/JS-Client
        request {
            // Setze die Basis-URL für alle Anfragen
            url {
                protocol = URLProtocol.HTTP
                // Verwende den Host aus der Konstante
                // In Constants.kt kann der Wert je nach Umgebung angepasst werden
                host = API_HOST
                port = SERVER_PORT // Verwende den Port aus Constants.kt
            }
        }
    }
}

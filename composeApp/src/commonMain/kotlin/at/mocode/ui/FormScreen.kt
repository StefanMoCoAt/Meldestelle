package at.mocode.ui

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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.mocode.model.ApiResponse
import at.mocode.model.Nennung
import at.mocode.network.httpClient
import at.mocode.util.isValidEmail
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.launch

/**
 * Form screen for submitting a "Nennung" (registration/entry)
 *
 * @param onFormSubmitted Callback function called when the form is successfully submitted
 * @param onSubmittedDataReceived Callback function called with the submitted data
 */
@Composable
fun FormScreen(
    onFormSubmitted: () -> Unit,
    onSubmittedDataReceived: (Nennung) -> Unit
) {
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

    var riderName by remember { mutableStateOf("") }
    var horseName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var comments by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

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

    Column(
        modifier = Modifier
            .widthIn(max = 800.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Online-Nennen",
            fontSize = 24.sp,
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
                // Validate all fields and show errors
                showRiderNameError = !isRiderNameValid
                showHorseNameError = !isHorseNameValid
                showContactError = !isContactValid
                showEventsError = !isEventsValid
                showEmailError = email.isNotBlank() && !isValidEmail(email)

                errorMessage = null // Reset error message on new submission attempt
                successMessage = null

                // Check if the form is valid
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

                    // Save the data for the confirmation page
                    onSubmittedDataReceived(formData)

                    coroutineScope.launch {
                        try {
                            // Construct the full URL with the platform-specific host and port
                            val url = "http://${at.mocode.PlatformInfo.apiHost}:${at.mocode.SERVER_PORT}/api/nennung"
                            val response = httpClient.post(url) {
                                contentType(ContentType.Application.Json)
                                setBody(formData)
                            }
                            if (response.status.isSuccess()) {
                                // Set formSubmitted to true to switch to the confirmation page
                                onFormSubmitted()
                            } else {
                                // Error handling for server errors - try to parse structured response
                                try {
                                    val errorResponse = response.body<ApiResponse>()
                                    println("Structured error response from server: $errorResponse")
                                    errorMessage = errorResponse.message
                                } catch (parseError: Exception) {
                                    // Fallback if no structured response can be parsed
                                    println("Could not parse error response: ${parseError.message}")
                                    errorMessage = "Error sending the entry (Server: ${response.status}). Please try again later."
                                }
                            }
                        } catch (e: Exception) {
                            // Error handling for network or client errors
                            println("Network or client error: ${e.message}")
                            // Create a user-friendly error message without technical details
                            errorMessage = "Connection error when sending the entry. " +
                                "Please check your internet connection and try again later."
                        } finally {
                            isLoading = false // Stop loading
                        }
                    }
                } else {
                    // Show a general error message
                    errorMessage =
                        "Please fill in all required fields (*) correctly and select at least one event."
                }
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            enabled = !isLoading // Disable button during loading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Text("Submit Entry")
            }
        }
    }
}

package at.mocode

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import at.mocode.model.Nennung
import at.mocode.ui.ConfirmationScreen
import at.mocode.ui.FormScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Main application composable
 */
@Composable
@Preview
fun App() {
    MaterialTheme {
        var formSubmitted by remember { mutableStateOf(false) }
        var submittedData by remember { mutableStateOf<Nennung?>(null) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .safeContentPadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            if (!formSubmitted) {
                FormScreen(
                    onFormSubmitted = { formSubmitted = true },
                    onSubmittedDataReceived = { submittedData = it }
                )
            } else {
                submittedData?.let { data ->
                    ConfirmationScreen(
                        submittedData = data,
                        onNewSubmission = {
                            formSubmitted = false
                            submittedData = null
                        }
                    )
                }
            }
        }
    }
}

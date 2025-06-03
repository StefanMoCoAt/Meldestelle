package at.mocode

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import at.mocode.model.Nennung
import at.mocode.model.Turnier
import at.mocode.ui.AdminTournamentScreen
import at.mocode.ui.ConfirmationScreen
import at.mocode.ui.FormScreen
import at.mocode.ui.TurnierListScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Main application composable
 */
@Composable
@Preview
fun App() {
    MaterialTheme {
        // Navigation state
        var currentScreen by remember { mutableStateOf<Screen>(Screen.TurnierList) }
        var selectedTurnier by remember { mutableStateOf<Turnier?>(null) }
        var submittedData by remember { mutableStateOf<Nennung?>(null) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .safeContentPadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            when (val screen = currentScreen) {
                is Screen.TurnierList -> {
                    TurnierListScreen(
                        onTurnierSelected = { turnier ->
                            selectedTurnier = turnier
                            currentScreen = Screen.Form
                        },
                        onAdminClicked = {
                            currentScreen = Screen.AdminTournament
                        }
                    )
                }
                is Screen.Form -> {
                    selectedTurnier?.let { turnier ->
                        FormScreen(
                            turnier = turnier,
                            onFormSubmitted = {
                                currentScreen = Screen.Confirmation
                            },
                            onSubmittedDataReceived = {
                                submittedData = it
                            },
                            onBackClicked = {
                                currentScreen = Screen.TurnierList
                            }
                        )
                    } ?: run {
                        // Fallback if no tournament is selected
                        currentScreen = Screen.TurnierList
                    }
                }
                is Screen.Confirmation -> {
                    submittedData?.let { data ->
                        ConfirmationScreen(
                            submittedData = data,
                            onNewSubmission = {
                                // Keep the same tournament selected for another submission
                                currentScreen = Screen.Form
                            }
                        )
                    } ?: run {
                        // Fallback if no data is submitted
                        currentScreen = Screen.TurnierList
                    }
                }
                is Screen.AdminTournament -> {
                    AdminTournamentScreen(
                        onBackClicked = {
                            currentScreen = Screen.TurnierList
                        }
                    )
                }
            }
        }
    }
}

/**
 * Sealed class representing the different screens in the application
 */
sealed class Screen {
    object TurnierList : Screen()
    object Form : Screen()
    object Confirmation : Screen()
    object AdminTournament : Screen()
}

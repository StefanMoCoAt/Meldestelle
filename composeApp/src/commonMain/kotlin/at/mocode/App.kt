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
 * Main application composable that serves as the entry point for the UI.
 *
 * This composable handles the navigation between different screens:
 * - Tournament list screen (initial screen)
 * - Registration form screen
 * - Confirmation screen
 * - Admin tournament management screen
 *
 * It maintains the navigation state and passes data between screens.
 */
@Composable
@Preview
fun App() {
    MaterialTheme {
        // Navigation state
        var currentScreen by remember { mutableStateOf<Screen>(Screen.TurnierList) }
        var selectedTurnier by remember { mutableStateOf<Turnier?>(null) }
        var submittedData by remember { mutableStateOf<Nennung?>(null) }

        // Navigation functions
        val navigateToTurnierList = {
            currentScreen = Screen.TurnierList
        }

        val navigateToForm = { turnier: Turnier? ->
            if (turnier != null) {
                selectedTurnier = turnier
            }
            currentScreen = Screen.Form
        }

        val navigateToConfirmation = {
            currentScreen = Screen.Confirmation
        }

        val navigateToAdmin = {
            currentScreen = Screen.AdminTournament
        }

        val updateSubmittedData = { data: Nennung ->
            submittedData = data
        }

        // Main content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .safeContentPadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            // Render the current screen
            when (val screen = currentScreen) {
                is Screen.TurnierList -> {
                    TurnierListScreen(
                        onTurnierSelected = { turnier ->
                            navigateToForm(turnier)
                        },
                        onAdminClicked = {
                            navigateToAdmin()
                        }
                    )
                }
                is Screen.Form -> {
                    // Show form screen if a tournament is selected, otherwise go back to list
                    selectedTurnier?.let { turnier ->
                        FormScreen(
                            turnier = turnier,
                            onFormSubmitted = {
                                navigateToConfirmation()
                            },
                            onSubmittedDataReceived = { data ->
                                updateSubmittedData(data)
                            },
                            onBackClicked = {
                                navigateToTurnierList()
                            }
                        )
                    } ?: run {
                        // Fallback if no tournament is selected
                        navigateToTurnierList()
                    }
                }
                is Screen.Confirmation -> {
                    // Show confirmation screen if data is submitted, otherwise go back to list
                    submittedData?.let { data ->
                        ConfirmationScreen(
                            submittedData = data,
                            onNewSubmission = {
                                // Keep the same tournament selected for another submission
                                navigateToForm(null)
                            }
                        )
                    } ?: run {
                        // Fallback if no data is submitted
                        navigateToTurnierList()
                    }
                }
                is Screen.AdminTournament -> {
                    AdminTournamentScreen(
                        onBackClicked = {
                            navigateToTurnierList()
                        }
                    )
                }
            }
        }
    }
}

/**
 * Sealed class representing the different screens in the application.
 * This is used for type-safe navigation between screens.
 */
sealed class Screen {
    /**
     * The tournament list screen.
     * This is the initial screen showing all available tournaments.
     */
    object TurnierList : Screen()

    /**
     * The registration form screen.
     * Displayed when a user selects a tournament to register for.
     */
    object Form : Screen()

    /**
     * The confirmation screen.
     * Shown after a successful form submission.
     */
    object Confirmation : Screen()

    /**
     * The admin tournament management screen.
     * Provides CRUD operations for tournaments.
     */
    object AdminTournament : Screen()
}

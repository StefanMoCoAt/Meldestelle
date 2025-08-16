package at.mocode.client.web

import androidx.compose.runtime.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.renderComposable
import at.mocode.client.data.service.PingService
import at.mocode.client.ui.viewmodel.PingViewModel
import at.mocode.client.ui.viewmodel.PingUiState

fun main() {
    // Catch any initialization errors and display user-friendly error
    try {
        renderComposable(rootElementId = "root") {
            Style(AppStylesheet)
            MeldestelleWebApp()
        }
    } catch (e: Exception) {
        console.error("Failed to initialize Meldestelle Web App", e)
        // Fallback error display
        val rootElement = js("document.getElementById('root')")
        if (rootElement != null) {
            val errorHtml = """
                <div style="display: flex; justify-content: center; align-items: center; height: 100vh; flex-direction: column; font-family: system-ui;">
                    <h1 style="color: #c62828; margin-bottom: 16px;">⚠️ Fehler beim Laden</h1>
                    <p style="color: #666; text-align: center;">Die Anwendung konnte nicht geladen werden.<br>Bitte laden Sie die Seite neu oder kontaktieren Sie den Support.</p>
                    <button onclick="window.location.reload()" style="margin-top: 20px; padding: 10px 20px; background: #1976d2; color: white; border: none; border-radius: 4px; cursor: pointer;">Seite neu laden</button>
                </div>
            """.trimIndent()
            js("rootElement.innerHTML = errorHtml")
        }
    }
}

@Composable
fun MeldestelleWebApp() {
    // Get baseUrl from window location with error handling
    val baseUrl = remember {
        try {
            js("window.location.origin").toString().ifEmpty { "http://localhost:8080" }
        } catch (e: Exception) {
            console.warn("Could not get window location, using default", e)
            "http://localhost:8080"
        }
    }

    // Create services with proper error handling
    val pingService = remember(baseUrl) {
        try {
            PingService(baseUrl)
        } catch (e: Exception) {
            console.error("Failed to create PingService", e)
            throw e
        }
    }

    val viewModel = remember(pingService) {
        try {
            PingViewModel(pingService)
        } catch (e: Exception) {
            console.error("Failed to create PingViewModel", e)
            throw e
        }
    }

    // Ensure proper cleanup on component disposal
    DisposableEffect(viewModel) {
        onDispose {
            try {
                viewModel.dispose()
            } catch (e: Exception) {
                console.warn("Error during ViewModel disposal", e)
            }
        }
    }

    Div(attrs = {
        classes(AppStylesheet.container)
    }) {
        Header(attrs = { classes(AppStylesheet.header) }) {
            H1 { Text("Meldestelle Web App") }
        }

        Main(attrs = { classes(AppStylesheet.main) }) {
            PingTestWebView(
                state = viewModel.uiState,
                onTestConnection = { viewModel.pingBackend() }
            )
        }

        Footer(attrs = { classes(AppStylesheet.footer) }) {
            P { Text("© 2025 Meldestelle - Powered by Kotlin Multiplatform") }
        }
    }
}

@Composable
fun PingTestWebView(
    state: PingUiState,
    onTestConnection: () -> Unit
) {
    Div(attrs = { classes(AppStylesheet.card) }) {
        H2 { Text("Backend Verbindungstest") }

        Button(
            attrs = {
                classes(AppStylesheet.button, AppStylesheet.primaryButton)
                if (state is PingUiState.Loading) {
                    attr("disabled", "")
                }
                onClick { onTestConnection() }
            }
        ) {
            if (state is PingUiState.Loading) {
                Span(attrs = { classes(AppStylesheet.spinner) }) {}
                Text(" Pinge Backend...")
            } else {
                Text("Ping Backend")
            }
        }

        // Status display with four distinct states
        Div {
            when (state) {
                is PingUiState.Initial -> {
                    Div {
                        Text("Klicke auf den Button, um das Backend zu testen")
                    }
                }
                is PingUiState.Loading -> {
                    Div {
                        Span(attrs = { classes(AppStylesheet.spinner) }) {}
                        Text(" Pinge Backend ...")
                    }
                }
                is PingUiState.Success -> {
                    Div(attrs = { classes(AppStylesheet.successMessage) }) {
                        Span { Text("✅ ") }
                        Text("Antwort vom Backend: ${state.response.status}")
                    }
                }
                is PingUiState.Error -> {
                    Div(attrs = { classes(AppStylesheet.errorMessage) }) {
                        Span { Text("❌ ") }
                        Text("Fehler: ${state.message}")
                    }
                }
            }
        }
    }
}

package at.mocode.client.web

import androidx.compose.runtime.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.renderComposable
import at.mocode.client.ui.components.PingTestComponent

fun main() {
    renderComposable(rootElementId = "root") {
        Style(AppStylesheet)
        MeldestelleWebApp()
    }
}

@Composable
fun MeldestelleWebApp() {
    // Get baseUrl from a window location or use default
    val baseUrl = remember {
        js("window.location.origin").toString().ifEmpty { "http://localhost:8080" }
    }
    val pingComponent = remember { PingTestComponent(baseUrl) }
    var pingState by remember { mutableStateOf(pingComponent.state) }

    LaunchedEffect(pingComponent) {
        pingComponent.onStateChanged = { newState ->
            pingState = newState
        }
    }

    DisposableEffect(pingComponent) {
        onDispose {
            pingComponent.dispose()
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
                state = pingState,
                onTestConnection = { pingComponent.testConnection() }
            )
        }

        Footer(attrs = { classes(AppStylesheet.footer) }) {
            P { Text("© 2025 Meldestelle - Powered by Kotlin Multiplatform") }
        }
    }
}

@Composable
fun PingTestWebView(
    state: at.mocode.client.ui.components.PingTestState,
    onTestConnection: () -> Unit
) {
    Div(attrs = { classes(AppStylesheet.card) }) {
        H2 { Text("Backend Verbindungstest") }

        Button(
            attrs = {
                classes(AppStylesheet.button, AppStylesheet.primaryButton)
                if (state.isLoading) {
                    attr("disabled", "")
                }
                onClick { onTestConnection() }
            }
        ) {
            if (state.isLoading) {
                Span(attrs = { classes(AppStylesheet.spinner) }) {}
                Text(" Testing...")
            } else {
                Text("Ping Backend Service")
            }
        }

        // Status Anzeige
        when {
            state.isConnected -> {
                Div(attrs = { classes(AppStylesheet.successMessage) }) {
                    Span { Text("✅ ") }
                    Text("Verbindung erfolgreich: ${state.response?.status}")
                }
            }

            state.error != null -> {
                Div(attrs = { classes(AppStylesheet.errorMessage) }) {
                    Span { Text("❌ ") }
                    Text("Fehler: ${state.error}")
                }
            }
        }
    }
}

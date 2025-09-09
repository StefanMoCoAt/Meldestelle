package at.mocode.client.kobweb.pages

import androidx.compose.runtime.*
import at.mocode.client.data.service.PingService
import at.mocode.client.ui.viewmodel.PingUiState
import at.mocode.client.ui.viewmodel.PingViewModel
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Spacer
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.modifiers.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.css.rgb
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Text
import at.mocode.client.kobweb.config.AppConfig
import at.mocode.client.kobweb.di.ServiceProvider
import at.mocode.client.kobweb.components.LoadingIndicator

@Page
@Composable
fun HomePage() {
    // Use dependency injection for better service management
    val viewModel = remember { ServiceProvider.createPingViewModel() }

    // Proper lifecycle management with ServiceProvider cleanup
    DisposableEffect(viewModel) {
        onDispose {
            ServiceProvider.cleanupViewModel(viewModel)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.px)
    ) {
        H1 {
            Text(AppConfig.APP_TITLE)
        }

        Spacer()

        H1 {
            Text("Ping Backend Service")
        }

        Spacer()

        // Status display area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.px),
            contentAlignment = Alignment.Center
        ) {
            when (val state = viewModel.uiState) {
                is PingUiState.Initial -> {
                    SpanText(
                        text = "Klicke auf den Button, um das Backend zu testen",
                        modifier = Modifier.color(rgb(0, 0, 0))
                    )
                }
                is PingUiState.Loading -> {
                    LoadingIndicator(
                        message = "Pinge Backend",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                is PingUiState.Success -> {
                    SpanText(
                        text = "Antwort vom Backend: ${state.response.status}",
                        modifier = Modifier.color(rgb(0, 150, 0))
                    )
                }
                is PingUiState.Error -> {
                    SpanText(
                        text = "Fehler: ${state.message}",
                        modifier = Modifier.color(rgb(180, 0, 0))
                    )
                }
            }
        }

        Spacer()

        Button(
            onClick = { viewModel.pingBackend() },
            enabled = viewModel.uiState !is PingUiState.Loading
        ) {
            SpanText("Ping Backend")
        }
    }
}

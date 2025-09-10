package at.mocode

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class PingResponse(
    val status: String,
    val timestamp: String? = null,
    val message: String? = null
)

sealed class PingState {
    object Idle : PingState()
    object Loading : PingState()
    data class Success(val response: PingResponse) : PingState()
    data class Error(val message: String) : PingState()
}

@Composable
@Preview
fun App() {
    MaterialTheme {
        var showContent by remember { mutableStateOf(false) }
        var pingState by remember { mutableStateOf<PingState>(PingState.Idle) }
        val coroutineScope = rememberCoroutineScope()

        // Create HTTP client
        val httpClient = remember {
            HttpClient {
                install(ContentNegotiation) {
                    json()
                }
            }
        }

        // Cleanup client on disposal
        DisposableEffect(Unit) {
            onDispose {
                httpClient.close()
            }
        }

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Meldestelle",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 32.dp, bottom = 16.dp)
            )

            // Platform Info Button
            Button(
                onClick = { showContent = !showContent },
                modifier = Modifier.padding(16.dp)
            ) {
                Text(if (showContent) "Platform-Info ausblenden" else "Platform-Info anzeigen")
            }

            // Ping Backend Button
            Button(
                onClick = {
                    coroutineScope.launch {
                        pingState = PingState.Loading
                        try {
                            val response: PingResponse = httpClient.get("http://localhost:8082/ping").body()
                            pingState = PingState.Success(response)
                        } catch (e: Exception) {
                            pingState = PingState.Error(e.message ?: "Unknown error occurred")
                        }
                    }
                },
                enabled = pingState !is PingState.Loading,
                modifier = Modifier.padding(8.dp)
            ) {
                if (pingState is PingState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Text("Ping Backend")
            }

            // Ping Status Display
            when (val state = pingState) {
                is PingState.Success -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "✅ Ping erfolgreich!",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF4CAF50)
                            )
                            Text(
                                text = "Status: ${state.response.status}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            state.response.timestamp?.let {
                                Text(
                                    text = "Zeit: $it",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }
                is PingState.Error -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "❌ Ping fehlgeschlagen",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFFF44336)
                            )
                            Text(
                                text = "Fehler: ${state.message}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
                else -> {
                    // Idle or Loading state - no additional display needed
                }
            }

            AnimatedVisibility(showContent) {
                val greeting = remember { Greeting().greet() }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(8.dp)
                    )
                    Text(
                        text = "Willkommen in der Meldestelle-Anwendung!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

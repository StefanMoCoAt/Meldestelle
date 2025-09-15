package at.mocode.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import at.mocode.ApiConfig

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

/**
 * Lazy-loadable Ping Service Component
 * Encapsulates HTTP client, state management, and ping functionality
 * This component is only fully initialized when first used
 */
@Composable
fun PingServiceComponent(
    modifier: Modifier = Modifier,
    onStateChange: (PingState) -> Unit = {}
) {
    var pingState by remember { mutableStateOf<PingState>(PingState.Idle) }
    val coroutineScope = rememberCoroutineScope()

    // Lazy HTTP client - only created when component is first composed
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

    // Notify parent of state changes
    LaunchedEffect(pingState) {
        onStateChange(pingState)
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Ping Backend Button
        Button(
            onClick = {
                coroutineScope.launch {
                    pingState = PingState.Loading
                    try {
                        // Konfigurierbare API-URL basierend auf Deployment-Umgebung
                        val response: PingResponse = httpClient.get(ApiConfig.pingEndpoint).body()
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

        // Status Display - conditionally rendered
        when (val state = pingState) {
            is PingState.Success -> {
                SuccessCard(
                    response = state.response,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            is PingState.Error -> {
                ErrorCard(
                    message = state.message,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            else -> {
                // Idle or Loading state - no additional display needed
            }
        }
    }
}

package at.mocode.client.ui.components

import at.mocode.client.data.service.PingService
import at.mocode.client.data.service.PingResponse
import kotlinx.coroutines.*

data class PingTestState(
    val isLoading: Boolean = false,
    val response: PingResponse? = null,
    val error: String? = null,
    val isConnected: Boolean = false
)

class PingTestComponent(baseUrl: String = "http://localhost:8080") {
    private val pingService = PingService(baseUrl)
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    var state: PingTestState = PingTestState()
        private set

    var onStateChanged: ((PingTestState) -> Unit)? = null

    fun testConnection() {
        updateState(state.copy(isLoading = true, error = null))

        scope.launch {
            pingService.ping()
                .onSuccess { response ->
                    updateState(
                        state.copy(
                            isLoading = false,
                            response = response,
                            isConnected = response.status == "pong"
                        )
                    )
                }
                .onFailure { error ->
                    updateState(
                        state.copy(
                            isLoading = false,
                            error = error.message ?: "Unbekannter Fehler",
                            isConnected = false
                        )
                    )
                }
        }
    }

    private fun updateState(newState: PingTestState) {
        state = newState
        onStateChanged?.invoke(state)
    }

    fun dispose() {
        scope.cancel()
    }
}

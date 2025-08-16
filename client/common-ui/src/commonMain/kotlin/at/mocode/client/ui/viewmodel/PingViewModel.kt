package at.mocode.client.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import at.mocode.client.data.service.PingService
import at.mocode.client.data.service.PingResponse
import kotlinx.coroutines.*

/**
 * Represents the four distinct UI states as defined in the trace-bullet-guideline.md
 */
sealed class PingUiState {
    /** Initial state: neutral message, button active */
    data object Initial : PingUiState()

    /** Loading state: loading message, button disabled */
    data object Loading : PingUiState()

    /** Success state: positive response, button active */
    data class Success(val response: PingResponse) : PingUiState()

    /** Error state: clear error message, button active */
    data class Error(val message: String) : PingUiState()
}

class PingViewModel(
    private val pingService: PingService,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
) {
    var uiState by mutableStateOf<PingUiState>(PingUiState.Initial)
        private set

    fun pingBackend() {
        uiState = PingUiState.Loading

        coroutineScope.launch {
            pingService.ping()
                .onSuccess { response ->
                    uiState = PingUiState.Success(response)
                }
                .onFailure { error ->
                    uiState = PingUiState.Error(
                        error.message ?: "Unbekannter Fehler beim Verbinden mit dem Backend"
                    )
                }
        }
    }

    fun dispose() {
        coroutineScope.cancel()
        pingService.close()
    }
}

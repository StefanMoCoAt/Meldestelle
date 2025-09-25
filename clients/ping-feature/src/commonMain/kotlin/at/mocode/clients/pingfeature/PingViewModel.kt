package at.mocode.clients.pingfeature

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.mocode.ping.api.PingResponse
import at.mocode.ping.api.EnhancedPingResponse
import at.mocode.ping.api.HealthResponse
import kotlinx.coroutines.launch

data class PingUiState(
    val isLoading: Boolean = false,
    val simplePingResponse: PingResponse? = null,
    val enhancedPingResponse: EnhancedPingResponse? = null,
    val healthResponse: HealthResponse? = null,
    val errorMessage: String? = null
)

class PingViewModel : ViewModel() {
    private val apiClient = PingApiClient()

    var uiState by mutableStateOf(PingUiState())
        private set

    fun performSimplePing() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            try {
                val response = apiClient.simplePing()
                uiState = uiState.copy(
                    isLoading = false,
                    simplePingResponse = response
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "Simple ping failed: ${e.message}"
                )
            }
        }
    }

    fun performEnhancedPing(simulate: Boolean = false) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            try {
                val response = apiClient.enhancedPing(simulate)
                uiState = uiState.copy(
                    isLoading = false,
                    enhancedPingResponse = response
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "Enhanced ping failed: ${e.message}"
                )
            }
        }
    }

    fun performHealthCheck() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            try {
                val response = apiClient.healthCheck()
                uiState = uiState.copy(
                    isLoading = false,
                    healthResponse = response
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "Health check failed: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        uiState = uiState.copy(errorMessage = null)
    }
}

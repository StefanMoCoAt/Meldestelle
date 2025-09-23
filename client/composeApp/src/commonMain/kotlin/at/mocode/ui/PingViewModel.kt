package at.mocode.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.mocode.model.EnhancedPingResponse
import at.mocode.model.HealthResponse
import at.mocode.model.PingResponse
import at.mocode.service.PingService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PingUiState(
    val isLoading: Boolean = false,
    val lastPingResponse: PingResponse? = null,
    val lastEnhancedResponse: EnhancedPingResponse? = null,
    val lastHealthResponse: HealthResponse? = null,
    val error: String? = null
)

class PingViewModel : ViewModel() {

    private val pingService = PingService()

    private val _uiState = MutableStateFlow(PingUiState())
    val uiState: StateFlow<PingUiState> = _uiState.asStateFlow()

    fun simplePing() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            pingService.ping()
                .onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        lastPingResponse = response
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Ping failed: ${exception.message}"
                    )
                }
        }
    }

    fun enhancedPing(simulate: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            pingService.enhancedPing(simulate)
                .onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        lastEnhancedResponse = response
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Enhanced ping failed: ${exception.message}"
                    )
                }
        }
    }

    fun healthCheck() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            pingService.health()
                .onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        lastHealthResponse = response
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Health check failed: ${exception.message}"
                    )
                }
        }
    }
}

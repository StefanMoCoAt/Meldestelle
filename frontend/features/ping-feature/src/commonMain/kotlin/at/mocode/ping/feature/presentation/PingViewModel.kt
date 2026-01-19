package at.mocode.ping.feature.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.mocode.ping.api.EnhancedPingResponse
import at.mocode.ping.api.HealthResponse
import at.mocode.ping.api.PingApi
import at.mocode.ping.api.PingResponse
import at.mocode.ping.feature.domain.PingSyncService
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class PingUiState(
  val isLoading: Boolean = false,
  val simplePingResponse: PingResponse? = null,
  val enhancedPingResponse: EnhancedPingResponse? = null,
  val healthResponse: HealthResponse? = null,
  val errorMessage: String? = null,
  val isSyncing: Boolean = false,
  val lastSyncResult: String? = null
)

class PingViewModel(
  private val apiClient: PingApi,
  private val syncService: PingSyncService
) : ViewModel() {

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

  fun performSecurePing() {
    viewModelScope.launch {
      uiState = uiState.copy(isLoading = true, errorMessage = null)
      try {
        val response = apiClient.securePing()
        uiState = uiState.copy(
          isLoading = false,
          simplePingResponse = response
        )
      } catch (e: Exception) {
        uiState = uiState.copy(
          isLoading = false,
          errorMessage = "Secure ping failed: ${e.message}"
        )
      }
    }
  }

  fun triggerSync() {
    viewModelScope.launch {
      uiState = uiState.copy(isSyncing = true, errorMessage = null)
      try {
        syncService.syncPings()
        // Use kotlin.time.Clock explicitly to avoid ambiguity and deprecation issues
        val now = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        uiState = uiState.copy(
          isSyncing = false,
          lastSyncResult = "Sync successful at $now"
        )
      } catch (e: Exception) {
        uiState = uiState.copy(
          isSyncing = false,
          errorMessage = "Sync failed: ${e.message}"
        )
      }
    }
  }

  fun clearError() {
    uiState = uiState.copy(errorMessage = null)
  }
}

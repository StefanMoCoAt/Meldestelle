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
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class LogEntry(
    val timestamp: String,
    val source: String,
    val message: String,
    val isError: Boolean = false
)

data class PingUiState(
  val isLoading: Boolean = false,
  val simplePingResponse: PingResponse? = null,
  val enhancedPingResponse: EnhancedPingResponse? = null,
  val healthResponse: HealthResponse? = null,
  val errorMessage: String? = null,
  val isSyncing: Boolean = false,
  val lastSyncResult: String? = null,
  val logs: List<LogEntry> = emptyList()
)

class PingViewModel(
  private val apiClient: PingApi,
  private val syncService: PingSyncService
) : ViewModel() {

  var uiState by mutableStateOf(PingUiState())
    private set

  private fun addLog(source: String, message: String, isError: Boolean = false) {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val timeString = "${now.hour.toString().padStart(2, '0')}:${now.minute.toString().padStart(2, '0')}:${now.second.toString().padStart(2, '0')}"
    val entry = LogEntry(timeString, source, message, isError)
    uiState = uiState.copy(logs = listOf(entry) + uiState.logs) // Prepend for newest first
  }

  fun performSimplePing() {
    viewModelScope.launch {
      uiState = uiState.copy(isLoading = true, errorMessage = null)
      addLog("SimplePing", "Sending request...")
      try {
        val response = apiClient.simplePing()
        uiState = uiState.copy(
          isLoading = false,
          simplePingResponse = response
        )
        addLog("SimplePing", "Success: ${response.status} from ${response.service}")
      } catch (e: Exception) {
        val msg = "Simple ping failed: ${e.message}"
        uiState = uiState.copy(isLoading = false, errorMessage = msg)
        addLog("SimplePing", "Failed: ${e.message}", isError = true)
      }
    }
  }

  fun performEnhancedPing(simulate: Boolean = false) {
    viewModelScope.launch {
      uiState = uiState.copy(isLoading = true, errorMessage = null)
      addLog("EnhancedPing", "Sending request (simulate=$simulate)...")
      try {
        val response = apiClient.enhancedPing(simulate)
        uiState = uiState.copy(
          isLoading = false,
          enhancedPingResponse = response
        )
        addLog("EnhancedPing", "Success: CB=${response.circuitBreakerState}, Time=${response.responseTime}ms")
      } catch (e: Exception) {
        val msg = "Enhanced ping failed: ${e.message}"
        uiState = uiState.copy(isLoading = false, errorMessage = msg)
        addLog("EnhancedPing", "Failed: ${e.message}", isError = true)
      }
    }
  }

  fun performHealthCheck() {
    viewModelScope.launch {
      uiState = uiState.copy(isLoading = true, errorMessage = null)
      addLog("HealthCheck", "Checking system health...")
      try {
        val response = apiClient.healthCheck()
        uiState = uiState.copy(
          isLoading = false,
          healthResponse = response
        )
        addLog("HealthCheck", "Status: ${response.status}, Healthy: ${response.healthy}")
      } catch (e: Exception) {
        val msg = "Health check failed: ${e.message}"
        uiState = uiState.copy(isLoading = false, errorMessage = msg)
        addLog("HealthCheck", "Failed: ${e.message}", isError = true)
      }
    }
  }

  fun performSecurePing() {
    viewModelScope.launch {
      uiState = uiState.copy(isLoading = true, errorMessage = null)
      addLog("SecurePing", "Sending authenticated request...")
      try {
        val response = apiClient.securePing()
        uiState = uiState.copy(
          isLoading = false,
          simplePingResponse = response
        )
        addLog("SecurePing", "Success: Authorized access granted.")
      } catch (e: Exception) {
        val msg = "Secure ping failed: ${e.message}"
        uiState = uiState.copy(isLoading = false, errorMessage = msg)
        addLog("SecurePing", "Access Denied/Error: ${e.message}", isError = true)
      }
    }
  }

  fun triggerSync() {
    viewModelScope.launch {
      uiState = uiState.copy(isSyncing = true, errorMessage = null)
      addLog("Sync", "Starting delta sync...")
      try {
        syncService.syncPings()
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        uiState = uiState.copy(
          isSyncing = false,
          lastSyncResult = "Sync successful at $now"
        )
        addLog("Sync", "Sync completed successfully.")
      } catch (e: Exception) {
        val msg = "Sync failed: ${e.message}"
        uiState = uiState.copy(isSyncing = false, errorMessage = msg)
        addLog("Sync", "Sync failed: ${e.message}", isError = true)
      }
    }
  }

  fun clearLogs() {
    uiState = uiState.copy(logs = emptyList())
  }

  fun clearError() {
    uiState = uiState.copy(errorMessage = null)
  }
}

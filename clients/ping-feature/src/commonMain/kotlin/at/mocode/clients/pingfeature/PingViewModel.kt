package at.mocode.clients.pingfeature

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.mocode.clients.pingfeature.api.ReitsportTestApi
import at.mocode.clients.pingfeature.model.DateTimeHelper
import at.mocode.clients.pingfeature.model.ReitsportRole
import at.mocode.ping.api.EnhancedPingResponse
import at.mocode.ping.api.HealthResponse
import at.mocode.ping.api.PingApi
import at.mocode.ping.api.PingResponse
import kotlinx.coroutines.launch

data class PingUiState(
  val isLoading: Boolean = false,
  val simplePingResponse: PingResponse? = null,
  val enhancedPingResponse: EnhancedPingResponse? = null,
  val healthResponse: HealthResponse? = null,
  val errorMessage: String? = null
)

class PingViewModel(
  private val apiClient: PingApi = PingApiClient()
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

  fun clearError() {
    uiState = uiState.copy(errorMessage = null)
  }

  /**
   * Erweiterte Methode: Echte API-Tests für Reitsport-Rollen
   */
  fun testReitsportRole(role: ReitsportRole) {
    viewModelScope.launch {
      uiState = uiState.copy(
        isLoading = true,
        errorMessage = null
      )

      try {
        // Echte API-Tests durchführen
        val apiClient = ReitsportTestApi()
        val testResults = apiClient.testRole(role)

        // Erfolgs-Statistiken berechnen
        val successful = testResults.count { it.success }
        val total = testResults.size
        val successRate = if (total > 0) (successful * 100 / total) else 0

        // Test-Summary erstellen
        val summary = buildString {
          appendLine("🎯 ${role.displayName} - Test Abgeschlossen")
          appendLine("📊 Erfolgsrate: $successful/$total Tests ($successRate%)")
          appendLine("⏱️ Durchschnittsdauer: ${testResults.map { it.duration }.average().toInt()}ms")
          appendLine("🔑 Berechtigungen: ${role.permissions.size}")
          appendLine("")
          appendLine("📋 Test-Ergebnisse:")

          testResults.forEach { result ->
            val icon = if (result.success) "✅" else "❌"
            val status = if (result.responseCode != null) " (${result.responseCode})" else ""
            appendLine("$icon ${result.scenarioName}$status - ${result.duration}ms")
          }
        }

        // Mock-Response für Anzeige
        val mockResponse = PingResponse(
          status = summary,
          timestamp = DateTimeHelper.formatDateTime(DateTimeHelper.now()),
          service = "Reitsport-Auth-Test"
        )

        uiState = uiState.copy(
          isLoading = false,
          simplePingResponse = mockResponse
        )

        println("[DEBUG] Reitsport-API-Test: ${role.displayName}")
        println("[DEBUG] Ergebnisse: $successful/$total erfolgreich")

      } catch (e: Exception) {
        uiState = uiState.copy(
          isLoading = false,
          errorMessage = "Reitsport-API-Test fehlgeschlagen: ${e.message}"
        )
        println("[ERROR] Reitsport-Test-Fehler: ${e.message}")
      }
    }
  }
}

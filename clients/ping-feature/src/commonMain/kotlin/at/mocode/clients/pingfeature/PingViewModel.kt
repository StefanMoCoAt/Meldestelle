package at.mocode.clients.pingfeature

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.mocode.clients.pingfeature.model.ReitsportRole
import at.mocode.ping.api.EnhancedPingResponse
import at.mocode.ping.api.HealthResponse
import at.mocode.ping.api.PingApi
import at.mocode.ping.api.PingResponse
import kotlinx.coroutines.delay
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
     * Neue Methode: Teste eine Reitsport-Rolle
     */
    fun testReitsportRole(role: ReitsportRole) {
        viewModelScope.launch {
            uiState = uiState.copy(
                isLoading = true,
                errorMessage = null,
                // Hier erweitern wir später den UiState für Reitsport-Tests
            )

            try {
                // Phase 2: Erstmal nur ein einfacher Test
                delay(1000) // Simuliere API-Call

                val testResult = "✅ ${role.displayName} getestet!\n" +
                        "Berechtigungen: ${role.permissions.size}\n" +
                        "Kategorie: ${role.category.displayName}"

                // Erstelle ein Mock-PingResponse für die Anzeige
                val mockResponse = PingResponse(
                    status = testResult,
                    timestamp = "Test completed",
                    service = "Reitsport-Auth-Test"
                )

                uiState = uiState.copy(
                    isLoading = false,
                    // Zeige Ergebnis in der bestehenden simplePingResponse
                    simplePingResponse = mockResponse
                )

                println("[DEBUG] Reitsport-Test: ${role.displayName} mit ${role.permissions.size} Berechtigungen")

            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "Reitsport-Test fehlgeschlagen: ${e.message}"
                )
            }
        }
    }
}

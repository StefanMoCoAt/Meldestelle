package at.mocode.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import at.mocode.PlatformInfo
import at.mocode.SERVER_PORT
import at.mocode.model.ApiResponse
import at.mocode.model.Turnier
import at.mocode.network.httpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * ViewModel for managing tournaments (CRUD operations)
 */
class TurnierViewModel(private val coroutineScope: CoroutineScope) {
    var isLoading by mutableStateOf(true)
    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)
    var turniere by mutableStateOf<List<Turnier>>(emptyList())
    var selectedTurnier by mutableStateOf<Turnier?>(null)
    var isEditing by mutableStateOf(false)
    var isCreating by mutableStateOf(false)

    /**
     * Load tournaments from API
     */
    fun loadTurniere() {
        coroutineScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val url = "http://${PlatformInfo.apiHost}:${SERVER_PORT}/api/turniere"
                val response = httpClient.get(url)
                turniere = response.body<List<Turnier>>()
                isLoading = false
            } catch (e: Exception) {
                errorMessage = "Fehler beim Laden der Turniere vom Server: ${e.message}"
                isLoading = false
            }
        }
    }

    /**
     * Create a new tournament
     */
    fun createTurnier(turnier: Turnier) {
        coroutineScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val url = "http://${PlatformInfo.apiHost}:${SERVER_PORT}/api/turniere"
                val response = httpClient.post(url) {
                    contentType(ContentType.Application.Json)
                    setBody(turnier)
                }
                if (response.status.isSuccess()) {
                    successMessage = "Turnier erfolgreich erstellt"
                    loadTurniere()
                    isCreating = false
                } else {
                    val errorResponse = response.body<ApiResponse>()
                    errorMessage = errorResponse.message ?: "Fehler beim Erstellen des Turniers"
                }
            } catch (e: Exception) {
                errorMessage = "Fehler beim Erstellen des Turniers: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Update an existing tournament
     */
    fun updateTurnier(turnier: Turnier) {
        coroutineScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val url = "http://${PlatformInfo.apiHost}:${SERVER_PORT}/api/turniere/${turnier.number}"
                val response = httpClient.put(url) {
                    contentType(ContentType.Application.Json)
                    setBody(turnier)
                }
                if (response.status.isSuccess()) {
                    successMessage = "Turnier erfolgreich aktualisiert"
                    loadTurniere()
                    isEditing = false
                } else {
                    val errorResponse = response.body<ApiResponse>()
                    errorMessage = errorResponse.message ?: "Fehler beim Aktualisieren des Turniers"
                }
            } catch (e: Exception) {
                errorMessage = "Fehler beim Aktualisieren des Turniers: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Delete a tournament
     */
    fun deleteTurnier(turnierNumber: Int) {
        coroutineScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val url = "http://${PlatformInfo.apiHost}:${SERVER_PORT}/api/turniere/$turnierNumber"
                val response = httpClient.delete(url)
                if (response.status.isSuccess()) {
                    successMessage = "Turnier erfolgreich gelöscht"
                    loadTurniere()
                    if (selectedTurnier?.number == turnierNumber) {
                        selectedTurnier = null
                    }
                } else {
                    val errorResponse = response.body<ApiResponse>()
                    errorMessage = errorResponse.message ?: "Fehler beim Löschen des Turniers"
                }
            } catch (e: Exception) {
                errorMessage = "Fehler beim Löschen des Turniers: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Clear success message after a delay
     */
    fun clearSuccessMessage() {
        successMessage = null
    }
}

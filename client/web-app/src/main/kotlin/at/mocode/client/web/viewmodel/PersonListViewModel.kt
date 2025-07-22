package at.mocode.client.web.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import at.mocode.client.common.repository.Person
import at.mocode.client.common.repository.PersonRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel for displaying a list of persons.
 * This is a simplified version that doesn't depend on androidx.lifecycle.
 * It uses Compose for Desktop's own state management.
 */
class PersonListViewModel(
    private val personRepository: PersonRepository
) {
    // Coroutine scope for launching background tasks
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    // UI state
    var persons by mutableStateOf<List<PersonUiModel>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        loadPersons()
    }

    fun loadPersons() {
        coroutineScope.launch {
            isLoading = true
            errorMessage = null

            try {
                // Load persons from the repository
                val personList = personRepository.findAllActive(limit = 100, offset = 0)

                // Map domain models to UI models
                persons = personList.map { it.toUiModel() }
            } catch (e: Exception) {
                errorMessage = "Fehler beim Laden der Personen: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun clearError() {
        errorMessage = null
    }

    fun refreshPersons() {
        loadPersons()
    }

    /**
     * Maps a domain Person to a UI PersonUiModel
     */
    private fun Person.toUiModel(): PersonUiModel {
        return PersonUiModel(
            id = this.id,
            name = this.getFullName(),
            email = this.email,
            phone = this.telefon,
            address = this.getFormattedAddress()
        )
    }
}

/**
 * UI model for a person.
 * This is a simplified version that doesn't depend on domain models.
 */
data class PersonUiModel(
    val id: String,
    val name: String,
    val email: String? = null,
    val phone: String? = null,
    val address: String? = null
)

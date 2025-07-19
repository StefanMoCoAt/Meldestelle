package at.mocode.ui.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.mocode.members.domain.model.DomPerson
import at.mocode.members.domain.repository.PersonRepository
import kotlinx.coroutines.launch

class PersonListViewModel(
    private val personRepository: PersonRepository
) : ViewModel() {

    // UI state
    var persons by mutableStateOf<List<DomPerson>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        loadPersons()
    }

    fun loadPersons() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                persons = personRepository.findAllActive(limit = 100, offset = 0)
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
}

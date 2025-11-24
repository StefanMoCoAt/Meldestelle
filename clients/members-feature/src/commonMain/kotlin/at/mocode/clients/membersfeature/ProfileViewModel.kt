package at.mocode.clients.membersfeature

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.mocode.clients.membersfeature.model.MemberProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = false,
    val profile: MemberProfile? = null,
    val errorMessage: String? = null
)

class ProfileViewModel(
    private val api: MembersApiClient = MembersApiClient()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadProfile() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val profile = api.getMyProfile()
                _uiState.value = ProfileUiState(isLoading = false, profile = profile)
            } catch (e: Exception) {
                _uiState.value = ProfileUiState(
                    isLoading = false,
                    errorMessage = e.message ?: "Profil konnte nicht geladen werden"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

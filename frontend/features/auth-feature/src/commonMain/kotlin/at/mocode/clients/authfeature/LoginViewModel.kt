package at.mocode.clients.authfeature

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.mocode.clients.authfeature.AuthenticatedHttpClient.addAuthHeader
import at.mocode.clients.shared.core.AppConstants
import io.ktor.client.request.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI state for the login screen
 */
data class LoginUiState(
  val username: String = "",
  val password: String = "",
  val isLoading: Boolean = false,
  val isAuthenticated: Boolean = false,
  val errorMessage: String? = null,
  val usernameError: String? = null,
  val passwordError: String? = null
) {
  val canLogin: Boolean
    get() = username.isNotBlank() && password.isNotBlank() && !isLoading
}

/**
 * ViewModel for handling login authentication logic
 */
class LoginViewModel(
  private val authTokenManager: AuthTokenManager
) : ViewModel() {

  private val _uiState = MutableStateFlow(LoginUiState())
  val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

  private val authApiClient = AuthApiClient()

  fun updateUsername(username: String) {
    _uiState.value = _uiState.value.copy(
      username = username,
      usernameError = null,
      errorMessage = null
    )
  }

  fun updatePassword(password: String) {
    _uiState.value = _uiState.value.copy(
      password = password,
      passwordError = null,
      errorMessage = null
    )
  }

  fun login() {
    val currentState = _uiState.value

    // Validate input
    if (currentState.username.isBlank()) {
      _uiState.value = currentState.copy(usernameError = "Benutzername ist erforderlich")
      return
    }

    if (currentState.password.isBlank()) {
      _uiState.value = currentState.copy(passwordError = "Passwort ist erforderlich")
      return
    }

    // Start the login process
    _uiState.value = currentState.copy(
      isLoading = true,
      errorMessage = null,
      usernameError = null,
      passwordError = null
    )

    viewModelScope.launch {
      try {
        val loginResponse = authApiClient.login(
          username = currentState.username,
          password = currentState.password
        )

        if (loginResponse.success && loginResponse.token != null) {
          // Store the JWT token
          authTokenManager.setToken(loginResponse.token)

          _uiState.value = _uiState.value.copy(
            isLoading = false,
            isAuthenticated = true,
            errorMessage = null
          )

          // Fire-and-forget: Trigger Backend Sync so the user exists in Members
          viewModelScope.launch {
            try {
              val client = AuthenticatedHttpClient.create()
              client.post("${AppConstants.GATEWAY_URL}/api/members/sync") {
                addAuthHeader()
              }
            } catch (_: Exception) {
              // Non-fatal: Wir zeigen Sync-Fehler im Login nicht an
            }
          }
        } else {
          _uiState.value = _uiState.value.copy(
            isLoading = false,
            errorMessage = loginResponse.message ?: "Anmeldung fehlgeschlagen"
          )
        }
      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(
          isLoading = false,
          errorMessage = "Verbindungsfehler: ${e.message}"
        )
      }
    }
  }

  fun logout() {
    authTokenManager.clearToken()
    _uiState.value = LoginUiState()
  }

  fun checkAuthenticationStatus() {
    val isAuthenticated = authTokenManager.hasValidToken()
    _uiState.value = _uiState.value.copy(isAuthenticated = isAuthenticated)
  }
}

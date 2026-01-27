package at.mocode.frontend.core.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.mocode.frontend.core.auth.data.AuthApiClient
import at.mocode.frontend.core.auth.data.AuthTokenManager
import io.ktor.client.request.post
import io.ktor.client.HttpClient
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
  private val authTokenManager: AuthTokenManager,
  private val authApiClient: AuthApiClient,
  private val apiClient: HttpClient
) : ViewModel() {

  private val _uiState = MutableStateFlow(LoginUiState())
  val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

  init {
    // Observe AuthTokenManager state to keep UI in sync
    viewModelScope.launch {
      authTokenManager.authState.collect { authState ->
        _uiState.value = _uiState.value.copy(
          isAuthenticated = authState.isAuthenticated
        )
        // If logged out, clear credentials
        if (!authState.isAuthenticated) {
          _uiState.value = LoginUiState()
        }
      }
    }
  }

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

          // isAuthenticated will be updated via the flow collector in init block

          _uiState.value = _uiState.value.copy(
            isLoading = false,
            errorMessage = null
          )

          // Fire-and-forget: Trigger Backend Sync so the user exists in Members
          viewModelScope.launch {
            try {
              // Fire-and-forget sync call; Bearer token added by Ktor Auth plugin
              // IMPORTANT: Use relative path (no leading slash) so Ktor appends it to baseUrl
              // baseUrl is http://localhost:8080/api (JS) or http://localhost:8081 (JVM)
              // Result: http://localhost:8080/api/members/sync -> Proxy -> http://localhost:8081/api/members/sync
              // apiClient.post("members/sync")
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
}

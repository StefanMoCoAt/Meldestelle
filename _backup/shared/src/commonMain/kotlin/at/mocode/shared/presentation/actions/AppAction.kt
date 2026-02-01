package at.mocode.shared.presentation.actions

import at.mocode.shared.presentation.state.Notification
import at.mocode.frontend.core.domain.models.User
import at.mocode.frontend.core.domain.models.AuthToken

sealed class AppAction {
  // Auth Actions
  sealed class Auth : AppAction() {
    data class LoginStart(val username: String, val password: String) : Auth()
    data class LoginSuccess(val user: User, val token: AuthToken) : Auth()
    data class LoginFailure(val error: String) : Auth()
    object Logout : Auth()
    data class RefreshToken(val newToken: AuthToken) : Auth()
  }

  // Navigation Actions
  sealed class Navigation : AppAction() {
    data class NavigateTo(val route: String) : Navigation()
    object NavigateBack : Navigation()
    data class UpdateHistory(val route: String) : Navigation()
  }

  // UI Actions
  sealed class UI : AppAction() {
    object ToggleDarkMode : UI()
    data class SetLoading(val isLoading: Boolean) : UI()
    data class ShowNotification(val notification: Notification) : UI()
    data class DismissNotification(val id: String) : UI()
  }

  // Network Actions
  sealed class Network : AppAction() {
    data class SetOnlineStatus(val isOnline: Boolean) : Network()
    data class UpdateLastSync(val timestamp: String) : Network()
  }
}

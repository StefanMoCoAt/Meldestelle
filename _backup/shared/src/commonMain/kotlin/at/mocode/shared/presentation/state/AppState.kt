package at.mocode.shared.presentation.state

import kotlinx.serialization.Serializable
import at.mocode.frontend.core.domain.models.User
import at.mocode.frontend.core.domain.models.AuthToken

@Serializable
data class AppState(
  val auth: AuthState = AuthState(),
  val navigation: NavigationState = NavigationState(),
  val ui: UiState = UiState(),
  val network: NetworkState = NetworkState()
)

@Serializable
data class AuthState(
  val isAuthenticated: Boolean = false,
  val user: User? = null,
  val token: AuthToken? = null,
  val isLoading: Boolean = false,
  val error: String? = null
)

@Serializable
data class NavigationState(
  val currentRoute: String = "/",
  val history: List<String> = emptyList(),
  val canGoBack: Boolean = false
)

@Serializable
data class UiState(
  val isDarkMode: Boolean = false,
  val isLoading: Boolean = false,
  val notifications: List<Notification> = emptyList()
)

@Serializable
data class NetworkState(
  val isOnline: Boolean = true,
  val lastSync: String? = null
)

@Serializable
data class Notification(
  val id: String,
  val title: String,
  val message: String,
  val type: NotificationType = NotificationType.INFO,
  val timestamp: String
)

enum class NotificationType {
  INFO, SUCCESS, WARNING, ERROR
}

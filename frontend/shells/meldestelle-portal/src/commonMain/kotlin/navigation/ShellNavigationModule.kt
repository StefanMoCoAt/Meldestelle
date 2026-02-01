package navigation

import at.mocode.frontend.core.auth.data.AuthTokenManager
import at.mocode.frontend.core.domain.models.User
import at.mocode.frontend.core.navigation.AppScreen
import at.mocode.frontend.core.navigation.CurrentUserProvider
import at.mocode.frontend.core.navigation.DeepLinkHandler
import at.mocode.frontend.core.navigation.NavigationPort
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.dsl.module

class ShellCurrentUserProvider(
  private val authTokenManager: AuthTokenManager,
) : CurrentUserProvider {
  override fun getCurrentUser(): User? {
    val state = authTokenManager.authState.value
    if (!state.isAuthenticated) return null
    // Roles are not yet modeled in AuthState; provide empty list for now
    return User(
      id = state.userId ?: state.username ?: "unknown",
      username = state.username ?: state.userId ?: "unknown",
      displayName = null,
      roles = emptyList(),
    )
  }
}

/**
 * A real implementation of NavigationPort that updates a StateFlow.
 * This allows the MainApp to observe changes and update the UI.
 */
class StateNavigationPort : NavigationPort {
  private val _currentScreen = MutableStateFlow<AppScreen>(AppScreen.Landing)
  val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

  override fun navigateTo(route: String) {
    val screen = AppScreen.fromRoute(route)
    println("[NavigationPort] navigateTo $route -> $screen")
    _currentScreen.value = screen
  }

  fun navigateToScreen(screen: AppScreen) {
      _currentScreen.value = screen
  }
}

val navigationModule = module {
  single<CurrentUserProvider> { ShellCurrentUserProvider(get()) }
  // Bind as both NavigationPort (for Core) and StateNavigationPort (for Shell)
  single { StateNavigationPort() }
  single<NavigationPort> { get<StateNavigationPort>() }
  single { DeepLinkHandler(get(), get()) }
}

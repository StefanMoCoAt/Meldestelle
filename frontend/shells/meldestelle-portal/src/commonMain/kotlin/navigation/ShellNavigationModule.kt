package navigation

import at.mocode.frontend.core.auth.data.AuthTokenManager
import at.mocode.frontend.core.domain.models.User
import at.mocode.frontend.core.navigation.CurrentUserProvider
import at.mocode.frontend.core.navigation.DeepLinkHandler
import at.mocode.frontend.core.navigation.NavigationPort
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

class NoopNavigationPort : NavigationPort {
  var lastRoute: String? = null
  override fun navigateTo(route: String) {
    lastRoute = route
    // Simple logging; actual routing is handled elsewhere in the shell
    println("[NavigationPort] navigateTo $route")
  }
}

val navigationModule = module {
  single<CurrentUserProvider> { ShellCurrentUserProvider(get()) }
  single<NavigationPort> { NoopNavigationPort() }
  single { DeepLinkHandler(get(), get()) }
}

package at.mocode.frontend.core.navigation

sealed class AppScreen(val route: String) {
  data object Landing : AppScreen(Routes.HOME)
  data object Home : AppScreen("/home")
  data object Login : AppScreen(Routes.LOGIN)
  data object Ping : AppScreen("/ping")
  data object Profile : AppScreen("/profile")
  data object AuthCallback : AppScreen("/auth/callback")

  companion object {
    fun fromRoute(route: String): AppScreen {
      return when (route) {
        Routes.HOME -> Landing
        "/home" -> Home
        Routes.LOGIN, Routes.Auth.LOGIN -> Login
        "/ping" -> Ping
        "/profile" -> Profile
        "/auth/callback" -> AuthCallback
        else -> Landing // Default fallback
      }
    }
  }
}

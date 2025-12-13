package at.mocode.clients.shared.navigation

sealed class AppScreen {
  data object Landing : AppScreen()
  data object Home : AppScreen()
  data object Login : AppScreen()
  data object Ping : AppScreen()
  data object Profile : AppScreen()
  data object AuthCallback : AppScreen()
}

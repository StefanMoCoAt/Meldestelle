package at.mocode.clients.shared.navigation

sealed class AppScreen {
    data object Home : AppScreen()
    data object Ping : AppScreen()
}

package at.mocode.clients.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import at.mocode.clients.shared.commonui.components.AppHeader
import at.mocode.clients.shared.commonui.components.AppScaffold
import at.mocode.clients.shared.commonui.theme.AppTheme
import at.mocode.clients.shared.navigation.AppScreen
import at.mocode.clients.pingfeature.PingScreen
import at.mocode.clients.pingfeature.PingViewModel

@Composable
fun App() {
    var currentScreen: AppScreen by remember { mutableStateOf(AppScreen.Home) }

    AppTheme {
        AppScaffold(
            header = {
                AppHeader(
                    title = "Meldestelle",
                    onNavigateToPing = { currentScreen = AppScreen.Ping }
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                when (currentScreen) {
                    is AppScreen.Home -> {
                        LandingScreen()
                    }
                    is AppScreen.Ping -> {
                        val pingViewModel: PingViewModel = viewModel()
                        PingScreen(viewModel = pingViewModel)
                    }
                }
            }
        }
    }
}

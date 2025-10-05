package at.mocode.clients.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import at.mocode.clients.shared.commonui.components.AppHeader
import at.mocode.clients.shared.commonui.components.AppScaffold
import at.mocode.clients.shared.commonui.theme.AppTheme
import at.mocode.clients.shared.navigation.AppScreen
import at.mocode.clients.pingfeature.PingScreen
import at.mocode.clients.pingfeature.PingViewModel
import at.mocode.clients.authfeature.LoginScreen
import at.mocode.clients.authfeature.AuthTokenManager
import androidx.compose.runtime.collectAsState

@Composable
fun App() {
    var currentScreen: AppScreen by remember { mutableStateOf(AppScreen.Home) }
    // Create a single PingViewModel instance for the lifetime of the App composition.
    val pingViewModel: PingViewModel = remember { PingViewModel() }
    // Create a single AuthTokenManager instance for the lifetime of the App composition.
    val authTokenManager: AuthTokenManager = remember { AuthTokenManager() }
    // Observe authentication state
    val authState by authTokenManager.authState.collectAsState()

    AppTheme {
        AppScaffold(
            header = {
                AppHeader(
                    title = "Meldestelle",
                    onNavigateToPing = { currentScreen = AppScreen.Ping },
                    onNavigateToLogin = { currentScreen = AppScreen.Login },
                    onLogout = {
                        authTokenManager.clearToken()
                        currentScreen = AppScreen.Home
                    },
                    isAuthenticated = authState.isAuthenticated,
                    username = authState.username,
                    userPermissions = authState.permissions.map { it.name }
                )
            },
            { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    when (currentScreen) {
                        is AppScreen.Home -> {
                            LandingScreen(authTokenManager = authTokenManager)
                        }

                        is AppScreen.Login -> {
                            LoginScreen(
                                authTokenManager = authTokenManager,
                                onLoginSuccess = { currentScreen = AppScreen.Home }
                            )
                        }

                        is AppScreen.Ping -> {
                            PingScreen(viewModel = pingViewModel)
                        }
                    }
                }
            }
        )
    }
}

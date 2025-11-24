import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import at.mocode.clients.membersfeature.ProfileScreen
import at.mocode.clients.membersfeature.ProfileViewModel
import at.mocode.clients.shared.navigation.AppScreen

@Composable
fun MainApp() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Home) }

            when (currentScreen) {
                is AppScreen.Home -> DevelopmentScreen(onOpenProfile = { currentScreen = AppScreen.Profile })
                is AppScreen.Login -> DevelopmentScreen(onOpenProfile = { currentScreen = AppScreen.Profile })
                is AppScreen.Ping -> DevelopmentScreen(onOpenProfile = { currentScreen = AppScreen.Profile })
                is AppScreen.Profile -> ProfileScreen(viewModel = remember { ProfileViewModel() })
            }
        }
    }
}

@Composable
fun DevelopmentScreen(onOpenProfile: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "🚀 Meldestelle Development Mode",
            style = MaterialTheme.typography.headlineMedium
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "🌐 Backend Connectivity",
                    style = MaterialTheme.typography.titleMedium
                )

                var testStatus by remember { mutableStateOf("Not tested") }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = { testStatus = "Testing Gateway..." }) {
                        Text("Test Gateway")
                    }
                    Button(onClick = { testStatus = "Testing Ping Service..." }) {
                        Text("Test Ping Service")
                    }
                    Button(onClick = onOpenProfile) {
                        Text("Open Profile")
                    }
                }

                Text("Status: $testStatus")
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "🏓 Ping Service Tests",
                    style = MaterialTheme.typography.titleMedium
                )

                var isDarkMode by remember { mutableStateOf(false) }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = { /* TODO: Health Check */ }) {
                        Text("Health Check")
                    }
                    Button(onClick = { /* TODO: Ping Normal */ }) {
                        Text("Ping Normal")
                    }
                    Button(onClick = { isDarkMode = !isDarkMode }) {
                        Text("Toggle Dark Mode")
                    }
                }

                Text("Dark Mode: ${if(isDarkMode) "🌙 Enabled" else "☀️ Disabled"}")
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "✅ System Status",
                    style = MaterialTheme.typography.titleMedium
                )
                Text("Frontend: 🟢 Running")
                Text("Backend: ⚠️ Testing needed")
                Text("Build: ✅ Successful")
            }
        }
    }
}

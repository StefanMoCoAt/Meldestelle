package at.mocode.clients.membersfeature

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProfileScreen(viewModel: ProfileViewModel) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "👤 Mein Profil", style = MaterialTheme.typography.headlineSmall)

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        state.errorMessage?.let { error ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Fehler", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(error)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { viewModel.clearError() }) { Text("Schließen") }
                }
            }
        }

        state.profile?.let { profile ->
            Card {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = profile.fullName.ifBlank { "Unbekannt" }, style = MaterialTheme.typography.titleLarge)
                    profile.username?.let { Text("Benutzername: $it") }
                    profile.email?.let { Text("E-Mail: $it") }
                    if (profile.roles.isNotEmpty()) {
                        Text("Rollen: ${profile.roles.joinToString(", ")}")
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { viewModel.loadProfile() }, enabled = !state.isLoading) {
                Text("Neu laden")
            }
        }
    }
}

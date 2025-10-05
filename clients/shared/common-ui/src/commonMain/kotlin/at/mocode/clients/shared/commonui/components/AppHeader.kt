package at.mocode.clients.shared.commonui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppHeader(
    title: String,
    onNavigateToPing: (() -> Unit)? = null,
    onNavigateToLogin: (() -> Unit)? = null,
    onLogout: (() -> Unit)? = null,
    isAuthenticated: Boolean = false,
    username: String? = null,
    userPermissions: List<String> = emptyList()
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        actions = {
            // Ping Service button
            onNavigateToPing?.let { navigateAction ->
                TextButton(
                    onClick = navigateAction
                ) {
                    Text("Ping Service")
                }
            }

            // Authentication buttons
            if (isAuthenticated) {
                // Show username with admin indicator if user has delete permissions
                username?.let { user ->
                    val isAdmin = userPermissions.any { it.contains("DELETE") }
                    Text(
                        text = if (isAdmin) "👑 Hallo, $user (Admin)" else "Hallo, $user",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isAdmin)
                            MaterialTheme.colorScheme.tertiary
                        else
                            MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                onLogout?.let { logoutAction ->
                    TextButton(
                        onClick = logoutAction
                    ) {
                        Text("Abmelden")
                    }
                }
            } else {
                // Show login button
                onNavigateToLogin?.let { loginAction ->
                    TextButton(
                        onClick = loginAction
                    ) {
                        Text("Anmelden")
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

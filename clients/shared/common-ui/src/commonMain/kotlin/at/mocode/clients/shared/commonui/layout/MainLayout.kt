package at.mocode.clients.shared.commonui.layout

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import at.mocode.clients.shared.commonui.components.*
import at.mocode.clients.shared.commonui.screens.LoginScreenContainer
import at.mocode.clients.shared.presentation.state.AppState
import at.mocode.clients.shared.presentation.actions.AppAction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayout(
    appState: AppState,
    onDispatchAction: (AppAction) -> Unit,
    onNavigateTo: (String) -> Unit,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    var showUserMenu by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Meldestelle",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    // Notifications
                    if (appState.ui.notifications.isNotEmpty()) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    contentColor = MaterialTheme.colorScheme.onError,
                                    containerColor = MaterialTheme.colorScheme.error
                                ) {
                                    Text(appState.ui.notifications.size.toString())
                                }
                            }
                        ) {
                            IconButton(
                                onClick = { onNavigateTo("/notifications") }
                            ) {
                                Text("🔔")
                            }
                        }
                    } else {
                        IconButton(
                            onClick = { onNavigateTo("/notifications") }
                        ) {
                            Text("🔔")
                        }
                    }

                    // Theme toggle
                    IconButton(
                        onClick = { onDispatchAction(AppAction.UI.ToggleDarkMode) }
                    ) {
                        Text(if (appState.ui.isDarkMode) "☀️" else "🌙")
                    }

                    // User menu
                    Box {
                        IconButton(
                            onClick = { showUserMenu = true }
                        ) {
                            Text("👤")
                        }

                        DropdownMenu(
                            expanded = showUserMenu,
                            onDismissRequest = { showUserMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = appState.auth.user?.firstName ?: "User",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = appState.auth.user?.email ?: "",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    showUserMenu = false
                                    onNavigateTo("/profile")
                                }
                            )

                            HorizontalDivider()

                            DropdownMenuItem(
                                text = { Text("Settings") },
                                onClick = {
                                    showUserMenu = false
                                    onNavigateTo("/settings")
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Help") },
                                onClick = {
                                    showUserMenu = false
                                    onNavigateTo("/help")
                                }
                            )

                            HorizontalDivider()

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Logout",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    showUserMenu = false
                                    onDispatchAction(AppAction.Auth.Logout)
                                }
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (appState.ui.notifications.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${appState.ui.notifications.size} notification(s)",
                            style = MaterialTheme.typography.bodySmall
                        )
                        TextButton(
                            onClick = { onNavigateTo("/notifications") }
                        ) {
                            Text("View All")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Loading overlay
            if (appState.ui.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    FullScreenLoading("Loading...")
                }
            } else {
                content()
            }
        }
    }
}

@Composable
fun AuthenticatedLayout(
    appState: AppState,
    onDispatchAction: (AppAction) -> Unit,
    onNavigateTo: (String) -> Unit,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    if (appState.auth.isAuthenticated) {
        MainLayout(
            appState = appState,
            onDispatchAction = onDispatchAction,
            onNavigateTo = onNavigateTo,
            content = content,
            modifier = modifier
        )
    } else {
        // Show login screen if not authenticated
        LoginScreenContainer(
            authState = appState.auth,
            onDispatchAction = onDispatchAction,
            modifier = modifier
        )
    }
}

@Composable
fun ResponsiveLayout(
    appState: AppState,
    onDispatchAction: (AppAction) -> Unit,
    onNavigateTo: (String) -> Unit,
    content: @Composable (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    // Simple responsive design - could be enhanced with actual screen size detection
    val isCompact = remember { mutableStateOf(false) }

    AuthenticatedLayout(
        appState = appState,
        onDispatchAction = onDispatchAction,
        onNavigateTo = onNavigateTo,
        content = { content(isCompact.value) },
        modifier = modifier
    )
}

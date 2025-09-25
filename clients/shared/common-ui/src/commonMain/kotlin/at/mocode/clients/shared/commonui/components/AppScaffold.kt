package at.mocode.clients.shared.commonui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    header: @Composable () -> Unit = {
        AppHeader(title = "Meldestelle")
    },
    footer: @Composable () -> Unit = {
        AppFooter()
    },
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = header,
        bottomBar = footer,
        content = content
    )
}

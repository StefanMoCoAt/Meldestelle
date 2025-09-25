package at.mocode.clients.shared.commonui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable

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

package at.mocode.clients.shared.commonui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
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
    Column(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = header,
            modifier = Modifier.weight(1f)
        ) {
            paddingValues ->
            content(paddingValues)
        }
        footer()
    }

}

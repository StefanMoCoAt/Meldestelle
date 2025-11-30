package at.mocode.clients.shared.commonui.components

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
  content: @Composable (PaddingValues) -> Unit,
  footer: @Composable () -> Unit = {
    AppFooter()
  },
) {
  Scaffold(
    topBar = header,
    bottomBar = footer,
    modifier = Modifier.fillMaxSize()
  ) { paddingValues ->
    content(paddingValues)
  }
}

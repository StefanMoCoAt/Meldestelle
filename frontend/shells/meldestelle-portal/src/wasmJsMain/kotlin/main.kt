import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import org.w3c.dom.HTMLElement

import at.mocode.shared.di.initKoin
import at.mocode.frontend.core.network.networkModule
import at.mocode.clients.authfeature.di.authFeatureModule

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
  // Initialize DI
  try {
    initKoin { modules(networkModule, authFeatureModule) }
    println("[WasmApp] Koin initialized")
  } catch (e: Exception) {
    println("[WasmApp] Koin init failed: ${e.message}")
  }

  val root = document.getElementById("ComposeTarget") as HTMLElement
  ComposeViewport(root) {
    MainApp()
  }
}

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import org.w3c.dom.HTMLElement

import at.mocode.shared.di.initKoin
import at.mocode.frontend.core.network.networkModule
import at.mocode.clients.authfeature.di.authFeatureModule
import navigation.navigationModule

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
  // Initialize DI
  try {
    initKoin { modules(networkModule, authFeatureModule, navigationModule) }
    println("[WasmApp] Koin initialized (with navigationModule)")
  } catch (e: Exception) {
    println("[WasmApp] Koin init failed: ${e.message}")
  }

  val root = document.getElementById("ComposeTarget") as HTMLElement
  ComposeViewport(root) {
    MainApp()
  }
}

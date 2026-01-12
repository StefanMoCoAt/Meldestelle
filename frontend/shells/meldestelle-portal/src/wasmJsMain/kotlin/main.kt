import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import org.w3c.dom.HTMLElement

import at.mocode.shared.di.initKoin
import at.mocode.frontend.core.network.networkModule
import at.mocode.clients.authfeature.di.authFeatureModule
import at.mocode.frontend.core.sync.di.syncModule
import at.mocode.ping.feature.di.pingFeatureModule
import at.mocode.frontend.core.localdb.AppDatabase
import at.mocode.frontend.core.localdb.DatabaseProvider
import navigation.navigationModule
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
  // Initialize DI
  try {
    initKoin { modules(networkModule, syncModule, pingFeatureModule, authFeatureModule, navigationModule) }
    println("[WasmApp] Koin initialized (with navigationModule)")
  } catch (e: Exception) {
    println("[WasmApp] Koin init failed: ${e.message}")
  }

  // Create the local DB asynchronously and register it into Koin.
  try {
    val provider = GlobalContext.get().get<DatabaseProvider>()
    MainScope().launch {
      try {
        val db = provider.createDatabase()
        loadKoinModules(
          module {
            single<AppDatabase> { db }
          }
        )
        println("[WasmApp] Local DB created and registered in Koin")
      } catch (e: dynamic) {
        println("[WasmApp] Local DB init warning: ${e?.message ?: e}")
      }
    }
  } catch (e: Exception) {
    println("[WasmApp] Local DB init warning: ${e.message}")
  }

  val root = document.getElementById("ComposeTarget") as HTMLElement
  ComposeViewport(root) {
    MainApp()
  }
}

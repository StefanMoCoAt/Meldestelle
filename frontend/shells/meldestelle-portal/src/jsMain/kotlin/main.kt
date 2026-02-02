import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import at.mocode.frontend.core.auth.di.authModule
import at.mocode.frontend.core.localdb.AppDatabase
import at.mocode.frontend.core.localdb.DatabaseProvider
import at.mocode.frontend.core.localdb.localDbModule
import at.mocode.frontend.core.network.networkModule
import at.mocode.frontend.core.sync.di.syncModule
import at.mocode.ping.feature.di.pingFeatureModule
import kotlinx.browser.document
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import navigation.navigationModule
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.w3c.dom.HTMLElement

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
  console.log("[WebApp] main() entered")

  MainScope().launch {
    try {
      // 1. Load Runtime Configuration (Async)
      console.log("[WebApp] Loading configuration...")
      val config = loadAppConfig()
      console.log("[WebApp] Configuration loaded: apiBaseUrl=${config.apiBaseUrl}")

      // 2. Initialize DI (Koin)
      // We register the config immediately so other modules can use it
      startKoin {
        modules(
          module { single { config } }, // Make AppConfig available for injection
          networkModule,
          localDbModule,
          syncModule,
          pingFeatureModule,
          authModule,
          navigationModule
        )
      }
      console.log("[WebApp] Koin initialized")

      // 3. Initialize Database (Async)
      val provider = GlobalContext.get().get<DatabaseProvider>()
      console.log("[WebApp] Initializing Database...")
      val db = provider.createDatabase()

      // Register the created DB instance into Koin
      loadKoinModules(
        module {
          single<AppDatabase> { db }
        }
      )
      console.log("[WebApp] Local DB created and registered in Koin")

      // 4. Start UI
      startAppWhenDomReady()

    } catch (e: dynamic) {
      console.error("[WebApp] CRITICAL: Initialization failed:", e)
      renderFatalError("Initialization failed: ${e?.message ?: e}")
    }
  }
}

@OptIn(ExperimentalComposeUiApi::class)
fun startAppWhenDomReady() {
  val state = document.asDynamic().readyState as String?
  if (state == "interactive" || state == "complete") {
    mountComposeApp()
  } else {
    document.addEventListener("DOMContentLoaded", { mountComposeApp() })
  }
}

@OptIn(ExperimentalComposeUiApi::class)
fun mountComposeApp() {
  try {
    console.log("[WebApp] Mounting Compose App...")
    val root = document.getElementById("ComposeTarget") as HTMLElement

    ComposeViewport(root) {
      MainApp()
    }

    // Remove loading spinner
    (document.querySelector(".loading") as? HTMLElement)?.let { it.parentElement?.removeChild(it) }
    console.log("[WebApp] App mounted successfully")

  } catch (e: Exception) {
    console.error("Failed to start Compose Web app", e)
    renderFatalError("UI Mount failed: ${e.message}")
  }
}

fun renderFatalError(message: String) {
  val fallbackTarget = (document.getElementById("ComposeTarget") ?: document.body) as HTMLElement
  fallbackTarget.innerHTML = """
    <div style='padding: 50px; text-align: center; color: #D32F2F; font-family: sans-serif;'>
      <h1>System Error</h1>
      <p>The application could not be started.</p>
      <pre style='background: #FFEBEE; padding: 10px; border-radius: 4px; text-align: left; display: inline-block;'>$message</pre>
    </div>
  """.trimIndent()
}

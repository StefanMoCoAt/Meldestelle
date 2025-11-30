import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import org.w3c.dom.HTMLElement
import at.mocode.clients.shared.di.initKoin
import at.mocode.frontend.core.network.networkModule
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext
import org.koin.core.qualifier.named
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
  console.log("[WebApp] main() entered")
  // Initialize DI (Koin) with shared modules + network module
  try {
    initKoin { modules(networkModule) }
    console.log("[WebApp] Koin initialized with networkModule")
  } catch (e: dynamic) {
    console.warn("[WebApp] Koin initialization warning:", e)
  }
  // Simple smoke request using DI apiClient
  try {
    val client = GlobalContext.get().get<HttpClient>(named("apiClient"))
    MainScope().launch {
      try {
        val resp: String = client.get("/api/ping/health").body()
        console.log("[WebApp] /api/ping/health → ", resp)
      } catch (e: dynamic) {
        console.warn("[WebApp] /api/ping/health failed:", e?.message ?: e)
      }
    }
  } catch (e: dynamic) {
    console.warn("[WebApp] Unable to resolve apiClient from Koin:", e)
  }
  fun startApp() {
    try {
      console.log("[WebApp] startApp(): readyState=", document.asDynamic().readyState)
      val root = document.getElementById("ComposeTarget") as HTMLElement
      console.log("[WebApp] ComposeTarget exists? ", (root != null))
      ComposeViewport(root) {
        MainApp()
      }
      // Remove the static loading placeholder if present
      (document.querySelector(".loading") as? HTMLElement)?.let { it.parentElement?.removeChild(it) }
      console.log("[WebApp] ComposeViewport mounted, loading placeholder removed")
    } catch (e: Exception) {
      console.error("Failed to start Compose Web app", e)
      val fallbackTarget = (document.getElementById("ComposeTarget") ?: document.body) as HTMLElement
      fallbackTarget.innerHTML =
        "<div style='padding: 50px; text-align: center;'>❌ Failed to load app: ${e.message}</div>"
    }
  }

  // Start immediately if DOM is already parsed, otherwise wait for DOMContentLoaded.
  val state = document.asDynamic().readyState as String?
  if (state == "interactive" || state == "complete") {
    console.log("[WebApp] DOM already ready (", state, ") → starting immediately")
    startApp()
  } else {
    console.log("[WebApp] Waiting for DOMContentLoaded, current state:", state)
    document.addEventListener("DOMContentLoaded", { startApp() })
  }
}

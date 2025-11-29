import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import org.w3c.dom.HTMLElement

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
  console.log("[WebApp] main() entered")
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

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLElement

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    window.onload = {
        try {
            val root = document.getElementById("ComposeTarget") as HTMLElement
            ComposeViewport(root) {
                MainApp()
            }
        } catch (e: Exception) {
            console.error("Failed to start Compose Web app", e)
            document.getElementById("root")?.innerHTML =
                "<div style='padding: 50px; text-align: center;'>❌ Failed to load app: ${e.message}</div>"
        }
    }
}

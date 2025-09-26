import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import at.mocode.clients.app.App
import kotlinx.browser.document
import org.w3c.dom.HTMLElement

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val root = document.getElementById("ComposeTarget") as HTMLElement
    ComposeViewport(root) {
        App()
    }
}

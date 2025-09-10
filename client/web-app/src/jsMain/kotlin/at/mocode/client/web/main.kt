package at.mocode.client.web

import androidx.compose.runtime.Composable
import at.mocode.client.ui.App
import org.jetbrains.compose.web.renderComposable

/**
 * Entry point for the Compose for Web application.
 * Follows the web-app guideline by using the shared App component from commonMain.
 */
fun main() {
    renderComposable(rootElementId = "root") {
        WebApp()
    }
}

@Composable
fun WebApp() {
    // Use the shared App component from commonMain
    // This follows the guideline principle of maximum code reuse
    App()
}

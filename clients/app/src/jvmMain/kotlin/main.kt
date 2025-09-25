import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import at.mocode.clients.app.App

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Meldestelle - Desktop Application"
    ) {
        App()
    }
}

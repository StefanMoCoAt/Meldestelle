import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import at.mocode.client.ui.App

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "MeldestellePro") {
        // Wir rufen hier exakt dieselbe geteilte App() Composable-Funktion auf.
        App()
    }
}

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.unit.dp

fun main() = application {
  Window(
    onCloseRequest = ::exitApplication,
    title = "Meldestelle - Desktop Development",
    state = WindowState(width = 1200.dp, height = 800.dp)
  ) {
    MainApp()
  }
}

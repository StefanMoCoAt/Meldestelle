import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.unit.dp
import at.mocode.shared.di.initKoin
import at.mocode.frontend.core.network.networkModule

fun main() = application {
  // Initialize DI (Koin) with shared modules + network module
  try {
    initKoin { modules(networkModule) }
    println("[DesktopApp] Koin initialized with networkModule")
  } catch (e: Exception) {
    println("[DesktopApp] Koin initialization warning: ${e.message}")
  }
  Window(
    onCloseRequest = ::exitApplication,
    title = "Meldestelle - Desktop Development",
    state = WindowState(width = 1200.dp, height = 800.dp)
  ) {
    MainApp()
  }
}

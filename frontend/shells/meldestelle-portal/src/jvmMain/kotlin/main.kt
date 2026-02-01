import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.unit.dp
import at.mocode.frontend.core.network.networkModule
import at.mocode.frontend.core.auth.di.authModule
import at.mocode.frontend.core.sync.di.syncModule
import at.mocode.ping.feature.di.pingFeatureModule
import at.mocode.frontend.core.localdb.AppDatabase
import at.mocode.frontend.core.localdb.DatabaseProvider
import at.mocode.frontend.core.localdb.localDbModule
import navigation.navigationModule
import kotlinx.coroutines.runBlocking
import org.koin.core.context.startKoin
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

fun main() = application {
  // Initialize DI (Koin) with shared modules + network module
  try {
    // Updated: Only load the consolidated pingFeatureModule from at.mocode.ping.feature.di
    startKoin { modules(networkModule, syncModule, pingFeatureModule, authModule, navigationModule, localDbModule) }
    println("[DesktopApp] Koin initialized with networkModule + authModule + navigationModule + pingFeatureModule + localDbModule")
  } catch (e: Exception) {
    println("[DesktopApp] Koin initialization warning: ${e.message}")
  }

  // Create the local DB once and register it into Koin so feature repositories can resolve it.
  try {
    val provider = org.koin.core.context.GlobalContext.get().get<DatabaseProvider>()
    val db = runBlocking { provider.createDatabase() }
    loadKoinModules(
      module {
        single<AppDatabase> { db }
      }
    )
    println("[DesktopApp] Local DB created and registered in Koin")
  } catch (e: Exception) {
    println("[DesktopApp] Local DB init warning: ${e.message}")
  }
  Window(
    onCloseRequest = ::exitApplication,
    title = "Meldestelle - Desktop Development",
    state = WindowState(width = 1200.dp, height = 800.dp)
  ) {
    MainApp()
  }
}

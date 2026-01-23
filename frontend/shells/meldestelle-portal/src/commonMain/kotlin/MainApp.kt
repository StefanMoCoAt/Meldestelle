import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import at.mocode.clients.shared.navigation.AppScreen
import at.mocode.frontend.core.auth.data.AuthTokenManager
import at.mocode.frontend.core.auth.presentation.LoginScreen
import at.mocode.frontend.core.auth.presentation.LoginViewModel
import at.mocode.ping.feature.presentation.PingScreen
import at.mocode.ping.feature.presentation.PingViewModel
import at.mocode.frontend.core.designsystem.components.AppFooter
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MainApp() {
  MaterialTheme {
    Surface(
      modifier = Modifier.fillMaxSize(),
      color = MaterialTheme.colorScheme.background
    ) {
      var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Landing) }

      // Resolve AuthTokenManager from Koin
      val authTokenManager = koinInject<AuthTokenManager>()

      // Delta-Sync blueprint: resolve the Ping feature view model via Koin.
      val pingViewModel: PingViewModel = koinViewModel()
      val loginViewModel: LoginViewModel = koinViewModel()

      when (currentScreen) {
        is AppScreen.Landing -> LandingScreen(
          onPrimaryCta = { currentScreen = AppScreen.Login },
          onSecondary = { currentScreen = AppScreen.Home }
        )
        is AppScreen.Home -> WelcomeScreen(
          authTokenManager = authTokenManager,
          onOpenPing = { currentScreen = AppScreen.Ping },
          onOpenLogin = { currentScreen = AppScreen.Login },
          onOpenProfile = { currentScreen = AppScreen.Profile }
        )

        is AppScreen.Login -> LoginScreen(
          viewModel = loginViewModel,
          onLoginSuccess = { currentScreen = AppScreen.Profile },
          onBack = { currentScreen = AppScreen.Home }
        )

        is AppScreen.Ping -> PingScreen(
          viewModel = pingViewModel,
          onBack = { currentScreen = AppScreen.Home } // Navigate back to Home
        )

        is AppScreen.Profile -> AuthStatusScreen(
          authTokenManager = authTokenManager,
          onBackToHome = { currentScreen = AppScreen.Home }
        )

        else -> {}
      }
    }
  }
}

@Composable
private fun LandingScreen(
  onPrimaryCta: () -> Unit,
  onSecondary: () -> Unit
) {
  val scrollState = rememberScrollState()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
  ) {
    // Hero
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp, vertical = 40.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Text(
        text = "Equest‑Events",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
      )

      Text(
        text = "Die kompetente Turnier‑Meldestelle.",
        style = MaterialTheme.typography.headlineLarge
      )
      Text(
        text = "Equest‑Events entwickelt die digitale Infrastruktur des österreichischen Pferdesports – aus der Praxis. Für die Praxis.",
        style = MaterialTheme.typography.bodyLarge
      )
      Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(onClick = onPrimaryCta) { Text("Anmelden (Pilot‑Partner)") }
        TextButton(onClick = onSecondary) { Text("Mehr erfahren") }
      }
    }

    // Manifest
    Surface(color = MaterialTheme.colorScheme.surfaceVariant) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp, vertical = 40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        Text("Unser Anspruch: Durchdachtes System.", style = MaterialTheme.typography.headlineMedium)
        Text(
          "Die Meldestelle ist das Herzstück jedes Turniers. Wenn sie stolpert, stockt der Sport. Wir verstehen den Balanceakt zwischen Veranstaltern, Reitern und den Verbänden.",
          style = MaterialTheme.typography.bodyLarge
        )
        Text(
          "Deshalb entwickeln wir Equest‑Events nicht am Reißbrett, sondern direkt am Turnier – aus der Sicht der Meldestelle, der Richter, der Zeitnehmer und aller Funktionäre.",
          style = MaterialTheme.typography.bodyLarge
        )
        Text(
          "Aktuell befindet sich unser System in einer Pilotphase für C‑ und C‑NEU‑Turniere. Wir wachsen organisch – Seite an Seite mit unseren Pilot‑Partnern.",
          style = MaterialTheme.typography.bodyLarge
        )
        Text(
          "Jedes Feedback fließt direkt in die Entwicklung ein, um eine Lösung zu schaffen, die den realen Bedürfnissen vor Ort entspricht.",
          style = MaterialTheme.typography.bodyLarge
        )
      }
    }

    // Features
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp, vertical = 40.dp),
      verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
      Text("Die drei Säulen", style = MaterialTheme.typography.headlineMedium)
      Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        FeatureCard(
          number = "01",
          title = "Regelwerks‑Intelligenz (FEI & ÖTO)",
          body = "Wir verbinden ÖTO und FEI – und nehmen Ihnen die Validierungs‑Komplexität ab. Von der Lizenzprüfung bis zur korrekten Anwendung der Bestimmungen."
        )
        FeatureCard(
          number = "02",
          title = "Plattformunabhängig & Offline‑fähig",
          body = "Stabil auf Laptop und mobil. Dank Offline‑Unterstützung arbeiten Sie nahtlos weiter – selbst wenn die Internetverbindung am Platz abreißt."
        )
        FeatureCard(
          number = "03",
          title = "Fokus auf den Sport",
          body = "Wir reduzieren Administration dort, wo es sinnvoll ist – damit sich alle auf das Wesentliche konzentrieren können: den Reitsport."
        )
      }
    }

    // Footer
    AppFooter()
  }
}

@Composable
private fun FeatureCard(number: String, title: String, body: String) {
  Surface( tonalElevation = 0.dp ) {
    Row(modifier = Modifier.fillMaxWidth()) {
      Column(modifier = Modifier.width(56.dp).padding(top = 6.dp)) {
        Text(text = number, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
      }
      Column(modifier = Modifier.weight(1f)) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(4.dp))
        Text(body, style = MaterialTheme.typography.bodyLarge)
      }
    }
  }
}

@Composable
private fun WelcomeScreen(
  authTokenManager: AuthTokenManager,
  onOpenPing: () -> Unit,
  onOpenLogin: () -> Unit,
  onOpenProfile: () -> Unit
) {
  val authState by authTokenManager.authState.collectAsState()
  val scrollState = rememberScrollState()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .padding(24.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Text(
      text = "Willkommen zur Meldestelle",
      style = MaterialTheme.typography.headlineMedium
    )

    // Auth info
    Card(modifier = Modifier.fillMaxWidth()) {
      Column(modifier = Modifier.padding(16.dp)) {
        if (authState.isAuthenticated) {
          Text("Du bist als ${authState.username ?: authState.userId ?: "unbekannt"} angemeldet.")
          Spacer(Modifier.height(8.dp))
          Button(onClick = onOpenProfile) { Text("Profil anzeigen") }
        } else {
          Text("Du bist nicht angemeldet.")
        }
      }
    }

    // Actions
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      Button(onClick = onOpenPing, modifier = Modifier.weight(1f)) { Text("Ping-Service") }
      if (!authState.isAuthenticated) {
        Button(
          onClick = onOpenLogin,
          modifier = Modifier.weight(1f)
        ) { Text("Login") }
      }
    }
  }
}

@Composable
private fun AuthStatusScreen(
  authTokenManager: AuthTokenManager,
  onBackToHome: () -> Unit
) {
  val authState by authTokenManager.authState.collectAsState()
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(24.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Text("Profil / Status", style = MaterialTheme.typography.headlineMedium)
    Card(modifier = Modifier.fillMaxWidth()) {
      Column(modifier = Modifier.padding(16.dp)) {
        if (authState.isAuthenticated) {
          Text("Du bist als ${authState.username ?: authState.userId ?: "unbekannt"} angemeldet.")
          Spacer(Modifier.height(8.dp))
          Button(onClick = {
            authTokenManager.clearToken()
            onBackToHome()
          }) { Text("Abmelden") }

          Spacer(Modifier.height(8.dp))
          OutlinedButton(onClick = onBackToHome) { Text("Zurück zur Startseite") }
        } else {
          Text("Nicht angemeldet.")
          Spacer(Modifier.height(8.dp))
          Button(onClick = onBackToHome) { Text("Zurück zur Startseite") }
        }
      }
    }
  }
}

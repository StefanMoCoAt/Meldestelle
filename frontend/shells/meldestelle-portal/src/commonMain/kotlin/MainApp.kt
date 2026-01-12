import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import at.mocode.clients.shared.navigation.AppScreen
import at.mocode.clients.authfeature.AuthTokenManager
import at.mocode.clients.pingfeature.PingScreen
import at.mocode.ping.feature.presentation.PingViewModel
import at.mocode.shared.core.AppConstants
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.text.input.PasswordVisualTransformation
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import at.mocode.clients.authfeature.AuthApiClient
import at.mocode.clients.authfeature.LoginViewModel
import at.mocode.clients.authfeature.oauth.OAuthPkceService
import at.mocode.clients.authfeature.oauth.AuthCallbackParams
import at.mocode.clients.authfeature.oauth.CallbackParams
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
      val authApiClient = koinInject<AuthApiClient>()
      // Delta-Sync blueprint: resolve the Ping feature view model via Koin.
      val pingViewModel: PingViewModel = koinViewModel()
      val scope = rememberCoroutineScope()

      // Handle PKCE callback on an app load (web)
      LaunchedEffect(Unit) {
        val callback: CallbackParams? = AuthCallbackParams.parse()
        if (callback != null) {
          val code = callback.code
          val state = callback.state
          val pkce = OAuthPkceService.current()
          if (pkce != null && pkce.state == state) {
            val res = authApiClient.exchangeAuthorizationCode(code, pkce.codeVerifier, AppConstants.webRedirectUri())
            val token = res.token
            if (res.success && token != null) {
              authTokenManager.setToken(token)
              OAuthPkceService.clear()
              currentScreen = AppScreen.Profile
            }
          }
        }
      }

      when (currentScreen) {
        is AppScreen.Landing -> LandingScreen(
          onPrimaryCta = { currentScreen = AppScreen.Login },
          onSecondary = { currentScreen = AppScreen.Home }
        )
        is AppScreen.Home -> WelcomeScreen(
          authTokenManager = authTokenManager,
          onOpenPing = { currentScreen = AppScreen.Ping },
          onOpenLogin = {
            // Fallback to the local LoginScreen (Password Grant) if PKCE cannot be started
            currentScreen = AppScreen.Login
          },
          onOpenProfile = { currentScreen = AppScreen.Profile }
        )

        is AppScreen.Login -> LoginScreen(
          onLoginSuccess = { currentScreen = AppScreen.Profile }
        )

        is AppScreen.Ping -> PingScreen(viewModel = pingViewModel)
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
  // Minimal Landing-Layout: Hero → Manifest → Features → Footer
  Column(modifier = Modifier.fillMaxSize()) {
    // Hero
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp, vertical = 40.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Wortmarke (Platzhalter)
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
    at.mocode.clients.shared.commonui.components.AppFooter()
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
  val uriHandler = LocalUriHandler.current
  val scope = rememberCoroutineScope()

  Column(
    modifier = Modifier
      .fillMaxSize()
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
          onClick = {
            // Try PKCE login (Authorization Code Flow w/ PKCE)
            scope.launch {
              try {
                val pkce = OAuthPkceService.startAuth()
                val url = OAuthPkceService.buildAuthorizeUrl(pkce, AppConstants.webRedirectUri())
                uriHandler.openUri(url)
              } catch (_: Throwable) {
                // Fallback: open the local Login screen (Password Grant)
                onOpenLogin()
              }
            }
          },
          modifier = Modifier.weight(1f)
        ) { Text("Login") }
      }
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      OutlinedButton(
        onClick = { uriHandler.openUri(AppConstants.registerUrl()) },
        modifier = Modifier.weight(1f)
      ) { Text("Registrieren (Keycloak)") }

      OutlinedButton(
        onClick = { uriHandler.openUri(AppConstants.loginUrl()) },
        modifier = Modifier.weight(1f)
      ) { Text("Keycloak Login-Seite") }
    }

    // Desktop Download Link
    OutlinedButton(
      onClick = { uriHandler.openUri(AppConstants.desktopDownloadUrl()) },
      modifier = Modifier.fillMaxWidth()
    ) { Text("Desktop-App herunterladen") }
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
        } else {
          Text("Nicht angemeldet.")
          Spacer(Modifier.height(8.dp))
          Button(onClick = onBackToHome) { Text("Zurück zur Startseite") }
        }
      }
    }
  }
}

@Composable
private fun LoginScreen(
  onLoginSuccess: () -> Unit
) {
  val viewModel = koinViewModel<LoginViewModel>()
  val uiState by viewModel.uiState.collectAsState()

  LaunchedEffect(uiState.isAuthenticated) {
    if (uiState.isAuthenticated) {
      onLoginSuccess()
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(24.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    Text("Anmeldung", style = MaterialTheme.typography.headlineMedium)

    OutlinedTextField(
      value = uiState.username,
      onValueChange = { viewModel.updateUsername(it) },
      label = { Text("Benutzername") },
      singleLine = true,
      enabled = !uiState.isLoading,
      isError = uiState.usernameError != null,
      modifier = Modifier.fillMaxWidth()
    )
    if (uiState.usernameError != null) {
      Text(uiState.usernameError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }

    OutlinedTextField(
      value = uiState.password,
      onValueChange = { viewModel.updatePassword(it) },
      label = { Text("Passwort") },
      singleLine = true,
      enabled = !uiState.isLoading,
      visualTransformation = PasswordVisualTransformation(),
      isError = uiState.passwordError != null,
      modifier = Modifier.fillMaxWidth()
    )
    if (uiState.passwordError != null) {
      Text(uiState.passwordError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }

    if (uiState.errorMessage != null) {
      Text(uiState.errorMessage!!, color = MaterialTheme.colorScheme.error)
    }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      Button(
        onClick = { viewModel.login() },
        enabled = uiState.canLogin
      ) { Text(if (uiState.isLoading) "Bitte warten…" else "Login") }
    }
  }
}

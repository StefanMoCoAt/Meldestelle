import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import at.mocode.clients.shared.navigation.AppScreen
import at.mocode.clients.authfeature.AuthTokenManager
import org.koin.core.context.GlobalContext
import at.mocode.clients.pingfeature.PingScreen
import at.mocode.clients.pingfeature.PingViewModel
import at.mocode.shared.core.AppConstants
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.text.input.PasswordVisualTransformation
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import at.mocode.clients.authfeature.AuthApiClient
import at.mocode.clients.authfeature.oauth.OAuthPkceService
import at.mocode.clients.authfeature.oauth.AuthCallbackParams
import at.mocode.clients.authfeature.oauth.CallbackParams

@Composable
fun MainApp() {
  MaterialTheme {
    Surface(
      modifier = Modifier.fillMaxSize(),
      color = MaterialTheme.colorScheme.background
    ) {
      var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Home) }

      // Resolve AuthTokenManager from Koin
      val authTokenManager = remember { GlobalContext.get().koin.get<AuthTokenManager>() }
      val pingViewModel = remember { PingViewModel() }
      val scope = rememberCoroutineScope()

      // Handle PKCE callback on an app load (web)
      LaunchedEffect(Unit) {
        val callback: CallbackParams? = AuthCallbackParams.parse()
        if (callback != null) {
          val code = callback.code
          val state = callback.state
          val pkce = OAuthPkceService.current()
          if (pkce != null && pkce.state == state) {
            val api = AuthApiClient()
            val res = api.exchangeAuthorizationCode(code, pkce.codeVerifier, AppConstants.webRedirectUri())
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
        is AppScreen.Home -> WelcomeScreen(
          authTokenManager = authTokenManager,
          onOpenPing = { AppScreen.Ping },
          onOpenLogin = {
            // Fallback to the local LoginScreen (Password Grant) if PKCE cannot be started
            currentScreen = AppScreen.Login
          },
          onOpenProfile = { currentScreen = AppScreen.Profile }
        )

        is AppScreen.Login -> LoginScreen(
          authTokenManager = authTokenManager,
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
  authTokenManager: AuthTokenManager,
  onLoginSuccess: () -> Unit
) {
  var username by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var error by remember { mutableStateOf<String?>(null) }
  var isLoading by remember { mutableStateOf(false) }
  val scope = rememberCoroutineScope()
  val api = remember { AuthApiClient() }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(24.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    Text("Anmeldung", style = MaterialTheme.typography.headlineMedium)

    OutlinedTextField(
      value = username,
      onValueChange = { username = it },
      label = { Text("Benutzername") },
      singleLine = true,
      enabled = !isLoading,
      modifier = Modifier.fillMaxWidth()
    )

    OutlinedTextField(
      value = password,
      onValueChange = { password = it },
      label = { Text("Passwort") },
      singleLine = true,
      enabled = !isLoading,
      visualTransformation = PasswordVisualTransformation(),
      modifier = Modifier.fillMaxWidth()
    )

    error?.let {
      Text(it, color = MaterialTheme.colorScheme.error)
    }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      Button(
        onClick = {
          error = null
          isLoading = true
          scope.launch {
            val res = api.login(username.trim(), password)
            val token = res.token
            if (res.success && token != null) {
              authTokenManager.setToken(token)
              isLoading = false
              onLoginSuccess()
            } else {
              isLoading = false
              error = res.message ?: "Login fehlgeschlagen"
            }
          }
        },
        enabled = !isLoading && username.isNotBlank() && password.isNotBlank()
      ) { Text(if (isLoading) "Bitte warten…" else "Login") }
    }
  }
}

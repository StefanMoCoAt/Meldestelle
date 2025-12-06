package at.mocode.clients.authfeature

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
  viewModel: LoginViewModel,
  onLoginSuccess: () -> Unit = {}
) {
  val uiState by viewModel.uiState.collectAsState()
  val passwordFocusRequester = remember { FocusRequester() }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    // Title
    Text(
      text = "Anmelden",
      style = MaterialTheme.typography.headlineMedium,
      color = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.padding(bottom = 32.dp)
    )

    // Username field
    OutlinedTextField(
      value = uiState.username,
      onValueChange = viewModel::updateUsername,
      label = { Text("Benutzername") },
      enabled = !uiState.isLoading,
      isError = uiState.usernameError != null,
      supportingText = uiState.usernameError?.let { { Text(it) } },
      keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Text,
        imeAction = ImeAction.Next
      ),
      keyboardActions = KeyboardActions(
        onNext = { passwordFocusRequester.requestFocus() }
      ),
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 16.dp)
    )

    // Password field
    OutlinedTextField(
      value = uiState.password,
      onValueChange = viewModel::updatePassword,
      label = { Text("Passwort") },
      enabled = !uiState.isLoading,
      isError = uiState.passwordError != null,
      supportingText = uiState.passwordError?.let { { Text(it) } },
      visualTransformation = PasswordVisualTransformation(),
      keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Password,
        imeAction = ImeAction.Done
      ),
      keyboardActions = KeyboardActions(
        onDone = {
          if (uiState.canLogin) {
            viewModel.login()
          }
        }
      ),
      modifier = Modifier
        .fillMaxWidth()
        .focusRequester(passwordFocusRequester)
        .padding(bottom = 24.dp)
    )

    // Error message
    if (uiState.errorMessage != null) {
      Card(
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 16.dp)
      ) {
        Text(
          text = uiState.errorMessage!!,
          color = MaterialTheme.colorScheme.onErrorContainer,
          style = MaterialTheme.typography.bodyMedium,
          textAlign = TextAlign.Center,
          modifier = Modifier.padding(16.dp)
        )
      }
    }

    // Login button
    Button(
      onClick = { viewModel.login() },
      enabled = uiState.canLogin && !uiState.isLoading,
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp)
    ) {
      if (uiState.isLoading) {
        CircularProgressIndicator(
          modifier = Modifier.size(20.dp),
          strokeWidth = 2.dp,
          color = MaterialTheme.colorScheme.onPrimary
        )
      } else {
        Text("Anmelden")
      }
    }
  }

  // Handle login success
  LaunchedEffect(uiState.isAuthenticated) {
    if (uiState.isAuthenticated) {
      onLoginSuccess()
    }
  }
}

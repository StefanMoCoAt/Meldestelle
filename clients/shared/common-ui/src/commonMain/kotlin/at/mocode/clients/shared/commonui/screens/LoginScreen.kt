package at.mocode.clients.shared.commonui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import at.mocode.clients.shared.commonui.components.*
import at.mocode.clients.shared.presentation.actions.AppAction
import at.mocode.clients.shared.presentation.state.AuthState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    authState: AuthState,
    onLoginClick: (String, String) -> Unit,
    onNavigateToRegister: () -> Unit = {},
    onForgotPassword: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var usernameError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    val focusManager = LocalFocusManager.current

    // Validate form
    val isFormValid = username.isNotBlank() && password.isNotBlank() &&
                     usernameError == null && passwordError == null

    fun validateUsername(value: String) {
        usernameError = FormValidation.validateRequired(value, "Username")
    }

    fun validatePassword(value: String) {
        passwordError = FormValidation.validatePassword(value)
    }

    fun handleLogin() {
        validateUsername(username)
        validatePassword(password)

        if (isFormValid) {
            onLoginClick(username.trim(), password)
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Text(
                    text = "Meldestelle",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Sign in to your account",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Username Field
                MeldestelleTextField(
                    value = username,
                    onValueChange = {
                        username = it
                        if (usernameError != null) validateUsername(it)
                    },
                    label = "Username",
                    placeholder = "Enter your username",
                    isError = usernameError != null,
                    errorMessage = usernameError,
                    enabled = !authState.isLoading,
                    imeAction = ImeAction.Next,
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )

                // Password Field
                MeldestellePasswordField(
                    value = password,
                    onValueChange = {
                        password = it
                        if (passwordError != null) validatePassword(it)
                    },
                    label = "Password",
                    placeholder = "Enter your password",
                    isError = passwordError != null,
                    errorMessage = passwordError,
                    enabled = !authState.isLoading,
                    imeAction = ImeAction.Done,
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            if (isFormValid) handleLogin()
                        }
                    )
                )

                // Error display
                authState.error?.let { errorMessage ->
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Login Button
                PrimaryButton(
                    text = "Sign In",
                    onClick = ::handleLogin,
                    enabled = isFormValid && !authState.isLoading,
                    isLoading = authState.isLoading,
                    fullWidth = true
                )

                // Forgot Password
                TextButton(
                    onClick = onForgotPassword,
                    enabled = !authState.isLoading
                ) {
                    Text("Forgot Password?")
                }

                HorizontalDivider()

                // Register Link
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Don't have an account?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    TextButton(
                        onClick = onNavigateToRegister,
                        enabled = !authState.isLoading
                    ) {
                        Text("Sign Up")
                    }
                }
            }
        }
    }
}

@Composable
fun LoginScreenContainer(
    authState: AuthState,
    onDispatchAction: (AppAction) -> Unit,
    onNavigateToRegister: () -> Unit = {},
    onForgotPassword: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    LoginScreen(
        authState = authState,
        onLoginClick = { username, password ->
            onDispatchAction(AppAction.Auth.LoginStart(username, password))
        },
        onNavigateToRegister = onNavigateToRegister,
        onForgotPassword = onForgotPassword,
        modifier = modifier
    )
}

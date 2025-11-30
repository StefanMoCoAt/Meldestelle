package at.mocode.clients.shared.commonui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun MeldestelleTextField(
  value: String,
  onValueChange: (String) -> Unit,
  modifier: Modifier = Modifier,
  label: String? = null,
  placeholder: String? = null,
  leadingIcon: ImageVector? = null,
  trailingIcon: ImageVector? = null,
  onTrailingIconClick: (() -> Unit)? = null,
  isError: Boolean = false,
  errorMessage: String? = null,
  helperText: String? = null,
  enabled: Boolean = true,
  readOnly: Boolean = false,
  singleLine: Boolean = true,
  maxLines: Int = Int.MAX_VALUE,
  keyboardType: KeyboardType = KeyboardType.Text,
  imeAction: ImeAction = ImeAction.Default,
  keyboardActions: KeyboardActions = KeyboardActions.Default,
  visualTransformation: VisualTransformation = VisualTransformation.None
) {
  Column(modifier = modifier) {
    OutlinedTextField(
      value = value,
      onValueChange = onValueChange,
      modifier = Modifier.fillMaxWidth(),
      label = label?.let { { Text(it) } },
      placeholder = placeholder?.let { { Text(it) } },
      leadingIcon = leadingIcon?.let { icon ->
        { Icon(imageVector = icon, contentDescription = null) }
      },
      trailingIcon = if (trailingIcon != null) {
        {
          IconButton(
            onClick = onTrailingIconClick ?: {}
          ) {
            Icon(imageVector = trailingIcon, contentDescription = null)
          }
        }
      } else null,
      isError = isError,
      enabled = enabled,
      readOnly = readOnly,
      singleLine = singleLine,
      maxLines = maxLines,
      keyboardOptions = KeyboardOptions(
        keyboardType = keyboardType,
        imeAction = imeAction
      ),
      keyboardActions = keyboardActions,
      visualTransformation = visualTransformation
    )

    // Error or helper text
    when {
      isError && errorMessage != null -> {
        Text(
          text = errorMessage,
          color = MaterialTheme.colorScheme.error,
          style = MaterialTheme.typography.bodySmall,
          modifier = Modifier.padding(start = 16.dp, top = 4.dp)
        )
      }

      helperText != null -> {
        Text(
          text = helperText,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          style = MaterialTheme.typography.bodySmall,
          modifier = Modifier.padding(start = 16.dp, top = 4.dp)
        )
      }
    }
  }
}

@Composable
fun MeldestellePasswordField(
  value: String,
  onValueChange: (String) -> Unit,
  modifier: Modifier = Modifier,
  label: String = "Password",
  placeholder: String? = null,
  isError: Boolean = false,
  errorMessage: String? = null,
  helperText: String? = null,
  enabled: Boolean = true,
  imeAction: ImeAction = ImeAction.Done,
  keyboardActions: KeyboardActions = KeyboardActions.Default
) {
  var passwordVisible by remember { mutableStateOf(false) }

  MeldestelleTextField(
    value = value,
    onValueChange = onValueChange,
    modifier = modifier,
    label = label,
    placeholder = placeholder,
    trailingIcon = if (passwordVisible) {
      // You would need to import the actual icon from Material Icons
      null // Placeholder for visibility off icon
    } else {
      null // Placeholder for visibility on icon
    },
    onTrailingIconClick = { passwordVisible = !passwordVisible },
    isError = isError,
    errorMessage = errorMessage,
    helperText = helperText,
    enabled = enabled,
    keyboardType = KeyboardType.Password,
    imeAction = imeAction,
    keyboardActions = keyboardActions,
    visualTransformation = if (passwordVisible) {
      VisualTransformation.None
    } else {
      PasswordVisualTransformation()
    }
  )
}

@Composable
fun MeldestelleEmailField(
  value: String,
  onValueChange: (String) -> Unit,
  modifier: Modifier = Modifier,
  label: String = "Email",
  placeholder: String? = null,
  isError: Boolean = false,
  errorMessage: String? = null,
  helperText: String? = null,
  enabled: Boolean = true,
  imeAction: ImeAction = ImeAction.Next,
  keyboardActions: KeyboardActions = KeyboardActions.Default
) {
  MeldestelleTextField(
    value = value,
    onValueChange = onValueChange,
    modifier = modifier,
    label = label,
    placeholder = placeholder,
    isError = isError,
    errorMessage = errorMessage,
    helperText = helperText,
    enabled = enabled,
    keyboardType = KeyboardType.Email,
    imeAction = imeAction,
    keyboardActions = keyboardActions
  )
}

/**
 * Form validation utilities
 */
object FormValidation {
  fun validateEmail(email: String): String? {
    return when {
      email.isEmpty() -> "Email is required"
      !email.contains("@") -> "Invalid email format"
      !email.matches(Regex("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$")) -> "Invalid email format"
      else -> null
    }
  }

  fun validatePassword(password: String): String? {
    return when {
      password.isEmpty() -> "Password is required"
      password.length < 6 -> "Password must be at least 6 characters"
      else -> null
    }
  }

  fun validateRequired(value: String, fieldName: String): String? {
    return if (value.isEmpty()) "$fieldName is required" else null
  }
}

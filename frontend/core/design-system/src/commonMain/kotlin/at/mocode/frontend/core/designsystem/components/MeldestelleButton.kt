package at.mocode.frontend.core.designsystem.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class ButtonVariant {
  PRIMARY, SECONDARY, OUTLINE, TEXT
}

enum class ButtonSize {
  SMALL, MEDIUM, LARGE
}

@Composable
fun MeldestelleButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  variant: ButtonVariant = ButtonVariant.PRIMARY,
  size: ButtonSize = ButtonSize.MEDIUM,
  enabled: Boolean = true,
  isLoading: Boolean = false,
  fullWidth: Boolean = false
) {
  val buttonModifier = modifier.then(
    if (fullWidth) Modifier.fillMaxWidth() else Modifier
  ).then(
    when (size) {
      ButtonSize.SMALL -> Modifier.height(32.dp)
      ButtonSize.MEDIUM -> Modifier.height(40.dp)
      ButtonSize.LARGE -> Modifier.height(48.dp)
    }
  )

  when (variant) {
    ButtonVariant.PRIMARY -> Button(
      onClick = onClick,
      modifier = buttonModifier,
      enabled = enabled && !isLoading
    ) {
      ButtonContent(text = text, isLoading = isLoading)
    }

    ButtonVariant.SECONDARY -> FilledTonalButton(
      onClick = onClick,
      modifier = buttonModifier,
      enabled = enabled && !isLoading
    ) {
      ButtonContent(text = text, isLoading = isLoading)
    }

    ButtonVariant.OUTLINE -> OutlinedButton(
      onClick = onClick,
      modifier = buttonModifier,
      enabled = enabled && !isLoading
    ) {
      ButtonContent(text = text, isLoading = isLoading)
    }

    ButtonVariant.TEXT -> TextButton(
      onClick = onClick,
      modifier = buttonModifier,
      enabled = enabled && !isLoading
    ) {
      ButtonContent(text = text, isLoading = isLoading)
    }
  }
}

@Composable
private fun ButtonContent(
  text: String,
  isLoading: Boolean
) {
  if (isLoading) {
    CircularProgressIndicator(
      modifier = Modifier.padding(2.dp),
      strokeWidth = 2.dp
    )
  } else {
    Text(text)
  }
}

@Composable
fun PrimaryButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  isLoading: Boolean = false,
  fullWidth: Boolean = false
) = MeldestelleButton(
  text = text,
  onClick = onClick,
  modifier = modifier,
  variant = ButtonVariant.PRIMARY,
  enabled = enabled,
  isLoading = isLoading,
  fullWidth = fullWidth
)

@Composable
fun SecondaryButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  isLoading: Boolean = false,
  fullWidth: Boolean = false
) = MeldestelleButton(
  text = text,
  onClick = onClick,
  modifier = modifier,
  variant = ButtonVariant.SECONDARY,
  enabled = enabled,
  isLoading = isLoading,
  fullWidth = fullWidth
)

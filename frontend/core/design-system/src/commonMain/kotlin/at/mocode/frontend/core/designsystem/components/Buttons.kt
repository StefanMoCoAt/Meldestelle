package at.mocode.frontend.core.designsystem.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import at.mocode.frontend.core.designsystem.theme.Dimens

/**
 * Ein kompakter Button für unsere High-Density UI.
 *
 * Warum ein eigener Button?
 * Der Standard Material3 Button ist sehr hoch (40dp+) und hat viel Padding.
 * Das verschwendet Platz in Tabellen oder Toolbars.
 * Unser 'DenseButton' ist fix 32dp hoch- und hat weniger Innenabstand.
 */
@Composable
fun DenseButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  containerColor: Color = MaterialTheme.colorScheme.primary
) {
  Button(
    onClick = onClick,
    enabled = enabled,
    modifier = modifier.height(32.dp), // Fixe, kompakte Höhe
    shape = MaterialTheme.shapes.small, // Nutzt unsere 4dp Rundung
    colors = ButtonDefaults.buttonColors(containerColor = containerColor),
    contentPadding = PaddingValues(horizontal = Dimens.SpacingM, vertical = 0.dp) // Wenig Padding
  ) {
    Text(
      text = text,
      style = MaterialTheme.typography.labelMedium // Kleinere Schrift
    )
  }
}

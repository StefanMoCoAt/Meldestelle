package at.mocode.frontend.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import at.mocode.frontend.core.designsystem.theme.Dimens

/**
 * Eine flache, umrandete Card für Dashboards.
 *
 * Warum?
 * Standard Cards haben oft Schatten (Elevation), was bei vielen Cards unruhig wirkt.
 * Im Enterprise-Kontext sind flache Cards mit dünnem Border (1px) oft sauberer.
 */
@Composable
fun DashboardCard(
  modifier: Modifier = Modifier,
  content: @Composable ColumnScope.() -> Unit
) {
  Card(
    modifier = modifier,
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), // Dünner Rahmen
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // Kein Schatten
  ) {
    Column(
      modifier = Modifier.padding(Dimens.SpacingS) // Kompaktes Padding innen
    ) {
      content()
    }
  }
}

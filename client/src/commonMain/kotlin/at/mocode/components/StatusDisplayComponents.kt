package at.mocode.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Lazy-loadable Success Card Component
 * Only loaded when a successful ping response is available
 */
@Composable
fun SuccessCard(
    response: PingResponse,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "✅ Ping erfolgreich!",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF4CAF50)
            )
            Text(
                text = "Status: ${response.status}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
            response.timestamp?.let {
                Text(
                    text = "Zeit: $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

/**
 * Lazy-loadable Error Card Component
 * Only loaded when a ping error occurs
 */
@Composable
fun ErrorCard(
    message: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "❌ Ping fehlgeschlagen",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFF44336)
            )
            Text(
                text = "Fehler: $message",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

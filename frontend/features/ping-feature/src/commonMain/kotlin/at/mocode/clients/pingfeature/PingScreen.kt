package at.mocode.clients.pingfeature

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import at.mocode.ping.feature.presentation.PingViewModel

/**
 * Delta-Sync Tracer UI (minimal):
 * The new Ping feature view model focuses on syncing `PingEvent`s into the local DB.
 */
@Composable
fun PingScreen(viewModel: PingViewModel) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Text(
      text = "Ping Delta-Sync",
      style = MaterialTheme.typography.headlineMedium,
      fontWeight = FontWeight.Bold
    )

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      Button(onClick = { viewModel.triggerSync() }) {
        Text("Sync now")
      }
    }

    Text(
      text = "This screen triggers the generic SyncManager against /api/pings/sync and stores events locally.",
      style = MaterialTheme.typography.bodyMedium
    )
  }
}

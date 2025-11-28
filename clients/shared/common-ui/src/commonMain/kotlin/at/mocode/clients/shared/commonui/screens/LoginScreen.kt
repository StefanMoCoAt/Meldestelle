package at.mocode.clients.shared.commonui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Legacy placeholder to avoid compilation issues. The actual Login is implemented
 * in the app module now. This placeholder can be removed once all usages are updated.
 */
@Composable
fun LoginScreenPlaceholder() {
    Card(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Legacy LoginScreen",
                style = MaterialTheme.typography.titleLarge
            )
            Text("Diese Ansicht wurde migriert. Bitte die neue Login-Seite im App-Modul verwenden.")
        }
    }
}

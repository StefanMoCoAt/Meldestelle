package at.mocode.client.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import at.mocode.client.ui.features.ping.PingScreen

@Composable
fun App() {
    MaterialTheme {
        PingScreen()
    }
}

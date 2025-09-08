package at.mocode.client.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import at.mocode.client.ui.App

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Meldestelle Desktop App",
        state = WindowState(
            position = WindowPosition(Alignment.Center),
            width = 800.dp,
            height = 600.dp
        )
    ) {
        // Use the shared App component from common-ui
        // This eliminates code duplication and ensures consistent UI across platforms
        App(baseUrl = System.getProperty("meldestelle.api.url", "http://localhost:8081"))
    }
}

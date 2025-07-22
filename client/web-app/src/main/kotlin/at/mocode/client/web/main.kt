package at.mocode.client.web

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        title = "Meldestelle - Reitersport Management",
        onCloseRequest = ::exitApplication
    ) {
        App()
    }
}

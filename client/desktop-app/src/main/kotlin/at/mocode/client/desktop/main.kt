package at.mocode.client.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Meldestelle - Reitersport Management"
    ) {
        App()
    }
}

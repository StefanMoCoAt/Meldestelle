package at.mocode

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow

@OptIn(ExperimentalComposeUiApi::class)
fun main() {

//    ComposeViewport(document.getElementById("ComposeTarget")!!) {
//        App()
//    }

    CanvasBasedWindow(canvasElementId = "ComposeTarget") {
        App()
    }
}

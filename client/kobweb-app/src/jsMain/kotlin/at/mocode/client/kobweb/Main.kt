package at.mocode.client.kobweb

import androidx.compose.runtime.*
import com.varabyte.kobweb.core.App
import com.varabyte.kobweb.silk.SilkApp
import com.varabyte.kobweb.silk.components.layout.Surface
import com.varabyte.kobweb.silk.init.InitSilk
import com.varabyte.kobweb.silk.init.InitSilkContext
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.minHeight
import org.jetbrains.compose.web.css.vh

@InitSilk
fun initSilk(ctx: InitSilkContext) {
    // You can configure your app here.
    // This will be called once when your app starts up.
    //
    // As an example, you can use `ctx.stylesheet` to add styles,
    // or `ctx.theme` to modify colors, fonts, etc.
}

@App
@Composable
fun MyApp(content: @Composable () -> Unit) {
    SilkApp {
        Surface(modifier = Modifier.minHeight(100.vh)) {
            content()
        }
    }
}

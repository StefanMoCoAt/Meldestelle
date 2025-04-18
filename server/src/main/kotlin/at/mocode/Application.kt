package at.mocode

import at.mocode.plugins.configureDatabase
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {

    // Als Erstes die Datenbank konfigurieren:
    configureDatabase()

    // Danach deine anderen Konfigurationen (Routing etc.):
    routing {
        get("/") {
            call.respondText("Ktor: ${Greeting().greet()}")
        }
    }
}

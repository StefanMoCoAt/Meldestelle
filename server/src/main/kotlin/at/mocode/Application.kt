package at.mocode

import at.mocode.plugins.configureDatabase
import at.mocode.routes.configureAdminRoutes
import at.mocode.routes.configureHomeRoutes
import at.mocode.routes.configureNennungRoutes
import io.ktor.server.application.*
import io.ktor.server.netty.*



fun main(args: Array<String>) {
    EngineMain.main(args)
}

/**
 * Application module configuration.
 */
fun Application.module() {
    // Configure database first
    configureDatabase()

    // Configure routes
    configureHomeRoutes()
    configureNennungRoutes()
    configureAdminRoutes()
}

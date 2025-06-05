package at.mocode

import at.mocode.plugins.configureDatabase
import at.mocode.routes.configureAdminRoutes
import at.mocode.routes.configureHomeRoutes
import at.mocode.routes.configureNennungRoutes
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*


fun main(args: Array<String>) {
    EngineMain.main(args)
}

/**
 * Application module configuration.
 */
fun Application.module() {
    // Configure database first
    configureDatabase()

    // Configure static resources
    configureStaticResources()

    // Configure routes
    configureHomeRoutes()
    configureNennungRoutes()
    configureAdminRoutes()
}

/**
 * Configure static resources.
 */
fun Application.configureStaticResources() {
    routing {
        staticResources(remotePath = "/css", basePackage = "static/css")
        staticResources(remotePath = "/js", basePackage = "static/js")
        staticResources(remotePath = "/images", basePackage = "static/images")
    }
}

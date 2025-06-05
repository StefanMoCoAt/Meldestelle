package at.mocode.routes

import at.mocode.config.DependencyInjection
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory

/**
 * Configures routes for the home page.
 */
fun Route.homeRoutes() {
    val log = LoggerFactory.getLogger("HomeRoutes")
    val turnierRepository = DependencyInjection.turnierRepository
    val homeView = DependencyInjection.homeView

    get("/") {
        log.info("Handling request to home page")

        // Insert dummy tournament if needed
        turnierRepository.insertDummyTurnierIfEmpty()

        // Get all tournaments
        val turniere = turnierRepository.getAllTurniere()

        // Render the home page
        homeView.renderHomePage(call, turniere)
    }
}

/**
 * Extension function to register home routes.
 */
fun Application.configureHomeRoutes() {
    routing {
        homeRoutes()
    }
}

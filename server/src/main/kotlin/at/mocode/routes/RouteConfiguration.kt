package at.mocode.routes

import io.ktor.server.routing.*

/**
 * Centralized route configuration that organizes all API routes
 * by domain and functionality for better maintainability
 */
object RouteConfiguration {

    /**
     * Configure all API routes in a structured manner
     */
    fun Route.configureApiRoutes() {
        route("/api") {
            // Core domain routes
            configureCoreRoutes()

            // Domain-specific routes
            configureDomainRoutes()

            // Event/Tournament management routes
            configureEventRoutes()
        }
    }

    /**
     * Configure core domain routes (Person, Verein, etc.)
     */
    private fun Route.configureCoreRoutes() {
        // Person and organization management
        personRoutes()
        vereinRoutes()

        // Articles and products
        artikelRoutes()
    }

    /**
     * Configure domain-specific routes (licenses, horses, qualifications)
     */
    private fun Route.configureDomainRoutes() {
        route("/domain") {
            domLizenzRoutes()
            domPferdRoutes()
            domQualifikationRoutes()
        }
    }

    /**
     * Configure event and tournament management routes
     */
    private fun Route.configureEventRoutes() {
        route("/events") {
            // Event hierarchy: Veranstaltung -> Turnier -> Bewerb -> Abteilung
            veranstaltungRoutes()
            turnierRoutes()
            bewerbRoutes()
            abteilungRoutes()
        }
    }

    /**
     * Configure administrative and utility routes
     */
    private fun Route.configureAdminRoutes() {
        route("/admin") {
            // Future: Admin-specific endpoints
            // userManagementRoutes()
            // systemConfigRoutes()
            // auditLogRoutes()
        }
    }

    /**
     * Configure public/external API routes
     */
    private fun Route.configurePublicRoutes() {
        route("/public") {
            // Future: Public endpoints that don't require authentication
            // publicEventListRoutes()
            // publicResultsRoutes()
        }
    }
}

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
            // Version-agnostic routes (always use latest version)
            configureCoreRoutes()
            configureDomainRoutes()
            configureEventRoutes()
            configureDomainEventRoutes()

            // Versioned API routes
            route("/v1") {
                configureCoreRoutes()
                configureDomainRoutes()
                configureEventRoutes()
                configureDomainEventRoutes()
            }

            // Future versions can be added here
            // route("/v2") {
            //     configureV2Routes()
            // }
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

            // Places/Venues for events
            platzRoutes()
        }
    }

    /**
     * Configure domain event routes (event sourcing)
     */
    private fun Route.configureDomainEventRoutes() {
        // Domain events API for event sourcing
        eventRoutes()
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

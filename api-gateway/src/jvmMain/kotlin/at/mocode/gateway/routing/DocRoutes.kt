package at.mocode.gateway.routing

import at.mocode.dto.base.ApiResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.io.File

/**
 * Sets up routes for API documentation
 */
fun Routing.docRoutes() {
    // Central API documentation endpoint - HTML version
    get("/api") {
        call.respondRedirect("/docs", permanent = false)
    }

    // JSON API documentation endpoint for backward compatibility
    get("/api/json") {
        val apiDocumentation = ApiDocumentationData(
            title = "Meldestelle Self-Contained Systems API",
            description = "Unified API Gateway for all bounded contexts",
            contexts = listOf(
                ApiContext(
                    name = "Authentication Context",
                    path = "/auth",
                    description = "User authentication, registration, and profile management"
                ),
                ApiContext(
                    name = "Master Data Context",
                    path = "/api/masterdata",
                    description = "Reference data management (countries, states, age classes, venues)"
                ),
                ApiContext(
                    name = "Horse Registry Context",
                    path = "/api/horses",
                    description = "Horse registration, ownership, and pedigree management"
                ),
                ApiContext(
                    name = "Event Management Context",
                    path = "/api/events",
                    description = "Event creation, management, and participant registration"
                )
            )
        )

        call.respond(
            ApiResponse.success(
                data = apiDocumentation,
                message = "API documentation retrieved successfully"
            )
        )
    }
}

/**
 * Data class for API documentation response
 */
@Serializable
data class ApiDocumentationData(
    val title: String,
    val description: String,
    val contexts: List<ApiContext>
)

/**
 * Data class for API context information
 */
@Serializable
data class ApiContext(
    val name: String,
    val path: String,
    val description: String
)

package at.mocode.infrastructure.gateway.routing

import at.mocode.infrastructure.gateway.discovery.ServiceDiscovery
import at.mocode.core.utils.config.AppConfig
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

/**
 * Simple error response for service routing errors
 */
@Serializable
data class ServiceErrorResponse(
    val error: String,
    val code: String,
    val service: String? = null
)

/**
 * Simple success response for service routing
 */
@Serializable
data class ServiceSuccessResponse(
    val message: String,
    val service: String,
    val instance: ServiceInstanceInfo
)

@Serializable
data class ServiceInstanceInfo(
    val id: String,
    val name: String,
    val host: String,
    val port: Int
)

/**
 * Configure dynamic service routing using Consul service discovery.
 * This allows the API Gateway to discover services registered with Consul and route requests to them.
 */
fun Routing.serviceRoutes() {
    val config = AppConfig

    // Check if we're in a test environment
    val isTestEnvironment = System.getProperty("kotlinx.coroutines.test") != null ||
                           Thread.currentThread().stackTrace.any { it.className.contains("test", ignoreCase = true) }

    // Initialize service discovery if enabled and not in test environment
    val serviceDiscovery = if (config.serviceDiscovery.enabled && !isTestEnvironment) {
        try {
            ServiceDiscovery(
                consulHost = config.serviceDiscovery.consulHost,
                consulPort = config.serviceDiscovery.consulPort
            )
        } catch (e: Exception) {
            // If service discovery fails to initialize, log and continue without it
            println("Service discovery initialization failed: ${e.message}")
            null
        }
    } else null

    // Define service routes
    // Master Data Service Routes
    route("/api/masterdata") {
        handle {
            handleServiceRequest(call, "master-data", serviceDiscovery)
        }
    }

    // Horse Registry Service Routes
    route("/api/horses") {
        handle {
            handleServiceRequest(call, "horse-registry", serviceDiscovery)
        }
    }

    // Event Management Service Routes
    route("/api/events") {
        handle {
            handleServiceRequest(call, "event-management", serviceDiscovery)
        }
    }

    // Member Management Service Routes
    route("/api/members") {
        handle {
            handleServiceRequest(call, "member-management", serviceDiscovery)
        }
    }
}

/**
 * Handle a service request by discovering the service and forwarding the request.
 * This is a simplified implementation that just returns service information.
 * In a production environment, this would forward the request to the service.
 */
private suspend fun handleServiceRequest(
    call: ApplicationCall,
    serviceName: String,
    serviceDiscovery: ServiceDiscovery?
) {
    try {
        // Check if service discovery is available
        if (serviceDiscovery == null) {
            val errorResponse = ServiceErrorResponse(
                error = "Service discovery is not available",
                code = "SERVICE_DISCOVERY_DISABLED"
            )
            call.respond(HttpStatusCode.ServiceUnavailable, errorResponse)
            return
        }

        // Get service instance
        val serviceInstance = serviceDiscovery.getServiceInstance(serviceName)

        if (serviceInstance == null) {
            val errorResponse = ServiceErrorResponse(
                error = "Service $serviceName is not available",
                code = "SERVICE_NOT_FOUND",
                service = serviceName
            )
            call.respond(HttpStatusCode.ServiceUnavailable, errorResponse)
            return
        }

        // Respond with service information
        val successResponse = ServiceSuccessResponse(
            message = "Service discovery working",
            service = serviceName,
            instance = ServiceInstanceInfo(
                id = serviceInstance.id,
                name = serviceInstance.name,
                host = serviceInstance.host,
                port = serviceInstance.port
            )
        )
        call.respond(HttpStatusCode.OK, successResponse)
    } catch (e: Exception) {
        val errorResponse = ServiceErrorResponse(
            error = "Error routing request to service $serviceName: ${e.message}",
            code = "SERVICE_ERROR",
            service = serviceName
        )
        call.respond(HttpStatusCode.InternalServerError, errorResponse)
    }
}

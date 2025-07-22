package at.mocode.infrastructure.gateway.routing

import at.mocode.infrastructure.gateway.discovery.ServiceDiscovery
import at.mocode.core.utils.config.AppConfig
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Configure dynamic service routing using Consul service discovery.
 * This allows the API Gateway to discover services registered with Consul and route requests to them.
 */
fun Routing.serviceRoutes() {
    val config = AppConfig

    // Initialize service discovery if enabled
    val serviceDiscovery = if (config.serviceDiscovery.enabled) {
        ServiceDiscovery(
            consulHost = config.serviceDiscovery.consulHost,
            consulPort = config.serviceDiscovery.consulPort
        )
    } else null

    // Define service routes
    if (serviceDiscovery != null) {
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
}

/**
 * Handle a service request by discovering the service and forwarding the request.
 * This is a simplified implementation that just returns service information.
 * In a production environment, this would forward the request to the service.
 */
private suspend fun handleServiceRequest(
    call: ApplicationCall,
    serviceName: String,
    serviceDiscovery: ServiceDiscovery
) {
    try {
        // Get service instance
        val serviceInstance = serviceDiscovery.getServiceInstance(serviceName)

        if (serviceInstance == null) {
            call.respond(HttpStatusCode.ServiceUnavailable, "Service $serviceName is not available")
            return
        }

        // Respond with service information
        call.respond(
            HttpStatusCode.OK,
            mapOf(
                "message" to "Service discovery working",
                "service" to serviceName,
                "instance" to mapOf(
                    "id" to serviceInstance.id,
                    "name" to serviceInstance.name,
                    "host" to serviceInstance.host,
                    "port" to serviceInstance.port
                )
            )
        )
    } catch (e: Exception) {
        call.respond(
            HttpStatusCode.InternalServerError,
            "Error routing request to service $serviceName: ${e.message}"
        )
    }
}

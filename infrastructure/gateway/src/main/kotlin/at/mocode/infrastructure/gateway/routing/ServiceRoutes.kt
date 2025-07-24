package at.mocode.infrastructure.gateway.routing

import at.mocode.infrastructure.gateway.discovery.ServiceDiscovery
import at.mocode.core.utils.config.AppConfig
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
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

    // Define service routes with all HTTP methods
    // Master Data Service Routes
    route("/api/masterdata") {
        get("{...}") { handleServiceRequest(call, "master-data", serviceDiscovery) }
        post("{...}") { handleServiceRequest(call, "master-data", serviceDiscovery) }
        put("{...}") { handleServiceRequest(call, "master-data", serviceDiscovery) }
        delete("{...}") { handleServiceRequest(call, "master-data", serviceDiscovery) }
        patch("{...}") { handleServiceRequest(call, "master-data", serviceDiscovery) }
    }

    // Horse Registry Service Routes
    route("/api/horses") {
        get("{...}") { handleServiceRequest(call, "horse-registry", serviceDiscovery) }
        post("{...}") { handleServiceRequest(call, "horse-registry", serviceDiscovery) }
        put("{...}") { handleServiceRequest(call, "horse-registry", serviceDiscovery) }
        delete("{...}") { handleServiceRequest(call, "horse-registry", serviceDiscovery) }
        patch("{...}") { handleServiceRequest(call, "horse-registry", serviceDiscovery) }
    }

    // Event Management Service Routes
    route("/api/events") {
        get("{...}") { handleServiceRequest(call, "event-management", serviceDiscovery) }
        post("{...}") { handleServiceRequest(call, "event-management", serviceDiscovery) }
        put("{...}") { handleServiceRequest(call, "event-management", serviceDiscovery) }
        delete("{...}") { handleServiceRequest(call, "event-management", serviceDiscovery) }
        patch("{...}") { handleServiceRequest(call, "event-management", serviceDiscovery) }
    }

    // Member Management Service Routes
    route("/api/members") {
        get("{...}") { handleServiceRequest(call, "member-management", serviceDiscovery) }
        post("{...}") { handleServiceRequest(call, "member-management", serviceDiscovery) }
        put("{...}") { handleServiceRequest(call, "member-management", serviceDiscovery) }
        delete("{...}") { handleServiceRequest(call, "member-management", serviceDiscovery) }
        patch("{...}") { handleServiceRequest(call, "member-management", serviceDiscovery) }
    }
}

/**
 * HTTP client for forwarding requests to backend services
 */
private val httpClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json()
    }
}

/**
 * Handle a service request by discovering the service and forwarding the request.
 * This implementation forwards the complete HTTP request to the backend service.
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

        // Build target URL
        val targetUrl = "http://${serviceInstance.host}:${serviceInstance.port}${call.request.uri}"

        // Forward the request to the backend service
        val response = httpClient.request(targetUrl) {
            method = call.request.httpMethod

            // Copy all headers except Host and Content-Length (handled automatically)
            call.request.headers.forEach { name, values ->
                if (name.lowercase() !in listOf("host", "content-length")) {
                    values.forEach { value ->
                        header(name, value)
                    }
                }
            }

            // Copy request body if present
            if (call.request.httpMethod in listOf(HttpMethod.Post, HttpMethod.Put, HttpMethod.Patch)) {
                val requestBody = call.receiveText()
                if (requestBody.isNotEmpty()) {
                    setBody(requestBody)
                }
            }
        }

        // Forward the response back to the client
        call.response.status(response.status)

        // Copy response headers
        response.headers.forEach { name, values ->
            if (name.lowercase() !in listOf("content-length", "transfer-encoding")) {
                values.forEach { value ->
                    call.response.header(name, value)
                }
            }
        }

        // Copy response body
        val responseBody = response.bodyAsText()
        call.respondText(responseBody, response.contentType())

    } catch (e: Exception) {
        val errorResponse = ServiceErrorResponse(
            error = "Error routing request to service $serviceName: ${e.message}",
            code = "SERVICE_ERROR",
            service = serviceName
        )
        call.respond(HttpStatusCode.InternalServerError, errorResponse)
    }
}

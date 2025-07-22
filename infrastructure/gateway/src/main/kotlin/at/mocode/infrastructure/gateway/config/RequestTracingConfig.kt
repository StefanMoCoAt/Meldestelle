package at.mocode.infrastructure.gateway.config

import at.mocode.core.utils.config.AppConfig
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.*
import java.util.UUID

/**
 * Configuration for request tracing and cross-service correlation.
 *
 * This configuration adds support for:
 * - Request ID generation and propagation
 * - Cross-service tracing
 * - Correlation ID extraction from incoming requests
 * - Correlation ID propagation to outgoing requests
 */

// Define attribute key for storing request ID in the ApplicationCall
val REQUEST_ID_KEY = AttributeKey<String>("RequestId")
val REQUEST_START_TIME_KEY = AttributeKey<Long>("RequestStartTime")

/**
 * Configures request tracing for the API Gateway.
 */
fun Application.configureRequestTracing() {
    val config = AppConfig.logging

    // Install a hook to intercept all incoming requests
    intercept(ApplicationCallPipeline.Monitoring) {
        // Store the start time for timing measurements
        val startTime = System.currentTimeMillis()
        call.attributes.put(REQUEST_START_TIME_KEY, startTime)

        // Try to extract request ID from incoming request headers
        val requestId = if (config.generateRequestIdIfMissing) {
            call.request.header(config.requestIdHeader) ?: generateRequestId()
        } else {
            call.request.header(config.requestIdHeader) ?: "no-request-id"
        }

        // Store the request ID in the call attributes for later use
        call.attributes.put(REQUEST_ID_KEY, requestId)

        // Add tracing headers to the response
        if (config.propagateRequestId) {
            // Add the primary request ID header
            call.response.header(config.requestIdHeader, requestId)

            // Add additional tracing headers for better cross-service correlation
            call.response.header("X-Correlation-ID", requestId)
            call.response.header("X-Request-Start-Time", startTime.toString())
            call.response.header("X-Service-Name", AppConfig.appInfo.name)
            call.response.header("X-Service-Version", AppConfig.appInfo.version)

            // Add trace parent header for W3C trace context compatibility
            // Format: 00-traceid-parentid-01 (version-traceid-parentid-flags)
            val traceId = requestId.replace("-", "").takeLast(32).padStart(32, '0')
            val parentId = requestId.hashCode().toString(16).takeLast(16).padStart(16, '0')
            call.response.header("traceparent", "00-$traceId-$parentId-01")
        }

        // Log the request with enhanced tracing information
        if (config.logRequests) {
            val clientIp = call.request.origin.remoteHost
            val userAgent = call.request.userAgent() ?: "unknown"
            val referer = call.request.header("Referer") ?: "-"
            val contentType = call.request.contentType().toString()
            val contentLength = call.request.header(HttpHeaders.ContentLength) ?: "0"
            val host = call.request.host()
            val scheme = call.request.local.scheme
            val port = call.request.port()
            val method = call.request.httpMethod.value
            val path = call.request.path()
            val queryString = call.request.queryString().let { if (it.isNotEmpty()) "?$it" else "" }

            // Extract trace context from incoming request if present
            val traceParent = call.request.header("traceparent") ?: "-"
            val traceState = call.request.header("tracestate") ?: "-"

            if (config.useStructuredLogging) {
                application.log.info(
                    "type=request " +
                    "requestId=$requestId " +
                    "method=$method " +
                    "path=$path " +
                    "query=$queryString " +
                    "scheme=$scheme " +
                    "host=$host " +
                    "port=$port " +
                    "client=$clientIp " +
                    "userAgent=\"$userAgent\" " +
                    "referer=\"$referer\" " +
                    "contentType=$contentType " +
                    "contentLength=$contentLength " +
                    "traceParent=$traceParent " +
                    "traceState=$traceState " +
                    "timestamp=${System.currentTimeMillis()}"
                )
            } else {
                application.log.info(
                    "Incoming request: $method $path$queryString - " +
                    "Host: $host:$port - " +
                    "Scheme: $scheme - " +
                    "Client: $clientIp - " +
                    "UserAgent: $userAgent - " +
                    "Referer: $referer - " +
                    "ContentType: $contentType - " +
                    "ContentLength: $contentLength - " +
                    "RequestID: $requestId - " +
                    "TraceParent: $traceParent"
                )
            }
        }
    }

    // Install a hook to intercept all outgoing responses
    intercept(ApplicationCallPipeline.Plugins) {
        // Get the request ID from the call attributes
        val requestId = call.attributes[REQUEST_ID_KEY]

        // Process the request
        proceed()

        // Calculate response time if configured
        if (config.logResponseTime) {
            val startTime = call.attributes[REQUEST_START_TIME_KEY]
            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime

            // Add timing information to response headers
            call.response.header("X-Response-Time", "$duration")

            // Log the response with enhanced tracing information
            if (config.logResponses) {
                val status = call.response.status() ?: HttpStatusCode.OK
                val path = call.request.path()
                val method = call.request.httpMethod.value
                val contentType = call.response.headers["Content-Type"] ?: "-"
                val contentLength = call.response.headers["Content-Length"] ?: "0"

                // Get memory usage for performance monitoring
                val memoryUsage = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()

                // Extract trace context from response
                val traceParent = call.response.headers["traceparent"] ?: "-"

                if (config.useStructuredLogging) {
                    application.log.info(
                        "type=response " +
                        "requestId=$requestId " +
                        "method=$method " +
                        "path=$path " +
                        "status=$status " +
                        "duration=${duration}ms " +
                        "contentType=$contentType " +
                        "contentLength=$contentLength " +
                        "traceParent=$traceParent " +
                        "memoryUsage=${memoryUsage}b " +
                        "timestamp=${System.currentTimeMillis()}"
                    )
                } else {
                    application.log.info(
                        "Response: $status - " +
                        "Method: $method - " +
                        "Path: $path - " +
                        "RequestID: $requestId - " +
                        "Duration: ${duration}ms - " +
                        "ContentType: $contentType - " +
                        "ContentLength: $contentLength - " +
                        "TraceParent: $traceParent - " +
                        "MemoryUsage: ${memoryUsage}b"
                    )
                }
            }
        } else if (config.logResponses) {
            // Log the response without timing information but with enhanced tracing data
            val status = call.response.status() ?: HttpStatusCode.OK
            val path = call.request.path()
            val method = call.request.httpMethod.value
            val contentType = call.response.headers["Content-Type"] ?: "-"
            val contentLength = call.response.headers["Content-Length"] ?: "0"

            // Get memory usage for performance monitoring
            val memoryUsage = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()

            // Extract trace context from response
            val traceParent = call.response.headers["traceparent"] ?: "-"

            if (config.useStructuredLogging) {
                application.log.info(
                    "type=response " +
                    "requestId=$requestId " +
                    "method=$method " +
                    "path=$path " +
                    "status=$status " +
                    "contentType=$contentType " +
                    "contentLength=$contentLength " +
                    "traceParent=$traceParent " +
                    "memoryUsage=${memoryUsage}b " +
                    "timestamp=${System.currentTimeMillis()}"
                )
            } else {
                application.log.info(
                    "Response: $status - " +
                    "Method: $method - " +
                    "Path: $path - " +
                    "RequestID: $requestId - " +
                    "ContentType: $contentType - " +
                    "ContentLength: $contentLength - " +
                    "TraceParent: $traceParent - " +
                    "MemoryUsage: ${memoryUsage}b"
                )
            }
        }
    }

    log.info("Request tracing configured with header: ${config.requestIdHeader}")
}

/**
 * Generates a new request ID with enhanced context information.
 *
 * Format: prefix-environment-service-timestamp-uuid
 * Example: req-prod-gateway-1627384950123-550e8400-e29b-41d4-a716-446655440000
 */
private fun generateRequestId(): String {
    val uuid = UUID.randomUUID().toString()
    val timestamp = System.currentTimeMillis()

    // Get environment prefix safely (first 4 chars or fewer)
    val environment = AppConfig.environment.toString().let { env ->
        if (env.length > 4) env.substring(0, 4) else env
    }.lowercase()

    // Get service name, replacing spaces with dashes
    val serviceName = AppConfig.appInfo.name.replace(" ", "-").lowercase()

    return "req-$environment-$serviceName-$timestamp-$uuid"
}

/**
 * Extension function to get the request ID from the call.
 */
fun ApplicationCall.requestId(): String = attributes[REQUEST_ID_KEY]

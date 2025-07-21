package at.mocode.gateway.config

import at.mocode.dto.base.ApiResponse
import at.mocode.shared.config.AppConfig
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.slf4j.event.Level
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.random.Random

/**
 * Monitoring and logging configuration for the API Gateway.
 *
 * Configures request logging, error handling, and status pages.
 * Works together with RequestTracingConfig for cross-service tracing.
 * Includes log sampling for high-traffic endpoints to reduce log volume.
 */

// Map to track request counts by path for log sampling
private val requestCountsByPath = ConcurrentHashMap<String, AtomicInteger>()

// Map to track high-traffic paths that are being sampled
private val sampledPaths = ConcurrentHashMap<String, Boolean>()

// Scheduler to reset request counts periodically
private val requestCountResetScheduler = Executors.newSingleThreadScheduledExecutor().apply {
    scheduleAtFixedRate({
        // Reset all counters every minute
        requestCountsByPath.clear()

        // Log which paths are being sampled
        if (sampledPaths.isNotEmpty()) {
            val sampledPathsList = sampledPaths.keys.joinToString(", ")
            println("[LogSampling] Currently sampling high-traffic paths: $sampledPathsList")
        }

        // Clear sampled paths to re-evaluate in the next period
        sampledPaths.clear()
    }, 1, 1, TimeUnit.MINUTES)
}

/**
 * Determines if a request should be logged based on sampling configuration.
 *
 * @param path The request path
 * @param statusCode The response status code
 * @param loggingConfig The logging configuration
 * @return True if the request should be logged, false otherwise
 */
private fun shouldLogRequest(path: String, statusCode: HttpStatusCode?, loggingConfig: at.mocode.shared.config.LoggingConfig): Boolean {
    // If sampling is disabled, always log
    if (!loggingConfig.enableLogSampling) {
        return true
    }

    // Always log errors if configured
    if (loggingConfig.alwaysLogErrors && statusCode != null && statusCode.value >= 400) {
        return true
    }

    // Always log specific paths if configured
    val normalizedPath = path.trimStart('/')
    if (loggingConfig.alwaysLogPaths.any { normalizedPath.startsWith(it.trimStart('/')) }) {
        return true
    }

    // Get or create counter for this path
    val basePath = extractBasePath(path)
    val counter = requestCountsByPath.computeIfAbsent(basePath) { AtomicInteger(0) }
    val count = counter.incrementAndGet()

    // Check if this is a high-traffic path
    if (count >= loggingConfig.highTrafficThreshold) {
        // Mark this path as being sampled
        sampledPaths[basePath] = true

        // Sample based on configured rate
        return Random.nextInt(100) < loggingConfig.samplingRate
    }

    // Not a high-traffic path, log normally
    return true
}

/**
 * Extracts the base path from a full path for grouping similar requests.
 * For example, "/api/v1/users/123" becomes "/api/v1/users"
 */
private fun extractBasePath(path: String): String {
    val parts = path.split("/").filter { it.isNotEmpty() }

    // Handle special cases
    if (parts.isEmpty()) return "/"

    // For API paths, include up to the resource name (typically 3 parts: api, version, resource)
    if (parts.size >= 1 && parts[0] == "api") {
        val depth = minOf(3, parts.size)
        return "/" + parts.take(depth).joinToString("/")
    }

    // For other paths, include up to 2 parts
    val depth = minOf(2, parts.size)
    return "/" + parts.take(depth).joinToString("/")
}
fun Application.configureMonitoring() {
    val loggingConfig = AppConfig.logging

    // Erweiterte Call-Logging-Konfiguration
    install(CallLogging) {
        level = when (loggingConfig.level.uppercase()) {
            "DEBUG" -> Level.DEBUG
            "TRACE" -> Level.TRACE
            "WARN" -> Level.WARN
            "ERROR" -> Level.ERROR
            else -> Level.INFO
        }

        // Filtere Pfade, die vom Logging ausgeschlossen werden sollen
        filter { call ->
            val path = call.request.path()
            !loggingConfig.excludePaths.any { path.startsWith(it) }
        }

        // Formatiere Log-Einträge mit erweitertem Format
        format { call ->
            val status = call.response.status()
            val httpMethod = call.request.httpMethod.value
            val path = call.request.path()
            val userAgent = call.request.headers["User-Agent"]
            val clientIp = call.request.local.remoteHost
            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

            // Get the request ID from the call attributes (set by RequestTracingConfig)
            val requestId = call.attributes.getOrNull(REQUEST_ID_KEY) ?: "no-request-id"

            if (loggingConfig.useStructuredLogging) {
                // Strukturiertes Logging-Format
                buildString {
                    append("timestamp=$timestamp ")
                    append("method=$httpMethod ")
                    append("path=$path ")
                    append("status=$status ")
                    append("client=$clientIp ")
                    append("requestId=$requestId ")

                    // Log Headers wenn konfiguriert
                    if (loggingConfig.logRequestHeaders) {
                        val authHeader = call.request.headers["Authorization"]
                        if (authHeader != null) {
                            append("auth=true ")
                        }

                        val contentType = call.request.headers["Content-Type"]
                        if (contentType != null) {
                            append("contentType=$contentType ")
                        }

                        // Log all headers if in debug mode, filtering sensitive data
                        if (loggingConfig.level.equals("DEBUG", ignoreCase = true)) {
                            append("headers={")
                            call.request.headers.entries().joinTo(this, ", ") {
                                if (isSensitiveHeader(it.key)) {
                                    "${it.key}=*****"
                                } else {
                                    "${it.key}=${it.value.joinToString(",")}"
                                }
                            }
                            append("} ")
                        }
                    }

                    // Log Query-Parameter wenn konfiguriert
                    if (loggingConfig.logRequestParameters && call.request.queryParameters.entries().isNotEmpty()) {
                        append("params={")
                        call.request.queryParameters.entries().joinTo(this, ", ") {
                            if (isSensitiveParameter(it.key)) {
                                "${it.key}=*****"
                            } else {
                                "${it.key}=${it.value.joinToString(",")}"
                            }
                        }
                        append("} ")
                    }

                    if (userAgent != null) {
                        append("userAgent=\"${userAgent.replace("\"", "\\\"")}\" ")
                    }

                    // Log response time if available from RequestTracingConfig
                    call.attributes.getOrNull(REQUEST_START_TIME_KEY)?.let { startTime ->
                        val duration = System.currentTimeMillis() - startTime
                        append("duration=${duration}ms ")
                    }

                    // Add performance metrics
                    val memoryUsage = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
                    append("memoryUsage=${memoryUsage}b ")

                    // Add additional performance metrics in debug mode
                    if (loggingConfig.level.equals("DEBUG", ignoreCase = true)) {
                        val availableProcessors = Runtime.getRuntime().availableProcessors()
                        val maxMemory = Runtime.getRuntime().maxMemory()
                        append("processors=$availableProcessors ")
                        append("maxMemory=${maxMemory}b ")
                    }
                }
            } else {
                // Einfaches Logging-Format
                val duration = call.attributes.getOrNull(REQUEST_START_TIME_KEY)?.let {
                    " - Duration: ${System.currentTimeMillis() - it}ms"
                } ?: ""

                "$timestamp - $status: $httpMethod $path - RequestID: $requestId - $clientIp - $userAgent$duration"
            }
        }
    }

    // Erweiterte Logging-Konfiguration für den API-Gateway
    log.info("API Gateway konfiguriert mit erweitertem Logging und Cross-Service Tracing")
    log.info("Logging-Konfiguration: level=${loggingConfig.level}, " +
            "logRequests=${loggingConfig.logRequests}, " +
            "logResponses=${loggingConfig.logResponses}, " +
            "logRequestHeaders=${loggingConfig.logRequestHeaders}, " +
            "logRequestParameters=${loggingConfig.logRequestParameters}, " +
            "requestIdHeader=${loggingConfig.requestIdHeader}, " +
            "propagateRequestId=${loggingConfig.propagateRequestId}")

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            // Get the request ID for error logging
            val requestId = call.attributes.getOrNull(REQUEST_ID_KEY) ?: "no-request-id"

            call.application.log.error("Unhandled exception - RequestID: $requestId", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiResponse.error<Any>("Internal server error: ${cause.message}")
            )
        }

        status(HttpStatusCode.NotFound) { call, status ->
            // Get the request ID for error logging
            val requestId = call.attributes.getOrNull(REQUEST_ID_KEY) ?: "no-request-id"

            call.application.log.warn("Not found - Path: ${call.request.path()} - RequestID: $requestId")
            call.respond(
                status,
                ApiResponse.error<Any>("Endpoint not found: ${call.request.path()}")
            )
        }

        status(HttpStatusCode.Unauthorized) { call, status ->
            // Get the request ID for error logging
            val requestId = call.attributes.getOrNull(REQUEST_ID_KEY) ?: "no-request-id"

            call.application.log.warn("Unauthorized access - Path: ${call.request.path()} - RequestID: $requestId")
            call.respond(
                status,
                ApiResponse.error<Any>("Authentication required")
            )
        }

        status(HttpStatusCode.Forbidden) { call, status ->
            // Get the request ID for error logging
            val requestId = call.attributes.getOrNull(REQUEST_ID_KEY) ?: "no-request-id"

            call.application.log.warn("Forbidden access - Path: ${call.request.path()} - RequestID: $requestId")
            call.respond(
                status,
                ApiResponse.error<Any>("Access forbidden")
            )
        }

        // Rate limit exceeded
        status(HttpStatusCode.TooManyRequests) { call, status ->
            // Get the request ID for error logging
            val requestId = call.attributes.getOrNull(REQUEST_ID_KEY) ?: "no-request-id"

            call.application.log.warn("Rate limit exceeded - Path: ${call.request.path()} - RequestID: $requestId")
            call.respond(
                status,
                ApiResponse.error<Any>("Rate limit exceeded. Please try again later.")
            )
        }
    }
}

/**
 * Determines if a header is sensitive and should be masked in logs.
 */
private fun isSensitiveHeader(headerName: String): Boolean {
    val sensitiveHeaders = listOf(
        "authorization", "cookie", "set-cookie", "x-api-key", "api-key",
        "password", "token", "secret", "credential", "apikey"
    )
    return sensitiveHeaders.any { headerName.lowercase().contains(it) }
}

/**
 * Determines if a parameter is sensitive and should be masked in logs.
 */
private fun isSensitiveParameter(paramName: String): Boolean {
    val sensitiveParams = listOf(
        "password", "token", "secret", "credential", "apikey", "key",
        "auth", "pin", "code", "otp", "cvv", "ssn", "credit"
    )
    return sensitiveParams.any { paramName.lowercase().contains(it) }
}

package at.mocode.gateway.config

import at.mocode.dto.base.ApiResponse
import at.mocode.shared.config.AppConfig
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.slf4j.event.Level
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

/**
 * Monitoring and logging configuration for the API Gateway.
 *
 * Configures request logging, error handling, and status pages.
 * Works together with RequestTracingConfig for cross-service tracing.
 * Includes log sampling for high-traffic endpoints to reduce log volume.
 */

// Map to track request counts by path for log sampling
// Using a more efficient ConcurrentHashMap with initial capacity and load factor
private val requestCountsByPath = ConcurrentHashMap<String, AtomicInteger>(32, 0.75f)

// Map to track high-traffic paths that are being sampled
private val sampledPaths = ConcurrentHashMap<String, Boolean>(16, 0.75f)

// Scheduler to reset request counts periodically
private val requestCountResetScheduler = Executors.newSingleThreadScheduledExecutor { r ->
    val thread = Thread(r, "log-sampling-reset-thread")
    thread.isDaemon = true // Make it a daemon thread so it doesn't prevent JVM shutdown
    thread
}

// Schedule the task with proper lifecycle management
private fun scheduleRequestCountReset() {
    // Reset counters every 5 minutes instead of every minute to reduce overhead
    requestCountResetScheduler.scheduleAtFixedRate({
        try {
            // Reset all counters
            requestCountsByPath.clear()

            // Log which paths are being sampled (only if there are any)
            if (sampledPaths.isNotEmpty()) {
                // More efficient string building for logging
                val sampledPathsCount = sampledPaths.size
                if (sampledPathsCount <= 5) {
                    // For a small number of paths, log them all
                    val sampledPathsList = sampledPaths.keys.joinToString(", ")
                    println("[LogSampling] Currently sampling $sampledPathsCount high-traffic paths: $sampledPathsList")
                } else {
                    // For many paths, just log the count to avoid excessive logging
                    println("[LogSampling] Currently sampling $sampledPathsCount high-traffic paths")
                }
            }

            // Clear sampled paths to re-evaluate in the next period
            sampledPaths.clear()
        } catch (e: Exception) {
            // Catch any exceptions to prevent the scheduler from stopping
            println("[LogSampling] Error in reset task: ${e.message}")
        }
    }, 5, 5, TimeUnit.MINUTES)
}

// Shutdown hook to clean up resources
private fun shutdownRequestCountResetScheduler() {
    requestCountResetScheduler.shutdown()
    try {
        if (!requestCountResetScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
            requestCountResetScheduler.shutdownNow()
        }
    } catch (e: InterruptedException) {
        requestCountResetScheduler.shutdownNow()
        Thread.currentThread().interrupt()
    }
}

/**
 * Determines if a request should be logged based on sampling configuration.
 * Optimized for performance with early returns and cached path normalization.
 *
 * @param path The request path
 * @param statusCode The response status code
 * @param loggingConfig The logging configuration
 * @return True if the request should be logged, false otherwise
 */
private fun shouldLogRequest(path: String, statusCode: HttpStatusCode?, loggingConfig: at.mocode.shared.config.LoggingConfig): Boolean {
    // Fast path: If sampling is disabled, always log
    if (!loggingConfig.enableLogSampling) {
        return true
    }

    // Fast path: Always log errors if configured
    if (statusCode != null && statusCode.value >= 400 && loggingConfig.alwaysLogErrors) {
        return true
    }

    // Check if this is a path that should always be logged
    // Only normalize the path if we have paths to check against
    if (loggingConfig.alwaysLogPaths.isNotEmpty()) {
        val normalizedPath = path.trimStart('/')
        // Use any with early return for better performance
        for (alwaysLogPath in loggingConfig.alwaysLogPaths) {
            if (normalizedPath.startsWith(alwaysLogPath.trimStart('/'))) {
                return true
            }
        }
    }

    // Get the base path for traffic counting
    val basePath = extractBasePath(path)

    // Check if this path is already known to be high-traffic
    if (sampledPaths.containsKey(basePath)) {
        // Already identified as high-traffic, apply sampling
        return Random.nextInt(100) < loggingConfig.samplingRate
    }

    // Get or create counter for this path
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
    if (parts[0] == "api") {
        val depth = minOf(3, parts.size)
        return "/" + parts.take(depth).joinToString("/")
    }

    // For other paths, include up to 2 parts
    val depth = minOf(2, parts.size)
    return "/" + parts.take(depth).joinToString("/")
}
fun Application.configureMonitoring() {
    val loggingConfig = AppConfig.logging

    // Note: Prometheus metrics configuration has been moved to PrometheusConfig.kt

    // Start the request count reset scheduler
    scheduleRequestCountReset()

    // Register shutdown hook for application lifecycle
    this.monitor.subscribe(ApplicationStopPreparing) {
        log.info("Application stopping, shutting down schedulers...")
        shutdownRequestCountResetScheduler()
    }

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
        filter { call: ApplicationCall ->
            val path = call.request.path()
            !loggingConfig.excludePaths.any { path.startsWith(it) }
        }

        // Formatiere Log-Einträge mit erweitertem Format
        format { call: ApplicationCall ->
            val status = call.response.status()
            val httpMethod = call.request.httpMethod.value
            val path = call.request.path()
            val userAgent = call.request.headers["User-Agent"]
            val clientIp = call.request.local.remoteHost
            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

            // Get the request ID from the call attributes (set by RequestTracingConfig)
            val requestId: String = call.attributes.getOrNull(REQUEST_ID_KEY) ?: "no-request-id"

            if (loggingConfig.useStructuredLogging) {
                // Optimized structured logging format using StringBuilder with initial capacity
                // Estimate the initial capacity based on typical log entry size
                val initialCapacity = 256 +
                    (if (loggingConfig.logRequestHeaders) 128 else 0) +
                    (if (loggingConfig.logRequestParameters) 128 else 0)

                val sb = StringBuilder(initialCapacity)

                // Basic request information - always included
                sb.append("timestamp=").append(timestamp).append(' ')
                  .append("method=").append(httpMethod).append(' ')
                  .append("path=").append(path).append(' ')
                  .append("status=").append(status).append(' ')
                  .append("client=").append(clientIp).append(' ')
                  .append("requestId=").append(requestId).append(' ')

                // Log Headers wenn konfiguriert
                if (loggingConfig.logRequestHeaders) {
                    val authHeader = call.request.headers["Authorization"]
                    if (authHeader != null) {
                        sb.append("auth=true ")
                    }

                    val contentType = call.request.headers["Content-Type"]
                    if (contentType != null) {
                        sb.append("contentType=").append(contentType).append(' ')
                    }

                    // Log all headers if in debug mode, filtering sensitive data
                    if (loggingConfig.level.equals("DEBUG", ignoreCase = true)) {
                        sb.append("headers={")
                        var first = true
                        for (entry in call.request.headers.entries()) {
                            if (!first) sb.append(", ")
                            first = false

                            if (isSensitiveHeader(entry.key)) {
                                sb.append(entry.key).append("=*****")
                            } else {
                                sb.append(entry.key).append('=').append(entry.value.joinToString(","))
                            }
                        }
                        sb.append("} ")
                    }
                }

                // Log Query-Parameter wenn konfiguriert
                if (loggingConfig.logRequestParameters && call.request.queryParameters.entries().isNotEmpty()) {
                    sb.append("params={")
                    var first = true
                    for (entry in call.request.queryParameters.entries()) {
                        if (!first) sb.append(", ")
                        first = false

                        if (isSensitiveParameter(entry.key)) {
                            sb.append(entry.key).append("=*****")
                        } else {
                            sb.append(entry.key).append('=').append(entry.value.joinToString(","))
                        }
                    }
                    sb.append("} ")
                }

                if (userAgent != null) {
                    // Use a simpler approach to avoid escape sequence issues
                    val escapedUserAgent = userAgent.replace("\"", "\\\"")
                    sb.append("userAgent=\"").append(escapedUserAgent).append("\" ")
                }

                // Log response time if available from RequestTracingConfig
                call.attributes.getOrNull(REQUEST_START_TIME_KEY)?.let { startTime: Long ->
                    val duration = System.currentTimeMillis() - startTime
                    sb.append("duration=").append(duration).append("ms ")
                }

                // Add performance metrics - only calculate memory usage if needed
                // Only include memory metrics in every 10th log entry to reduce overhead
                if (Random.nextInt(10) == 0) {
                    val memoryUsage = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
                    sb.append("memoryUsage=").append(memoryUsage).append("b ")

                    // Add additional performance metrics in debug mode
                    if (loggingConfig.level.equals("DEBUG", ignoreCase = true)) {
                        val availableProcessors = Runtime.getRuntime().availableProcessors()
                        val maxMemory = Runtime.getRuntime().maxMemory()
                        sb.append("processors=").append(availableProcessors).append(' ')
                          .append("maxMemory=").append(maxMemory).append("b ")
                    }
                }

                sb.toString()
            } else {
                // Einfaches Logging-Format
                val duration = call.attributes.getOrNull(REQUEST_START_TIME_KEY)?.let { startTime: Long ->
                    " - Duration: ${System.currentTimeMillis() - startTime}ms"
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
        exception<Throwable> { call: ApplicationCall, cause: Throwable ->
            // Get the request ID for error logging
            val requestId: String = call.attributes.getOrNull(REQUEST_ID_KEY) ?: "no-request-id"

            call.application.log.error("Unhandled exception - RequestID: $requestId", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiResponse.error<Any>("Internal server error: ${cause.message}")
            )
        }

        status(HttpStatusCode.NotFound) { call: ApplicationCall, status: HttpStatusCode ->
            // Get the request ID for error logging
            val requestId: String = call.attributes.getOrNull(REQUEST_ID_KEY) ?: "no-request-id"

            call.application.log.warn("Not found - Path: ${call.request.path()} - RequestID: $requestId")
            call.respond(
                status,
                ApiResponse.error<Any>("Endpoint not found: ${call.request.path()}")
            )
        }

        status(HttpStatusCode.Unauthorized) { call: ApplicationCall, status: HttpStatusCode ->
            // Get the request ID for error logging
            val requestId: String = call.attributes.getOrNull(REQUEST_ID_KEY) ?: "no-request-id"

            call.application.log.warn("Unauthorized access - Path: ${call.request.path()} - RequestID: $requestId")
            call.respond(
                status,
                ApiResponse.error<Any>("Authentication required")
            )
        }

        status(HttpStatusCode.Forbidden) { call: ApplicationCall, status: HttpStatusCode ->
            // Get the request ID for error logging
            val requestId: String = call.attributes.getOrNull(REQUEST_ID_KEY) ?: "no-request-id"

            call.application.log.warn("Forbidden access - Path: ${call.request.path()} - RequestID: $requestId")
            call.respond(
                status,
                ApiResponse.error<Any>("Access forbidden")
            )
        }

        // Rate limit exceeded
        status(HttpStatusCode.TooManyRequests) { call: ApplicationCall, status: HttpStatusCode ->
            // Get the request ID for error logging
            val requestId: String = call.attributes.getOrNull(REQUEST_ID_KEY) ?: "no-request-id"

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

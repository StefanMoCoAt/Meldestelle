package at.mocode.gateway.config

import at.mocode.shared.config.AppConfig
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.util.*
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

/**
 * Configuration for log sampling in the API Gateway.
 *
 * This configuration adds support for:
 * - Sampling logs for high-traffic endpoints to reduce log volume
 * - Configurable sampling rate and thresholds
 * - Always logging errors and specific paths regardless of sampling
 * - Periodic reset of request counters
 */

// Logger for log sampling
private val logger = LoggerFactory.getLogger("LogSampling")

// Map to track request counts by path for log sampling
private val requestCountsByPath = ConcurrentHashMap<String, AtomicInteger>()

// Map to track high-traffic paths that are being sampled
private val sampledPaths = ConcurrentHashMap<String, Boolean>()

// Attribute key for storing whether a request should be logged
val SHOULD_LOG_REQUEST_KEY = AttributeKey<Boolean>("ShouldLogRequest")

// Scheduler to reset request counts periodically
private val requestCountResetScheduler = Executors.newSingleThreadScheduledExecutor().apply {
    scheduleAtFixedRate({
        try {
            // Reset all counters every minute
            requestCountsByPath.clear()

            // Log which paths are being sampled
            if (sampledPaths.isNotEmpty()) {
                val sampledPathsList = sampledPaths.keys.joinToString(", ")
                logger.info("Currently sampling high-traffic paths: $sampledPathsList")
            }

            // Clear sampled paths to re-evaluate in the next period
            sampledPaths.clear()
        } catch (e: Exception) {
            logger.error("Error in request count reset scheduler", e)
        }
    }, 1, 1, TimeUnit.MINUTES)
}

/**
 * Configures log sampling for the API Gateway.
 */
fun Application.configureLogSampling() {
    val loggingConfig = AppConfig.logging

    // Log configuration information
    if (loggingConfig.enableLogSampling) {
        log.info("Log sampling ENABLED with rate: ${loggingConfig.samplingRate}%")
        log.info("High traffic threshold: ${loggingConfig.highTrafficThreshold} requests per minute")
        log.info("Always log paths: ${loggingConfig.alwaysLogPaths.joinToString(", ")}")
        log.info("Always log errors: ${loggingConfig.alwaysLogErrors}")
    } else {
        log.info("Log sampling DISABLED")
        return
    }

    // Install interceptor to apply log sampling logic
    intercept(ApplicationCallPipeline.Monitoring) {
        val path = call.request.path()

        // Determine if this request should be logged
        val shouldLog = shouldLogRequest(path, null, loggingConfig)

        // Store the decision in call attributes for later use
        call.attributes.put(SHOULD_LOG_REQUEST_KEY, shouldLog)

        // Continue processing the request
        proceed()

        // Update the decision based on the response status (for error logging)
        if (!shouldLog && loggingConfig.alwaysLogErrors) {
            val status = call.response.status()
            if (status != null && status.value >= 400) {
                call.attributes.put(SHOULD_LOG_REQUEST_KEY, true)
            }
        }
    }

    // Instead of trying to modify CallLogging after installation,
    // we'll use the interceptor to decide if logging should happen
    // The CallLogging plugin will be configured in MonitoringConfig.kt
}

/**
 * Determines if a request should be logged based on sampling configuration.
 *
 * @param path The request path
 * @param statusCode The response status code (null for request phase)
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
    if (parts[0] == "api") {
        val depth = minOf(3, parts.size)
        return "/" + parts.take(depth).joinToString("/")
    }

    // For other paths, include up to 2 parts
    val depth = minOf(2, parts.size)
    return "/" + parts.take(depth).joinToString("/")
}

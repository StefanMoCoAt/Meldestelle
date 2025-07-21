package at.mocode.gateway.config

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.util.*
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Timer
import io.micrometer.prometheus.PrometheusMeterRegistry
import java.util.concurrent.ConcurrentHashMap

/**
 * Custom application metrics configuration.
 *
 * Adds application-specific metrics for better monitoring:
 * - API endpoint response times
 * - Request counts by endpoint and status code
 * - Error rates
 * - Database query metrics
 */

// Reference to the Prometheus registry from PrometheusConfig
private val appRegistry: PrometheusMeterRegistry
    get() = at.mocode.gateway.config.appMicrometerRegistry

// Attribute key for request start time
private val REQUEST_TIMER_ATTRIBUTE = AttributeKey<Timer.Sample>("RequestTimerSample")

// Cache for endpoint timers to avoid creating new ones for each request
private val endpointTimers = ConcurrentHashMap<String, Timer>()

// Cache for endpoint counters
private val endpointCounters = ConcurrentHashMap<Pair<String, Int>, Counter>()

// Cache for error counters
private val errorCounters = ConcurrentHashMap<String, Counter>()

/**
 * Configures custom application metrics.
 */
fun Application.configureCustomMetrics() {
    // Install a hook to intercept all requests for timing
    intercept(ApplicationCallPipeline.Monitoring) {
        // Start timing the request
        val timerSample = Timer.start(appRegistry)
        call.attributes.put(REQUEST_TIMER_ATTRIBUTE, timerSample)
    }

    // Install a hook to record metrics after the request is processed
    intercept(ApplicationCallPipeline.Fallback) {
        val status = call.response.status()?.value ?: 0
        val method = call.request.httpMethod.value
        val route = extractRoutePattern(call)

        // Record request count
        getOrCreateRequestCounter(method, route, status).increment()

        // Record timing
        call.attributes.getOrNull(REQUEST_TIMER_ATTRIBUTE)?.let { timerSample ->
            val timer = getOrCreateEndpointTimer(method, route)
            timerSample.stop(timer)
        }

        // Record errors
        if (status >= 400) {
            getOrCreateErrorCounter(method, route, status).increment()
        }
    }

    // Register database metrics
    registerDatabaseMetrics()

    log.info("Custom application metrics configured")
}

/**
 * Extracts a normalized route pattern from the call.
 * Converts dynamic path segments to a generic pattern.
 * For example: /api/users/123 -> /api/users/{id}
 */
private fun extractRoutePattern(call: ApplicationCall): String {
    val path = call.request.path()

    // Try to get the route from the call attributes if available
    call.attributes.getOrNull(AttributeKey<Route>("ktor.request.route"))?.let { route ->
        return route.toString()
    }

    // Otherwise, normalize the path by replacing likely IDs with {id}
    val segments = path.split("/")
    val normalizedSegments = segments.map { segment ->
        // If segment looks like an ID (UUID, number), replace with {id}
        if (segment.matches(Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")) ||
            segment.matches(Regex("\\d+"))
        ) {
            "{id}"
        } else {
            segment
        }
    }

    return normalizedSegments.joinToString("/")
}

/**
 * Gets or creates a timer for the specified endpoint.
 */
private fun getOrCreateEndpointTimer(method: String, route: String): Timer {
    val key = "$method $route"
    return endpointTimers.computeIfAbsent(key) {
        Timer.builder("http.server.requests")
            .tag("method", method)
            .tag("route", route)
            .publishPercentileHistogram()
            .register(appRegistry)
    }
}

/**
 * Gets or creates a counter for the specified endpoint and status.
 */
private fun getOrCreateRequestCounter(method: String, route: String, status: Int): Counter {
    val key = Pair("$method $route", status)
    return endpointCounters.computeIfAbsent(key) {
        Counter.builder("http.server.requests.count")
            .tag("method", method)
            .tag("route", route)
            .tag("status", status.toString())
            .register(appRegistry)
    }
}

/**
 * Gets or creates an error counter for the specified endpoint and status.
 */
private fun getOrCreateErrorCounter(method: String, route: String, status: Int): Counter {
    val key = "$method $route $status"
    return errorCounters.computeIfAbsent(key) {
        Counter.builder("http.server.errors")
            .tag("method", method)
            .tag("route", route)
            .tag("status", status.toString())
            .register(appRegistry)
    }
}

/**
 * Registers database metrics.
 */
private fun registerDatabaseMetrics() {
    // Create a gauge for active connections
    appRegistry.gauge("db.connections.active",
        at.mocode.shared.database.DatabaseFactory,
        { it.getActiveConnections().toDouble() })

    // Create a gauge for idle connections
    appRegistry.gauge("db.connections.idle",
        at.mocode.shared.database.DatabaseFactory,
        { it.getIdleConnections().toDouble() })

    // Create a gauge for total connections
    appRegistry.gauge("db.connections.total",
        at.mocode.shared.database.DatabaseFactory,
        { it.getTotalConnections().toDouble() })
}

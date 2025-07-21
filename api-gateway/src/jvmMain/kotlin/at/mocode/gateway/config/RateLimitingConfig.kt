package at.mocode.gateway.config

import at.mocode.shared.config.AppConfig
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlin.time.Duration.Companion.minutes
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.nio.charset.StandardCharsets
import java.lang.management.ManagementFactory
import java.util.concurrent.atomic.AtomicInteger

/**
 * Configuration for advanced rate limiting in the API Gateway.
 *
 * This configuration adds support for:
 * - Global rate limiting
 * - Endpoint-specific rate limiting
 * - User-type-specific rate limiting
 * - Rate limit headers in responses
 * - Token parsing caching for improved performance
 * - Adaptive rate limiting based on server load
 */

// Cache for parsed JWT tokens to avoid repeated decoding
// Key: Token hash, Value: Parsed token data (userId to userType mapping)
private val tokenCache = ConcurrentHashMap<Int, Pair<String, String>>()

// Cache expiration settings
private const val TOKEN_CACHE_MAX_SIZE = 10000 // Maximum number of tokens to cache
private const val TOKEN_CACHE_EXPIRATION_MINUTES = 60L // Cache expiration time in minutes

// Schedule cache cleanup to prevent memory leaks
private val cacheCleanupScheduler = java.util.Timer("token-cache-cleanup").apply {
    schedule(object : java.util.TimerTask() {
        override fun run() {
            if (tokenCache.size > TOKEN_CACHE_MAX_SIZE) {
                // If the cache exceeds max size, remove the oldest entries (simple approach)
                val keysToRemove = tokenCache.keys.take(tokenCache.size - TOKEN_CACHE_MAX_SIZE / 2)
                keysToRemove.forEach { tokenCache.remove(it) }
            }
        }
    }, TimeUnit.MINUTES.toMillis(10), TimeUnit.MINUTES.toMillis(10))
}

/**
 * Adaptive rate limiting configuration.
 * These settings control how rate limits are adjusted based on server load.
 */
private object AdaptiveRateLimiting {
    // Enable/disable adaptive rate limiting
    const val ENABLED = true

    // Thresholds for CPU usage (percentage)
    const val CPU_MEDIUM_LOAD_THRESHOLD = 60.0 // Medium load threshold (60%)
    const val CPU_HIGH_LOAD_THRESHOLD = 80.0   // High-load threshold (80%)

    // Thresholds for memory usage (percentage)
    const val MEMORY_MEDIUM_LOAD_THRESHOLD = 70.0 // Medium load threshold (70%)
    const val MEMORY_HIGH_LOAD_THRESHOLD = 85.0   // High-load threshold (85%)

    // Rate limit adjustment factors
    const val MEDIUM_LOAD_FACTOR = 0.7 // Reduce limits to 70% under a medium load
    const val HIGH_LOAD_FACTOR = 0.4   // Reduce limits to 40% under a high load

    // Monitoring interval in milliseconds
    const val MONITORING_INTERVAL_MS = 5000L // Check every 5 seconds

    // Current load factor (starts at 1.0 = 100%)
    val currentLoadFactor = AtomicInteger(100)

    // Get the current load factor as a double (0.0-1.0)
    fun getCurrentLoadFactor(): Double = currentLoadFactor.get() / 100.0

    // Initialize the load monitoring
    init {
        if (ENABLED) {
            startLoadMonitoring()
        }
    }

    /**
     * Start monitoring server load and adjusting the rate limit factor.
     */
    private fun startLoadMonitoring() {
        val timer = java.util.Timer("adaptive-rate-limit-monitor", true)
        val operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean()
        val runtime = Runtime.getRuntime()

        timer.schedule(object : java.util.TimerTask() {
            override fun run() {
                try {
                    // Get CPU load (if available)
                    val cpuLoad = if (operatingSystemMXBean is com.sun.management.OperatingSystemMXBean) {
                        operatingSystemMXBean.processCpuLoad * 100
                    } else {
                        // Fallback if the specific implementation is not available
                        operatingSystemMXBean.systemLoadAverage.takeIf { it >= 0 }?.let {
                            it * 100 / runtime.availableProcessors()
                        } ?: 50.0 // Default to 50% if not available
                    }

                    // Get memory usage
                    val maxMemory = runtime.maxMemory().toDouble()
                    val usedMemory = (runtime.totalMemory() - runtime.freeMemory()).toDouble()
                    val memoryUsage = (usedMemory / maxMemory) * 100

                    // Determine load factor based on CPU and memory usage
                    val newLoadFactor = when {
                        cpuLoad > CPU_HIGH_LOAD_THRESHOLD || memoryUsage > MEMORY_HIGH_LOAD_THRESHOLD ->
                            (HIGH_LOAD_FACTOR * 100).toInt()
                        cpuLoad > CPU_MEDIUM_LOAD_THRESHOLD || memoryUsage > MEMORY_MEDIUM_LOAD_THRESHOLD ->
                            (MEDIUM_LOAD_FACTOR * 100).toInt()
                        else -> 100 // Normal load = 100%
                    }

                    // Update the load factor if it changed
                    val oldLoadFactor = currentLoadFactor.getAndSet(newLoadFactor)
                    if (oldLoadFactor != newLoadFactor) {
                        println("[AdaptiveRateLimiting] Load factor changed: ${oldLoadFactor/100.0} -> ${newLoadFactor/100.0} " +
                                "(CPU: ${String.format("%.1f", cpuLoad)}%, Memory: ${String.format("%.1f", memoryUsage)}%)")
                    }
                } catch (e: Exception) {
                    // If any error occurs, reset to normal load
                    currentLoadFactor.set(100)
                    println("[AdaptiveRateLimiting] Error monitoring system load: ${e.message}")
                }
            }
        }, 0, MONITORING_INTERVAL_MS)
    }

    /**
     * Adjust a rate limit based on the current server load.
     */
    fun adjustRateLimit(baseLimit: Int): Int {
        if (!ENABLED) return baseLimit

        val factor = getCurrentLoadFactor()
        return (baseLimit * factor).toInt().coerceAtLeast(1) // Ensure at least 1 request is allowed
    }
}

/**
 * Efficient hashing function for request keys.
 * Uses FNV-1a hash algorithm which is fast and has good distribution.
 */
private fun efficientHash(input: String): Int {
    val bytes = input.toByteArray(StandardCharsets.UTF_8)
    var hash = 0x811c9dc5.toInt() // FNV-1a prime

    for (byte in bytes) {
        hash = hash xor byte.toInt()
        hash = hash * 0x01000193 // FNV-1a prime multiplier
    }

    return hash
}

/**
 * Generates an efficient request key from multiple inputs.
 * Avoids string concatenation by hashing each input separately and combining the hashes.
 */
private fun generateRequestKey(vararg inputs: String?): String {
    var combinedHash = 0

    for (input in inputs) {
        if (input != null && input.isNotEmpty()) {
            // Combine hashes using XOR and bit rotation for better distribution
            val inputHash = efficientHash(input)
            combinedHash = (combinedHash xor inputHash) + ((combinedHash shl 5) + (combinedHash shr 2))
        }
    }

    return combinedHash.toString()
}
fun Application.configureRateLimiting() {
    val config = AppConfig.rateLimit

    if (!config.enabled) {
        log.info("Rate limiting is disabled")
        return
    }

    install(RateLimit) {
        // Global rate limiting configuration
        global {
            // Limit based on configuration, adjusted for server load
            rateLimiter(
                limit = AdaptiveRateLimiting.adjustRateLimit(config.globalLimit),
                refillPeriod = config.globalPeriodMinutes.minutes
            )
            // Enhanced request-key based on IP address and optional User-Agent
            // Using efficient hashing for better performance
            requestKey { call ->
                val ip = call.request.local.remoteHost
                val userAgent = call.request.userAgent() ?: ""
                // Use efficient hashing to generate request key
                generateRequestKey(ip, userAgent)
            }
        }

        // Endpoint-specific rate limiting
        for ((endpoint, limitConfig) in config.endpointLimits) {
            register(RateLimitName(endpoint)) {
                // Limit based on configuration, adjusted for server load
                rateLimiter(
                    limit = AdaptiveRateLimiting.adjustRateLimit(limitConfig.limit),
                    refillPeriod = limitConfig.periodMinutes.minutes
                )
                // Enhanced request-key with IP and optional request ID
                // Using efficient hashing for better performance
                requestKey { call ->
                    val ip = call.request.local.remoteHost
                    val requestId = call.attributes.getOrNull(REQUEST_ID_KEY)?.toString() ?: ""
                    val endpoint = endpoint // Include endpoint in the key for better separation
                    // Use efficient hashing to generate request key
                    generateRequestKey(ip, requestId, endpoint)
                }
            }
        }

        // User-type-specific rate limiting
        register(RateLimitName("anonymous")) {
            // Limit based on configuration, adjusted for server load
            rateLimiter(
                limit = AdaptiveRateLimiting.adjustRateLimit(config.userTypeLimits["anonymous"]?.limit ?: config.globalLimit),
                refillPeriod = (config.userTypeLimits["anonymous"]?.periodMinutes ?: config.globalPeriodMinutes).minutes
            )
            // Enhanced request-key with IP and user agent for anonymous users
            // Using efficient hashing for better performance
            requestKey { call ->
                val ip = call.request.local.remoteHost
                val userAgent = call.request.userAgent() ?: ""
                // Use efficient hashing to generate request key with "anon" prefix for type separation
                generateRequestKey("anon", ip, userAgent)
            }
        }

        register(RateLimitName("authenticated")) {
            // Limit based on configuration, adjusted for server load
            rateLimiter(
                limit = AdaptiveRateLimiting.adjustRateLimit(config.userTypeLimits["authenticated"]?.limit ?: config.globalLimit),
                refillPeriod = (config.userTypeLimits["authenticated"]?.periodMinutes ?: config.globalPeriodMinutes).minutes
            )
            // Using efficient hashing for better performance
            requestKey { call ->
                // Use user ID from JWT token if available, otherwise use IP
                val userId = call.request.header("Authorization")?.let { extractUserIdFromToken(it) }
                val ip = call.request.local.remoteHost
                // Use efficient hashing to generate request key with "auth" prefix for type separation
                generateRequestKey("auth", userId ?: "", ip)
            }
        }

        register(RateLimitName("admin")) {
            // Limit based on configuration, adjusted for server load
            rateLimiter(
                limit = AdaptiveRateLimiting.adjustRateLimit(config.userTypeLimits["admin"]?.limit ?: config.globalLimit),
                refillPeriod = (config.userTypeLimits["admin"]?.periodMinutes ?: config.globalPeriodMinutes).minutes
            )
            // Using efficient hashing for better performance
            requestKey { call ->
                // Use user ID from JWT token if available, otherwise use IP
                val userId = call.request.header("Authorization")?.let { extractUserIdFromToken(it) }
                val ip = call.request.local.remoteHost
                // Use efficient hashing to generate request key with "admin" prefix for type separation
                generateRequestKey("admin", userId ?: "", ip)
            }
        }
    }

    // Add rate limit headers to all responses
    if (config.includeHeaders) {
        intercept(ApplicationCallPipeline.Plugins) {
            // Get current load factor for adaptive rate limiting
            val loadFactor = AdaptiveRateLimiting.getCurrentLoadFactor()
            val adjustedGlobalLimit = AdaptiveRateLimiting.adjustRateLimit(config.globalLimit)

            // Add basic rate limit headers
            call.response.header("X-RateLimit-Enabled", "true")
            call.response.header("X-RateLimit-Limit", config.globalLimit.toString())
            call.response.header("X-RateLimit-Adjusted-Limit", adjustedGlobalLimit.toString())

            // Add adaptive rate limiting information
            call.response.header("X-RateLimit-Load-Factor", String.format("%.2f", loadFactor))
            call.response.header("X-RateLimit-Adaptive", AdaptiveRateLimiting.ENABLED.toString())

            // Add standard rate limit headers
            call.response.header("X-RateLimit-Policy", "${config.globalLimit} requests per ${config.globalPeriodMinutes} minutes")
            call.response.header("X-RateLimit-Adjusted-Policy", "${adjustedGlobalLimit} requests per ${config.globalPeriodMinutes} minutes")

            // Add estimated reset time (simplified version)
            val resetTimeSeconds = config.globalPeriodMinutes * 60
            call.response.header("X-RateLimit-Reset", resetTimeSeconds.toString())

            // Add retry-after header if rate limited (status code 429)
            if (call.response.status() == HttpStatusCode.TooManyRequests) {
                // Calculate retry-after value based on rate limit configuration
                val retryAfter = (config.globalPeriodMinutes * 60 / config.globalLimit).coerceAtLeast(1)
                call.response.header(HttpHeaders.RetryAfter, retryAfter.toString())
            }

            // Add more detailed headers based on the request path
            val path = call.request.path()
            config.endpointLimits.entries.find { path.startsWith("/${it.key}") }?.let { (endpoint, limitConfig) ->
                // Calculate adjusted limit for this endpoint
                val adjustedEndpointLimit = AdaptiveRateLimiting.adjustRateLimit(limitConfig.limit)

                call.response.header("X-RateLimit-Endpoint", endpoint)
                call.response.header("X-RateLimit-Endpoint-Limit", limitConfig.limit.toString())
                call.response.header("X-RateLimit-Endpoint-Adjusted-Limit", adjustedEndpointLimit.toString())
                call.response.header("X-RateLimit-Endpoint-Period", "${limitConfig.periodMinutes}m")
                call.response.header("X-RateLimit-Endpoint-Reset", (limitConfig.periodMinutes * 60).toString())
            }

            // Add user type rate limit headers if authenticated
            val authHeader = call.request.header("Authorization")
            if (authHeader != null) {
                val userType = determineUserType(authHeader)
                config.userTypeLimits[userType]?.let { limitConfig ->
                    // Calculate adjusted limit for this user type
                    val adjustedUserTypeLimit = AdaptiveRateLimiting.adjustRateLimit(limitConfig.limit)

                    call.response.header("X-RateLimit-UserType", userType)
                    call.response.header("X-RateLimit-UserType-Limit", limitConfig.limit.toString())
                    call.response.header("X-RateLimit-UserType-Adjusted-Limit", adjustedUserTypeLimit.toString())
                    call.response.header("X-RateLimit-UserType-Period", "${limitConfig.periodMinutes}m")
                    call.response.header("X-RateLimit-UserType-Reset", (limitConfig.periodMinutes * 60).toString())
                }
            }

            // Log rate limiting information if rate limited
            if (call.response.status() == HttpStatusCode.TooManyRequests) {
                val requestId = call.attributes.getOrNull(REQUEST_ID_KEY) ?: "no-request-id"
                val retryAfter = (config.globalPeriodMinutes * 60 / config.globalLimit).coerceAtLeast(1)
                val loadFactor = AdaptiveRateLimiting.getCurrentLoadFactor()
                val originalLimit = config.globalLimit
                val adjustedLimit = AdaptiveRateLimiting.adjustRateLimit(originalLimit)

                application.log.warn("Rate limit exceeded - Path: ${call.request.path()} - " +
                    "RequestID: $requestId - Client: ${call.request.local.remoteHost} - " +
                    "RetryAfter: ${retryAfter}s - " +
                    "LoadFactor: ${String.format("%.2f", loadFactor)} - " +
                    "OriginalLimit: $originalLimit - AdjustedLimit: $adjustedLimit")
            }
        }
    }

    // Log basic rate limiting configuration
    log.info("Rate limiting configured with global limit: ${config.globalLimit}/${config.globalPeriodMinutes}m")
    log.info("Endpoint-specific limits: ${config.endpointLimits.size} configured")
    log.info("User-type-specific limits: ${config.userTypeLimits.size} configured")

    // Log adaptive rate limiting configuration
    if (AdaptiveRateLimiting.ENABLED) {
        log.info("Adaptive rate limiting ENABLED with current load factor: ${String.format("%.2f", AdaptiveRateLimiting.getCurrentLoadFactor())}")
        log.info("Adaptive thresholds - CPU: Medium=${AdaptiveRateLimiting.CPU_MEDIUM_LOAD_THRESHOLD}%, High=${AdaptiveRateLimiting.CPU_HIGH_LOAD_THRESHOLD}%")
        log.info("Adaptive thresholds - Memory: Medium=${AdaptiveRateLimiting.MEMORY_MEDIUM_LOAD_THRESHOLD}%, High=${AdaptiveRateLimiting.MEMORY_HIGH_LOAD_THRESHOLD}%")
        log.info("Adaptive factors - Medium load: ${AdaptiveRateLimiting.MEDIUM_LOAD_FACTOR}, High load: ${AdaptiveRateLimiting.HIGH_LOAD_FACTOR}")
        log.info("Adaptive monitoring interval: ${AdaptiveRateLimiting.MONITORING_INTERVAL_MS}ms")

        // Log examples of adjusted limits
        log.info("Example adjusted limits at current load factor (${String.format("%.2f", AdaptiveRateLimiting.getCurrentLoadFactor())}): " +
                "Global: ${config.globalLimit} → ${AdaptiveRateLimiting.adjustRateLimit(config.globalLimit)}")
    } else {
        log.info("Adaptive rate limiting DISABLED")
    }
}

/**
 * Extract user ID from JWT token.
 * Parses the JWT token to extract the user ID from the subject claim.
 * Uses caching to avoid repeated parsing of the same token.
 */
private fun extractUserIdFromToken(authHeader: String): String? {
    try {
        // Remove "Bearer " prefix if present
        val token = if (authHeader.startsWith("Bearer ")) {
            authHeader.substring(7)
        } else {
            authHeader
        }

        // Calculate token hash for cache lookup
        val tokenHash = token.hashCode()

        // Check if token is in cache
        val cachedValue = tokenCache[tokenHash]
        if (cachedValue != null) {
            // Return cached user ID
            return cachedValue.first
        }

        // Token not in cache, parse it
        // Split the token into parts
        val parts = token.split(".")
        if (parts.size != 3) {
            return null // Not a valid JWT token
        }

        // Decode the payload (second part) - this is the expensive operation we want to cache
        val payload = String(java.util.Base64.getUrlDecoder().decode(parts[1]))

        // Extract the subject (user ID) using a simple regex
        // In a production environment, use a proper JWT library
        val subjectRegex = "\"sub\"\\s*:\\s*\"([^\"]+)\"".toRegex()
        val matchResult = subjectRegex.find(payload)

        // Determine user type in the same parsing operation to avoid duplicate work
        val userType = determineUserTypeFromPayload(payload)

        // Get the user ID
        val userId = matchResult?.groupValues?.get(1) ?: token.hashCode().toString()

        // Store in a cache for future use
        tokenCache[tokenHash] = Pair(userId, userType)

        return userId
    } catch (_: Exception) {
        // If any error occurs during parsing, fall back to using the token hash
        return authHeader.hashCode().toString()
    }
}

/**
 * Determine a user type from a JWT token.
 * Parses the JWT token to extract the user role from the claims.
 * Uses caching to avoid repeated parsing of the same token.
 */
private fun determineUserType(authHeader: String): String {
    try {
        // Remove "Bearer " prefix if present
        val token = if (authHeader.startsWith("Bearer ")) {
            authHeader.substring(7)
        } else {
            authHeader
        }

        // Calculate token hash for cache lookup
        val tokenHash = token.hashCode()

        // Check if token is in cache
        val cachedValue = tokenCache[tokenHash]
        if (cachedValue != null) {
            // Return cached user type
            return cachedValue.second
        }

        // Token not in cache, parse it
        // Split the token into parts
        val parts = token.split(".")
        if (parts.size != 3) {
            return "authenticated" // Default to authenticated if not a valid JWT
        }

        // Decode the payload (second part)
        val payload = String(java.util.Base64.getUrlDecoder().decode(parts[1]))

        // Determine user type from payload
        val userType = determineUserTypeFromPayload(payload)

        // Extract user ID in the same parsing operation to avoid duplicate work
        val subjectRegex = "\"sub\"\\s*:\\s*\"([^\"]+)\"".toRegex()
        val matchResult = subjectRegex.find(payload)
        val userId = matchResult?.groupValues?.get(1) ?: token.hashCode().toString()

        // Store in a cache for future use
        tokenCache[tokenHash] = Pair(userId, userType)

        return userType
    } catch (_: Exception) {
        // If any error occurs during parsing, default to authenticated
        return "authenticated"
    }
}

/**
 * Helper function to determine a user type from JWT payload.
 * Extracted to avoid code duplication between extractUserIdFromToken and determineUserType.
 */
private fun determineUserTypeFromPayload(payload: String): String {
    try {
        // Extract the role using a simple regex
        // Look for role, roles, or authority claims
        val roleRegex = "\"(role|roles|authorities)\"\\s*:\\s*\"([^\"]+)\"".toRegex()
        val matchResult = roleRegex.find(payload)

        if (matchResult != null) {
            val role = matchResult.groupValues[2].lowercase()
            return when {
                role.contains("admin") -> "admin"
                else -> "authenticated"
            }
        }

        // Check for an array of roles
        val rolesArrayRegex = "\"(role|roles|authorities)\"\\s*:\\s*\\[([^\\]]+)\\]".toRegex()
        val arrayMatchResult = rolesArrayRegex.find(payload)

        if (arrayMatchResult != null) {
            val rolesArray = arrayMatchResult.groupValues[2]
            return when {
                rolesArray.contains("admin") -> "admin"
                else -> "authenticated"
            }
        }

        // Default to authenticate if no role information found
        return "authenticated"
    } catch (_: Exception) {
        // If any error occurs during parsing, default to authenticated
        return "authenticated"
    }
}

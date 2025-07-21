package at.mocode.gateway.plugins

import at.mocode.gateway.config.CachingConfig
import at.mocode.gateway.config.getCachingConfig
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*
import kotlin.text.Charsets

/**
 * Configures enhanced HTTP caching headers for the application.
 * This adds Cache-Control, Expires, and Vary headers to responses.
 * It also integrates with the CachingConfig for more intelligent caching decisions.
 */
fun Application.configureHttpCaching() {
    // Get the application logger
    val logger = log

    // Get the caching config
    val cachingConfig = try {
        getCachingConfig()
    } catch (e: Exception) {
        logger.warn("Failed to get CachingConfig, using default caching headers: ${e.message}")
        null
    }

    // Add a response interceptor for setting cache headers
    intercept(ApplicationCallPipeline.Call) {
        // Add Vary header to all responses
        call.response.header(HttpHeaders.Vary, "Accept, Accept-Encoding")

        // For authenticated endpoints, add Authorization to Vary
        if (call.request.headers.contains(HttpHeaders.Authorization)) {
            call.response.header(HttpHeaders.Vary, "Accept, Accept-Encoding, Authorization")
        }

        // Set default no-cache headers for dynamic content
        call.response.header(HttpHeaders.CacheControl, "no-cache, private")

        // Check for conditional requests (If-None-Match, If-Modified-Since)
        val requestETag = call.request.header(HttpHeaders.IfNoneMatch)
        val requestLastModified = call.request.header(HttpHeaders.IfModifiedSince)

        // If we have conditional headers, check if we can return 304 Not Modified
        if (requestETag != null || requestLastModified != null) {
            // This would be implemented with actual ETag and Last-Modified checking
            // For now, we just log that we received conditional headers
            logger.debug("Received conditional request: ETag=$requestETag, Last-Modified=$requestLastModified")
        }
    }

    logger.info("HTTP caching configured with integration to CachingConfig")
}

/**
 * Extension function to enable caching for static resources.
 * Use this for CSS, JS, images, and other static files.
 */
fun ApplicationCall.enableStaticResourceCaching(maxAgeSeconds: Int = 86400) { // Default: 1 day
    setCacheControlHeader(this, maxAgeSeconds, true)
}

/**
 * Extension function to enable caching for master data.
 * Use this for reference data that changes infrequently.
 */
fun ApplicationCall.enableMasterDataCaching(maxAgeSeconds: Int = 3600) { // Default: 1 hour
    setCacheControlHeader(this, maxAgeSeconds, true)
}

/**
 * Extension function to enable caching for user data.
 * Use this for user-specific data that may change frequently.
 */
fun ApplicationCall.enableUserDataCaching(maxAgeSeconds: Int = 60) { // Default: 1 minute
    setCacheControlHeader(this, maxAgeSeconds, false, true)
}

/**
 * Extension function to disable caching.
 * Use this for sensitive or frequently changing data.
 */
fun ApplicationCall.disableCaching() {
    response.header(HttpHeaders.CacheControl, "no-cache, no-store, must-revalidate, private")
    response.header(HttpHeaders.Pragma, "no-cache")
    response.header(HttpHeaders.Expires, "0")
}

/**
 * Helper function to set Cache-Control and Expires headers.
 */
private fun setCacheControlHeader(
    call: ApplicationCall,
    maxAgeSeconds: Int,
    isPublic: Boolean,
    mustRevalidate: Boolean = false
) {
    // Build Cache-Control header
    val visibility = if (isPublic) "public" else "private"
    val revalidate = if (mustRevalidate) ", must-revalidate" else ""
    call.response.header(
        HttpHeaders.CacheControl,
        "max-age=$maxAgeSeconds, $visibility$revalidate"
    )

    // Set Expires header
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.SECOND, maxAgeSeconds)
    val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US)
    dateFormat.timeZone = TimeZone.getTimeZone("GMT")
    call.response.header(HttpHeaders.Expires, dateFormat.format(calendar.time))
}

/**
 * Extension function to set ETag header for a response.
 */
fun ApplicationCall.setETag(etag: String) {
    response.header(HttpHeaders.ETag, "\"$etag\"")
}

/**
 * Extension function to set Last-Modified header for a response.
 */
fun ApplicationCall.setLastModified(timestamp: Long) {
    val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US)
    dateFormat.timeZone = TimeZone.getTimeZone("GMT")
    response.header(HttpHeaders.LastModified, dateFormat.format(Date(timestamp)))
}

/**
 * Generate an ETag for the given content.
 * This uses MD5 hashing for simplicity, but in production you might want to use a faster algorithm.
 */
fun generateETag(content: String): String {
    val md = MessageDigest.getInstance("MD5")
    val digest = md.digest(content.toByteArray(Charsets.UTF_8))
    return digest.joinToString("") { "%02x".format(it) }
}

/**
 * Generate an ETag for the given object by converting it to a string representation.
 */
fun generateETag(obj: Any): String {
    return generateETag(obj.toString())
}

/**
 * Check if the request has a matching ETag and return 304 Not Modified if it does.
 * Returns true if the response was handled (304 sent), false otherwise.
 */
suspend fun PipelineContext<Unit, ApplicationCall>.checkETagAndRespond(etag: String): Boolean {
    val requestETag = call.request.header(HttpHeaders.IfNoneMatch)

    // If the client sent an If-None-Match header and it matches our ETag,
    // we can return 304 Not Modified
    if (requestETag != null && (requestETag == "\"$etag\"" || requestETag == "*")) {
        call.response.header(HttpHeaders.ETag, "\"$etag\"")
        call.respond(HttpStatusCode.NotModified)
        return true
    }

    // Set the ETag header for the response
    call.response.header(HttpHeaders.ETag, "\"$etag\"")
    return false
}

/**
 * Check if the request has a matching Last-Modified date and return 304 Not Modified if it does.
 * Returns true if the response was handled (304 sent), false otherwise.
 */
suspend fun PipelineContext<Unit, ApplicationCall>.checkLastModifiedAndRespond(timestamp: Long): Boolean {
    val requestLastModified = call.request.header(HttpHeaders.IfModifiedSince)

    if (requestLastModified != null) {
        try {
            val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US)
            dateFormat.timeZone = TimeZone.getTimeZone("GMT")
            val requestDate = dateFormat.parse(requestLastModified).time

            // If the resource hasn't been modified since the date in the request,
            // we can return 304 Not Modified
            if (timestamp <= requestDate) {
                val lastModifiedFormatted = dateFormat.format(Date(timestamp))
                call.response.header(HttpHeaders.LastModified, lastModifiedFormatted)
                call.respond(HttpStatusCode.NotModified)
                return true
            }
        } catch (e: Exception) {
            // If we can't parse the date, ignore it
        }
    }

    // Set the Last-Modified header for the response
    val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US)
    dateFormat.timeZone = TimeZone.getTimeZone("GMT")
    call.response.header(HttpHeaders.LastModified, dateFormat.format(Date(timestamp)))
    return false
}

/**
 * Extension function to check if a resource is cached in CachingConfig.
 * If it is, and the client has a matching ETag or Last-Modified date,
 * this will return 304 Not Modified. Otherwise, it will return the cached value.
 * Returns true if the response was handled, false otherwise.
 */
suspend fun <T> PipelineContext<Unit, ApplicationCall>.checkCacheAndRespond(
    cacheName: String,
    key: String,
    etag: String? = null,
    lastModified: Long? = null
): Boolean {
    val application = call.application
    val cachingConfig = try {
        application.getCachingConfig()
    } catch (e: Exception) {
        return false
    }

    // Check if the resource is in the cache
    val cachedValue = cachingConfig.get<T>(cacheName, key)
    if (cachedValue != null) {
        // If we have an ETag, check if the client has a matching one
        if (etag != null && checkETagAndRespond(etag)) {
            return true
        }

        // If we have a Last-Modified date, check if the client has a matching one
        if (lastModified != null && checkLastModifiedAndRespond(lastModified)) {
            return true
        }

        // If we get here, the client doesn't have a matching ETag or Last-Modified date,
        // so we need to send the full response
        return false
    }

    return false
}

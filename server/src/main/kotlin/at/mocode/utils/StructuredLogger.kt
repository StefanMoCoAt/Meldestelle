package at.mocode.utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Structured logging utility that provides convenient methods for logging with context
 * and structured data. Uses SLF4J with MDC for contextual information.
 */
class StructuredLogger(private val logger: Logger) {

    companion object {
        fun getLogger(name: String): StructuredLogger = StructuredLogger(LoggerFactory.getLogger(name))
        fun getLogger(clazz: Class<*>): StructuredLogger = StructuredLogger(LoggerFactory.getLogger(clazz))

        private val json = Json {
            prettyPrint = false
            ignoreUnknownKeys = true
        }
    }

    /**
     * Log with structured context data
     */
    fun info(message: String, context: Map<String, Any?> = emptyMap()) {
        withContext(context) {
            logger.info(message)
        }
    }

    fun debug(message: String, context: Map<String, Any?> = emptyMap()) {
        withContext(context) {
            logger.debug(message)
        }
    }

    fun warn(message: String, context: Map<String, Any?> = emptyMap()) {
        withContext(context) {
            logger.warn(message)
        }
    }

    fun error(message: String, throwable: Throwable? = null, context: Map<String, Any?> = emptyMap()) {
        withContext(context) {
            if (throwable != null) {
                logger.error(message, throwable)
            } else {
                logger.error(message)
            }
        }
    }

    /**
     * Log business events with structured data
     */
    fun logEvent(eventType: String, message: String, data: Map<String, Any?> = emptyMap()) {
        val eventContext = buildMap {
            put("event_type", eventType)
            put("event_timestamp", System.currentTimeMillis())
            putAll(data)
        }
        info(message, eventContext)
    }

    /**
     * Log API requests with structured data
     */
    fun logApiRequest(method: String, path: String, statusCode: Int? = null, duration: Long? = null, userId: String? = null) {
        val context = buildMap {
            put("http_method", method)
            put("http_path", path)
            put("request_type", "api_request")
            statusCode?.let { put("http_status", it) }
            duration?.let { put("duration_ms", it) }
            userId?.let { put("user_id", it) }
        }
        info("API Request: $method $path", context)
    }

    /**
     * Log database operations with structured data
     */
    fun logDatabaseOperation(operation: String, table: String, duration: Long? = null, recordCount: Int? = null) {
        val context = buildMap {
            put("db_operation", operation)
            put("db_table", table)
            put("operation_type", "database")
            duration?.let { put("duration_ms", it) }
            recordCount?.let { put("record_count", it) }
        }
        info("Database Operation: $operation on $table", context)
    }

    /**
     * Log service operations with structured data
     */
    fun logServiceOperation(service: String, operation: String, success: Boolean, duration: Long? = null, data: Map<String, Any?> = emptyMap()) {
        val context = buildMap {
            put("service_name", service)
            put("service_operation", operation)
            put("operation_success", success)
            put("operation_type", "service")
            duration?.let { put("duration_ms", it) }
            putAll(data)
        }
        val level = if (success) "info" else "error"
        val message = "Service Operation: $service.$operation ${if (success) "succeeded" else "failed"}"

        when (level) {
            "error" -> error(message, context = context)
            else -> info(message, context)
        }
    }

    /**
     * Execute a block with temporary MDC context
     */
    private fun withContext(context: Map<String, Any?>, block: () -> Unit) {
        val originalContext = MDC.getCopyOfContextMap() ?: emptyMap()
        try {
            // Add new context to MDC
            context.forEach { (key, value) ->
                when (value) {
                    null -> MDC.remove(key)
                    is String -> MDC.put(key, value)
                    is Number -> MDC.put(key, value.toString())
                    is Boolean -> MDC.put(key, value.toString())
                    else -> MDC.put(key, value.toString())
                }
            }
            block()
        } finally {
            // Restore original context
            MDC.clear()
            originalContext.forEach { (key, value) ->
                MDC.put(key, value)
            }
        }
    }

    /**
     * Create a child logger with additional context that will be included in all log messages
     */
    fun withContext(context: Map<String, Any?>): ContextualLogger {
        return ContextualLogger(this, context)
    }
}

/**
 * A logger that automatically includes contextual information in all log messages
 */
class ContextualLogger(
    private val parent: StructuredLogger,
    private val baseContext: Map<String, Any?>
) {
    fun info(message: String, additionalContext: Map<String, Any?> = emptyMap()) {
        parent.info(message, baseContext + additionalContext)
    }

    fun debug(message: String, additionalContext: Map<String, Any?> = emptyMap()) {
        parent.debug(message, baseContext + additionalContext)
    }

    fun warn(message: String, additionalContext: Map<String, Any?> = emptyMap()) {
        parent.warn(message, baseContext + additionalContext)
    }

    fun error(message: String, throwable: Throwable? = null, additionalContext: Map<String, Any?> = emptyMap()) {
        parent.error(message, throwable, baseContext + additionalContext)
    }

    fun logEvent(eventType: String, message: String, data: Map<String, Any?> = emptyMap()) {
        parent.logEvent(eventType, message, baseContext + data)
    }
}

/**
 * Extension functions for easy structured logging
 */
inline fun <reified T> T.structuredLogger(): StructuredLogger = StructuredLogger.getLogger(T::class.java)

/**
 * Measure execution time and log with structured data
 */
inline fun <T> StructuredLogger.measureAndLog(
    operation: String,
    context: Map<String, Any?> = emptyMap(),
    block: () -> T
): T {
    val startTime = System.currentTimeMillis()
    return try {
        val result = block()
        val duration = System.currentTimeMillis() - startTime
        info("Operation completed: $operation", context + mapOf("duration_ms" to duration, "success" to true))
        result
    } catch (e: Exception) {
        val duration = System.currentTimeMillis() - startTime
        error("Operation failed: $operation", e, context + mapOf("duration_ms" to duration, "success" to false))
        throw e
    }
}

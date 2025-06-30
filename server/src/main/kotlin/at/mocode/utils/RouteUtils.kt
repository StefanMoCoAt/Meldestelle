package at.mocode.utils

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom
import io.ktor.server.request.*
import io.ktor.server.routing.*
import at.mocode.utils.ResponseUtils.respondValidationError

/**
 * Utility functions for common route operations
 */
object RouteUtils {

    /**
     * Extract and validate UUID parameter from route
     */
    suspend fun RoutingCall.getUuidParameter(
        paramName: String,
        resourceName: String = paramName
    ): Uuid? {
        val paramValue = parameters[paramName]
        if (paramValue == null) {
            respondValidationError("Missing $resourceName ID")
            return null
        }

        return try {
            uuidFrom(paramValue)
        } catch (e: IllegalArgumentException) {
            respondValidationError("Invalid UUID format for $resourceName ID")
            null
        }
    }

    /**
     * Extract and validate required string parameter from route
     */
    suspend fun RoutingCall.getStringParameter(
        paramName: String,
        resourceName: String = paramName
    ): String? {
        val paramValue = parameters[paramName]
        if (paramValue.isNullOrBlank()) {
            respondValidationError("Missing or empty $resourceName parameter")
            return null
        }
        return paramValue
    }

    /**
     * Extract and validate boolean parameter from route
     */
    suspend fun RoutingCall.getBooleanParameter(
        paramName: String,
        resourceName: String = paramName
    ): Boolean? {
        val paramValue = parameters[paramName]
        if (paramValue == null) {
            respondValidationError("Missing $resourceName parameter")
            return null
        }

        return try {
            paramValue.toBoolean()
        } catch (e: Exception) {
            respondValidationError("Invalid boolean format for $resourceName parameter")
            null
        }
    }

    /**
     * Extract and validate required query parameter
     */
    suspend fun RoutingCall.getQueryParameter(
        paramName: String,
        resourceName: String = paramName
    ): String? {
        val paramValue = request.queryParameters[paramName]
        if (paramValue.isNullOrBlank()) {
            respondValidationError("Missing search query parameter '$paramName'")
            return null
        }
        return paramValue
    }

    /**
     * Safe receive with error handling
     */
    suspend inline fun <reified T : Any> RoutingCall.safeReceive(
        resourceName: String = "request body"
    ): T? {
        return try {
            receive<T>()
        } catch (e: Exception) {
            respondValidationError("Invalid $resourceName format", e.message)
            null
        }
    }

    /**
     * Execute repository operation with standardized error handling
     */
    suspend inline fun <T> RoutingCall.executeRepositoryOperation(
        operation: String,
        block: () -> T
    ): T? {
        return try {
            block()
        } catch (e: Exception) {
            ResponseUtils.run { handleException(e, operation) }
            null
        }
    }
}

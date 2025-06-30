package at.mocode.utils

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

/**
 * Standardized API response wrapper for a consistent response format
 */
@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: String? = null,
    val message: String? = null
)

/**
 * Error response data class
 */
@Serializable
data class ErrorResponse(
    val code: String,
    val message: String,
    val details: String? = null
)

/**
 * Utility object for common HTTP responses
 */
object ResponseUtils {

    /**
     * Respond with success and data
     */
    suspend inline fun <reified T> RoutingCall.respondSuccess(
        data: T,
        status: HttpStatusCode = HttpStatusCode.OK,
        message: String? = null
    ) {
        respond(status, ApiResponse(success = true, data = data, message = message))
    }

    /**
     * Respond with error
     */
    suspend fun RoutingCall.respondError(
        error: String,
        status: HttpStatusCode = HttpStatusCode.InternalServerError,
        details: String? = null
    ) {
        respond(status, ApiResponse<Nothing>(
            success = false,
            error = error,
            message = details
        ))
    }

    /**
     * Respond with validation error
     */
    suspend fun RoutingCall.respondValidationError(
        message: String,
        details: String? = null
    ) {
        respondError(
            error = "VALIDATION_ERROR",
            status = HttpStatusCode.BadRequest,
            details = "$message${details?.let { " - $it" } ?: ""}"
        )
    }

    /**
     * Respond with not found error
     */
    suspend fun RoutingCall.respondNotFound(
        resource: String = "Resource"
    ) {
        respondError(
            error = "NOT_FOUND",
            status = HttpStatusCode.NotFound,
            details = "$resource not found"
        )
    }

    /**
     * Respond with a created resource
     */
    suspend inline fun <reified T> RoutingCall.respondCreated(
        data: T,
        message: String? = null
    ) {
        respondSuccess(data, HttpStatusCode.Created, message)
    }

    /**
     * Respond with no content (for successful deletions)
     */
    suspend fun RoutingCall.respondNoContent() {
        respond(HttpStatusCode.NoContent)
    }

    /**
     * Handle common exceptions and respond appropriately
     */
    suspend fun RoutingCall.handleException(
        exception: Exception,
        operation: String = "operation"
    ) {
        when (exception) {
            is IllegalArgumentException -> respondValidationError(
                "Invalid input for $operation",
                exception.message
            )
            is NoSuchElementException -> respondNotFound()
            else -> respondError(
                "Internal server error during $operation",
                HttpStatusCode.InternalServerError,
                exception.message
            )
        }
    }
}

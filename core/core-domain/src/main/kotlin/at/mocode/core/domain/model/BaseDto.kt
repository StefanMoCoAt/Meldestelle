package at.mocode.core.domain.model

import at.mocode.core.domain.serialization.KotlinInstantSerializer
import at.mocode.core.domain.serialization.UuidSerializer
import com.benasher44.uuid.Uuid
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * A marker interface for all Data Transfer Objects.
 * While not strictly necessary, it can be useful for generic constraints.
 */
interface BaseDto

/**
 * Base DTO for domain entities that have a unique ID and audit timestamps.
 * Ensures that all primary entities share a common structure.
 */
@Serializable
abstract class EntityDto : BaseDto {
    @Serializable(with = UuidSerializer::class)
    abstract val id: Uuid

    @Serializable(with = KotlinInstantSerializer::class)
    abstract val createdAt: Instant

    @Serializable(with = KotlinInstantSerializer::class)
    abstract val updatedAt: Instant
}

/**
 * A structured representation of a single error.
 */
@Serializable
data class ErrorDto(
    val code: String, // A machine-readable error code, e.g., "VALIDATION_ERROR"
    val message: String, // A human-readable message, e.g., "Email is not valid"
    val field: String? = null // Optional: The specific field the error relates to
) : BaseDto

/**
 * A standardized and consistent wrapper for all API responses.
 * It clearly separates the data payload from metadata about the request's success and potential errors.
 *
 * @param T The type of the data payload.
 */
@Serializable
data class ApiResponse<T>(
    val data: T?,
    val success: Boolean,
    val errors: List<ErrorDto> = emptyList(), // OPTIMIZED: Using structured ErrorDto
    val timestamp: Instant = Clock.System.now()
) {
    companion object {
        /**
         * Factory function to create a standardized success response.
         */
        fun <T> success(data: T): ApiResponse<T> {
            return ApiResponse(data = data, success = true)
        }

        /**
         * Factory function to create a standardized error response.
         */
        fun <T> error(
            code: String,
            message: String,
            field: String? = null
        ): ApiResponse<T> {
            return ApiResponse(
                data = null,
                success = false,
                errors = listOf(ErrorDto(code = code, message = message, field = field))
            )
        }

        /**
         * Factory function to create a standardized error response with multiple errors.
         */
        fun <T> error(errors: List<ErrorDto>): ApiResponse<T> {
            return ApiResponse(data = null, success = false, errors = errors)
        }
    }
}

/**
 * A standardized wrapper for paginated API responses.
 * Contains the list of items for the current page as well as all necessary pagination metadata.
 *
 * @param T The type of the content in the page.
 */
@Serializable
data class PagedResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)

// REMOVED: The PaginationDto was redundant as all its information is already contained within PagedResponse.

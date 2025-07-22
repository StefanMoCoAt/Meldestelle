package at.mocode.core.domain.model

import at.mocode.core.domain.serialization.KotlinInstantSerializer
import at.mocode.core.domain.serialization.UuidSerializer
import com.benasher44.uuid.Uuid
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Base DTO interface for all data transfer objects
 */
interface BaseDto

/**
 * Base DTO for entities with ID and timestamps
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
 * Standard API response wrapper
 */
@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ErrorDto? = null,
    val message: String? = null
) : BaseDto {
    companion object {
        /**
         * Creates a successful API response with data
         */
        fun <T> success(data: T, message: String? = null): ApiResponse<T> {
            return ApiResponse(
                success = true,
                data = data,
                message = message
            )
        }

        /**
         * Creates an error API response
         */
        fun <T> error(message: String, code: String = "ERROR", details: Map<String, String>? = null): ApiResponse<T> {
            return ApiResponse(
                success = false,
                error = ErrorDto(
                    code = code,
                    message = message,
                    details = details
                )
            )
        }
    }
}

/**
 * Error information DTO
 */
@Serializable
data class ErrorDto(
    val code: String,
    val message: String,
    val details: Map<String, String>? = null
) : BaseDto

/**
 * Pagination information
 */
@Serializable
data class PaginationDto(
    val page: Int,
    val size: Int,
    val total: Long,
    val totalPages: Int
) : BaseDto

/**
 * Paginated response wrapper
 */
@Serializable
data class PagedResponse<T>(
    val data: List<T>,
    val pagination: PaginationDto
) : BaseDto


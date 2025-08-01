package at.mocode.core.domain.model

import at.mocode.core.domain.serialization.KotlinInstantSerializer
import at.mocode.core.domain.serialization.UuidSerializer
import com.benasher44.uuid.Uuid
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.serialization.Serializable

/**
 * A marker interface for all Data Transfer Objects.
 */
interface BaseDto

/**
 * Base DTO for domain entities that have unique ID and audit timestamps.
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
    val code: String,
    val message: String,
    val field: String? = null
) : BaseDto

/**
 * A standardized and consistent wrapper for all API responses.
 */
@Serializable
data class ApiResponse<T>(
    val data: T?,
    val success: Boolean,
    val errors: List<ErrorDto> = emptyList(),
    @Serializable(with = KotlinInstantSerializer::class)
    val timestamp: Instant = Clock.System.now()
) {
    companion object {
        fun <T> success(data: T): ApiResponse<T> {
            return ApiResponse(data = data, success = true)
        }

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

        fun <T> error(errors: List<ErrorDto>): ApiResponse<T> {
            return ApiResponse(data = null, success = false, errors = errors)
        }
    }
}

/**
 * A standardized wrapper for paginated API responses.
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

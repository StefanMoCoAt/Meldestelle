package at.mocode.core.domain.model

import at.mocode.core.domain.serialization.KotlinInstantSerializer
import at.mocode.core.domain.serialization.UuidSerializer
import com.benasher44.uuid.Uuid
import kotlinx.datetime.Clock
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
 * A standardized wrapper for all API responses.
 * Provides a consistent structure for data, success status, and errors.
 * @param T The type of the data payload.
 */
@Serializable
data class ApiResponse<T>(
    val data: T? = null,
    val success: Boolean = true,
    val message: String? = null,
    val errors: List<String> = emptyList(),
    val timestamp: Instant = Clock.System.now()
)

/**
 * A standardized wrapper for paginated API responses.
 * @param T The type of the content in the page.
 */
data class PagedResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)

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




package at.mocode.dto.base

import kotlinx.serialization.Serializable

/**
 * Base interface for all versioned DTOs.
 * Provides version information for API compatibility and evolution.
 */
interface VersionedDto {
    /**
     * The schema version of this DTO.
     * Used for API versioning and backward compatibility.
     */
    val schemaVersion: String

    /**
     * Optional data version for optimistic locking.
     * Can be used to detect concurrent modifications.
     */
    val dataVersion: Long?
        get() = null
}

/**
 * Base class for versioned DTOs with common versioning fields.
 */
@Serializable
abstract class BaseVersionedDto(
    override val schemaVersion: String = "1.0",
    override val dataVersion: Long? = null
) : VersionedDto

/**
 * Version information for API responses.
 */
@Serializable
data class ApiVersionInfo(
    val apiVersion: String,
    val supportedVersions: List<String>,
    val deprecatedVersions: List<String> = emptyList(),
    val minimumClientVersion: String? = null
)

/**
 * Wrapper for versioned API responses.
 */
@Serializable
data class VersionedResponse<T>(
    val data: T,
    val version: ApiVersionInfo,
    val timestamp: String
) where T : VersionedDto

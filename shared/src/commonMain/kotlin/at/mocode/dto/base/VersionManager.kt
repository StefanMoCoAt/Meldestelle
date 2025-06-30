package at.mocode.dto.base

import kotlinx.serialization.Serializable

/**
 * Manages API and DTO versioning across the application.
 */
object VersionManager {

    // Current API version
    const val CURRENT_API_VERSION = "1.0"

    // Supported API versions (newest first)
    val SUPPORTED_VERSIONS = listOf("1.0")

    // Deprecated versions (still supported but discouraged)
    val DEPRECATED_VERSIONS = emptyList<String>()

    // Minimum client version required
    const val MINIMUM_CLIENT_VERSION = "1.0"

    /**
     * Check if a version is supported
     */
    fun isVersionSupported(version: String): Boolean {
        return version in SUPPORTED_VERSIONS
    }

    /**
     * Check if a version is deprecated
     */
    fun isVersionDeprecated(version: String): Boolean {
        return version in DEPRECATED_VERSIONS
    }

    /**
     * Get version compatibility info
     */
    fun getVersionInfo(): ApiVersionInfo {
        return ApiVersionInfo(
            apiVersion = CURRENT_API_VERSION,
            supportedVersions = SUPPORTED_VERSIONS,
            deprecatedVersions = DEPRECATED_VERSIONS,
            minimumClientVersion = MINIMUM_CLIENT_VERSION
        )
    }

    /**
     * Validate client version compatibility
     */
    fun validateClientVersion(clientVersion: String?): VersionValidationResult {
        if (clientVersion == null) {
            return VersionValidationResult.MissingVersion
        }

        if (!isVersionSupported(clientVersion)) {
            return VersionValidationResult.UnsupportedVersion(clientVersion)
        }

        if (isVersionDeprecated(clientVersion)) {
            return VersionValidationResult.DeprecatedVersion(clientVersion)
        }

        return VersionValidationResult.Valid(clientVersion)
    }
}

/**
 * Result of version validation
 */
sealed class VersionValidationResult {
    data class Valid(val version: String) : VersionValidationResult()
    data class DeprecatedVersion(val version: String) : VersionValidationResult()
    data class UnsupportedVersion(val version: String) : VersionValidationResult()
    object MissingVersion : VersionValidationResult()
}

/**
 * Version migration interface for handling DTO evolution
 */
interface VersionMigrator<T : VersionedDto> {
    /**
     * Migrate DTO from one version to another
     */
    fun migrate(dto: T, fromVersion: String, toVersion: String): T

    /**
     * Check if migration is supported between versions
     */
    fun canMigrate(fromVersion: String, toVersion: String): Boolean
}

/**
 * Registry for version migrators
 */
object MigratorRegistry {
    private val migrators = mutableMapOf<String, VersionMigrator<*>>()

    fun <T : VersionedDto> register(dtoClass: String, migrator: VersionMigrator<T>) {
        migrators[dtoClass] = migrator
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : VersionedDto> getMigrator(dtoClass: String): VersionMigrator<T>? {
        return migrators[dtoClass] as? VersionMigrator<T>
    }
}

/**
 * Version compatibility annotations
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class Since(val version: String)

@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class Deprecated(val version: String, val message: String = "")

@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class Until(val version: String)

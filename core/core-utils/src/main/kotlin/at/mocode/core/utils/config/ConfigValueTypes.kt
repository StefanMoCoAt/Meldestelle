package at.mocode.core.utils.config

import kotlinx.serialization.Serializable

/**
 * Value classes for strongly typed configuration parameters.
 * These provide compile-time type safety for configuration values.
 */

// === Network Configuration Value Classes ===

/**
 * A strongly typed wrapper for port numbers.
 */
@Serializable
@JvmInline
value class Port(val value: Int) {
    init {
        require(value in 1..65535) { "Port must be between 1 and 65535, got: $value" }
    }

    override fun toString(): String = value.toString()
}

/**
 * A strongly typed wrapper for host names or IP addresses.
 */
@Serializable
@JvmInline
value class Host(val value: String) {
    init {
        require(value.isNotBlank()) { "Host cannot be blank" }
        require(value.length <= 253) { "Host name cannot exceed 253 characters" }
    }

    override fun toString(): String = value
}

// === Database Configuration Value Classes ===

/**
 * A strongly typed wrapper for database names.
 */
@Serializable
@JvmInline
value class DatabaseName(val value: String) {
    init {
        require(value.isNotBlank()) { "Database name cannot be blank" }
        require(value.matches(Regex("^[a-zA-Z][a-zA-Z0-9_]*$"))) {
            "Database name must start with a letter and contain only alphanumeric characters and underscores"
        }
    }

    override fun toString(): String = value
}

/**
 * A strongly typed wrapper for database usernames.
 */
@Serializable
@JvmInline
value class DatabaseUsername(val value: String) {
    init {
        require(value.isNotBlank()) { "Database username cannot be blank" }
    }

    override fun toString(): String = value
}

/**
 * A strongly typed wrapper for database passwords.
 */
@Serializable
@JvmInline
value class DatabasePassword(val value: String) {
    init {
        require(value.isNotBlank()) { "Database password cannot be blank" }
    }

    override fun toString(): String = "***" // Never expose the actual password

    fun getValue(): String = value
}

/**
 * A strongly typed wrapper for JDBC URLs.
 */
@Serializable
@JvmInline
value class JdbcUrl(val value: String) {
    init {
        require(value.isNotBlank()) { "JDBC URL cannot be blank" }
        require(value.startsWith("jdbc:")) { "JDBC URL must start with 'jdbc:'" }
    }

    override fun toString(): String = value
}

/**
 * A strongly typed wrapper for connection pool sizes.
 */
@Serializable
@JvmInline
value class PoolSize(val value: Int) {
    init {
        require(value > 0) { "Pool size must be positive" }
        require(value <= 1000) { "Pool size cannot exceed 1000" }
    }

    override fun toString(): String = value.toString()
}

// === Security Configuration Value Classes ===

/**
 * A strongly typed wrapper for API keys.
 */
@Serializable
@JvmInline
value class ApiKey(val value: String) {
    init {
        require(value.isNotBlank()) { "API key cannot be blank" }
        require(value.length >= 16) { "API key must be at least 16 characters long" }
    }

    override fun toString(): String = "***" // Never expose the actual key

    fun getValue(): String = value
}

/**
 * A strongly typed wrapper for JWT secrets.
 */
@Serializable
@JvmInline
value class JwtSecret(val value: String) {
    init {
        require(value.isNotBlank()) { "JWT secret cannot be blank" }
        require(value.length >= 32) { "JWT secret must be at least 32 characters long" }
    }

    override fun toString(): String = "***" // Never expose the actual secret

    fun getValue(): String = value
}

/**
 * A strongly typed wrapper for JWT issuer.
 */
@Serializable
@JvmInline
value class JwtIssuer(val value: String) {
    init {
        require(value.isNotBlank()) { "JWT issuer cannot be blank" }
    }

    override fun toString(): String = value
}

/**
 * A strongly typed wrapper for JWT audience.
 */
@Serializable
@JvmInline
value class JwtAudience(val value: String) {
    init {
        require(value.isNotBlank()) { "JWT audience cannot be blank" }
    }

    override fun toString(): String = value
}

/**
 * A strongly typed wrapper for JWT realm.
 */
@Serializable
@JvmInline
value class JwtRealm(val value: String) {
    init {
        require(value.isNotBlank()) { "JWT realm cannot be blank" }
    }

    override fun toString(): String = value
}

// === Application Configuration Value Classes ===

/**
 * A strongly typed wrapper for application names.
 */
@Serializable
@JvmInline
value class ApplicationName(val value: String) {
    init {
        require(value.isNotBlank()) { "Application name cannot be blank" }
        require(value.matches(Regex("^[A-Za-z][A-Za-z0-9-_]*$"))) {
            "Application name must start with a letter and contain only letters, numbers, hyphens, and underscores"
        }
    }

    override fun toString(): String = value
}

/**
 * A strongly typed wrapper for application versions.
 */
@Serializable
@JvmInline
value class ApplicationVersion(val value: String) {
    init {
        require(value.isNotBlank()) { "Application version cannot be blank" }
        require(value.matches(Regex("^\\d+\\.\\d+\\.\\d+(-[a-zA-Z0-9]+)?$"))) {
            "Application version must follow semantic versioning (e.g., 1.0.0 or 1.0.0-beta)"
        }
    }

    override fun toString(): String = value
}

/**
 * A strongly typed wrapper for worker thread counts.
 */
@Serializable
@JvmInline
value class WorkerCount(val value: Int) {
    init {
        require(value > 0) { "Worker count must be positive" }
        require(value <= Runtime.getRuntime().availableProcessors() * 4) {
            "Worker count should not exceed 4 times the available processors"
        }
    }

    override fun toString(): String = value.toString()
}

/**
 * A strongly typed wrapper for rate limits.
 */
@Serializable
@JvmInline
value class RateLimit(val value: Int) {
    init {
        require(value > 0) { "Rate limit must be positive" }
    }

    override fun toString(): String = value.toString()
}

/**
 * A strongly typed wrapper for time periods in minutes.
 */
@Serializable
@JvmInline
value class PeriodMinutes(val value: Int) {
    init {
        require(value > 0) { "Period must be positive" }
    }

    override fun toString(): String = value.toString()
}

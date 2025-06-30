package at.mocode.config

import io.ktor.server.application.*

/**
 * Application configuration management
 * Centralizes all configuration settings for better maintainability
 */
object AppConfig {

    /**
     * Application information
     */
    data class AppInfo(
        val name: String,
        val version: String,
        val environment: String,
        val description: String
    )

    /**
     * Database configuration
     */
    data class DatabaseConfig(
        val url: String,
        val driver: String,
        val user: String,
        val password: String,
        val maxPoolSize: Int = 10,
        val connectionTimeout: Long = 30000
    )

    /**
     * API configuration
     */
    data class ApiConfig(
        val baseUrl: String,
        val version: String,
        val enableCors: Boolean = true,
        val enableSwagger: Boolean = false,
        val rateLimitEnabled: Boolean = false
    )

    /**
     * Security configuration
     */
    data class SecurityConfig(
        val jwtSecret: String? = null,
        val jwtIssuer: String? = null,
        val jwtAudience: String? = null,
        val sessionTimeout: Long = 3600000, // 1 hour
        val enableAuthentication: Boolean = false
    )

    /**
     * Load configuration from application environment
     */
    fun loadConfig(application: Application): AppConfiguration {
        val config = application.environment.config

        val appInfo = AppInfo(
            name = config.propertyOrNull("application.name")?.getString() ?: "Meldestelle API Server",
            version = config.propertyOrNull("application.version")?.getString() ?: "1.0.0",
            environment = config.propertyOrNull("application.environment")?.getString() ?: "development",
            description = config.propertyOrNull("application.description")?.getString() ?: "Equestrian Event Management API"
        )

        val databaseConfig = DatabaseConfig(
            url = config.propertyOrNull("database.url")?.getString() ?: "jdbc:postgresql://localhost:5432/meldestelle",
            driver = config.propertyOrNull("database.driver")?.getString() ?: "org.postgresql.Driver",
            user = config.propertyOrNull("database.user")?.getString() ?: "postgres",
            password = config.propertyOrNull("database.password")?.getString() ?: "password",
            maxPoolSize = config.propertyOrNull("database.maxPoolSize")?.getString()?.toIntOrNull() ?: 10,
            connectionTimeout = config.propertyOrNull("database.connectionTimeout")?.getString()?.toLongOrNull() ?: 30000
        )

        val apiConfig = ApiConfig(
            baseUrl = config.propertyOrNull("api.baseUrl")?.getString() ?: "http://localhost:8080",
            version = config.propertyOrNull("api.version")?.getString() ?: "v1",
            enableCors = config.propertyOrNull("api.enableCors")?.getString()?.toBoolean() ?: true,
            enableSwagger = config.propertyOrNull("api.enableSwagger")?.getString()?.toBoolean() ?: (appInfo.environment == "development"),
            rateLimitEnabled = config.propertyOrNull("api.rateLimitEnabled")?.getString()?.toBoolean() ?: false
        )

        val securityConfig = SecurityConfig(
            jwtSecret = config.propertyOrNull("security.jwt.secret")?.getString(),
            jwtIssuer = config.propertyOrNull("security.jwt.issuer")?.getString(),
            jwtAudience = config.propertyOrNull("security.jwt.audience")?.getString(),
            sessionTimeout = config.propertyOrNull("security.sessionTimeout")?.getString()?.toLongOrNull() ?: 3600000,
            enableAuthentication = config.propertyOrNull("security.enableAuthentication")?.getString()?.toBoolean() ?: false
        )

        return AppConfiguration(appInfo, databaseConfig, apiConfig, securityConfig)
    }
}

/**
 * Complete application configuration
 */
data class AppConfiguration(
    val app: AppConfig.AppInfo,
    val database: AppConfig.DatabaseConfig,
    val api: AppConfig.ApiConfig,
    val security: AppConfig.SecurityConfig
) {
    /**
     * Check if running in development mode
     */
    val isDevelopment: Boolean
        get() = app.environment.lowercase() == "development"

    /**
     * Check if running in production mode
     */
    val isProduction: Boolean
        get() = app.environment.lowercase() == "production"

    /**
     * Get application info string for API endpoint
     */
    fun getAppInfoString(): String {
        return "${app.name} v${app.version} - Running in ${app.environment} mode"
    }
}

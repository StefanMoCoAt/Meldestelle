package at.mocode.config

import java.io.File
import java.io.FileInputStream
import java.util.Properties
import org.slf4j.LoggerFactory

/**
 * Configuration for email service.
 * Loads configuration from environment variables with fallbacks to the .env file and then default values.
 */
object EmailConfig {
    private val log = LoggerFactory.getLogger(EmailConfig::class.java)

    // Load values from the .env file if they exist
    private val envProperties = loadEnvFile()

    /**
     * Loads environment variables from .env file
     * @return Property object containing the variables from .env file or empty Properties if a file doesn't exist
     */
    private fun loadEnvFile(): Properties {
        val properties = Properties()

        // Try multiple possible locations for the .env file
        val possibleLocations = listOf(
            ".env",                // Current working directory
            "../.env",             // Parent directory
            "../../.env"           // Grandparent directory
        )

        for (location in possibleLocations) {
            val envFile = File(location)
            if (envFile.exists()) {
                try {
                    FileInputStream(envFile).use { fis ->
                        properties.load(fis)
                    }
                    log.info("Loaded .env file from: ${envFile.absolutePath}")
                    break  // Stop after successfully loading the file
                } catch (e: Exception) {
                    log.error("Error loading .env file from ${envFile.absolutePath}: ${e.message}")
                }
            }
        }

        return properties
    }

    /**
     * Helper function to get value from environment, .env file, or default
     * @param key The environment variable key
     * @param defaultValue The default value to use if the key is not found
     * @return The value from environment, .env file, or default
     */
    private fun getConfigValue(key: String, defaultValue: String): String {
        val value = System.getenv(key) ?: envProperties.getProperty(key) ?: defaultValue
        if (value == defaultValue) {
            log.debug("Using default value for $key: $defaultValue")
        }
        return value
    }

    // SMTP server configuration
    val smtpHost: String = getConfigValue("SMTP_HOST", "smtp.gmail.com")
    val smtpPort: Int = System.getenv("SMTP_PORT")?.toIntOrNull()
        ?: envProperties.getProperty("SMTP_PORT")?.toIntOrNull()
        ?: 587

    // Authentication credentials
    val smtpUsername: String = getConfigValue("SMTP_USER", "")
    val smtpPassword: String = getConfigValue("SMTP_PASSWORD", "")

    // Email addresses
    val recipientEmail: String = getConfigValue("RECIPIENT_EMAIL", "")
    val senderEmail: String = getConfigValue("SMTP_SENDER_EMAIL", smtpUsername)

    /**
     * Validates that all required configuration is present.
     * @return true if the configuration is valid, false otherwise
     */
    fun isValid(): Boolean {
        return smtpHost.isNotBlank() &&
               smtpPort > 0 &&
               smtpUsername.isNotBlank() &&
               smtpPassword.isNotBlank() &&
               recipientEmail.isNotBlank() &&
               senderEmail.isNotBlank()
    }

    /**
     * Returns a string describing any missing configuration.
     * @return A string with error messages or empty string if the configuration is valid
     */
    fun getValidationErrors(): String {
        val errors = mutableListOf<String>()

        if (smtpHost.isBlank()) errors.add("SMTP_HOST is not configured")
        if (smtpPort <= 0) errors.add("SMTP_PORT is invalid")
        if (smtpUsername.isBlank()) errors.add("SMTP_USER is not configured")
        if (smtpPassword.isBlank()) errors.add("SMTP_PASSWORD is not configured")
        if (recipientEmail.isBlank()) errors.add("RECIPIENT_EMAIL is not configured")
        if (senderEmail.isBlank()) errors.add("SMTP_SENDER_EMAIL is not configured")

        return errors.joinToString(", ")
    }
}

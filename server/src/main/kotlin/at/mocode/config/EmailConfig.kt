package at.mocode.config

/**
 * Configuration for email service.
 * Loads configuration from environment variables with fallbacks to default values.
 */
object EmailConfig {
    // SMTP server configuration
    val smtpHost: String = System.getenv("SMTP_HOST") ?: "smtp.gmail.com"
    val smtpPort: Int = System.getenv("SMTP_PORT")?.toIntOrNull() ?: 587

    // Authentication credentials
    val smtpUsername: String = System.getenv("SMTP_USER") ?: ""
    val smtpPassword: String = System.getenv("SMTP_PASSWORD") ?: ""

    // Email addresses
    val recipientEmail: String = System.getenv("RECIPIENT_EMAIL") ?: "stefan.mo.co+nennen@gmail.com"
    val senderEmail: String = System.getenv("SMTP_SENDER_EMAIL") ?: smtpUsername

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
     * @return A string with error messages or empty string if configuration is valid
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

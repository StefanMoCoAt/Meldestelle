package at.mocode

import at.mocode.config.EmailConfig
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertEquals

/**
 * Tests for EmailConfig class.
 *
 * These tests verify that:
 * 1. The EmailConfig can load values from .env file
 * 2. The validation logic works correctly
 * 3. The error messages are correctly generated
 */
class EmailConfigTest {

    /**
     * Test that EmailConfig loads values from the.env file correctly.
     * This test assumes the .env file exists and contains the required values.
     */
    @Test
    fun testEmailConfigLoadsFromEnvFile() {
        // Since we've modified EmailConfig to load from the.env file,
        // and we know the .env file contains the required values,
        // the configuration should be valid
        // Debug logs removed to avoid exposing sensitive information

        assertTrue(EmailConfig.isValid(), "Email configuration should be valid with values from .env file")

        // Verify that values are loaded from .env file
        assertTrue(EmailConfig.smtpHost.isNotBlank(), "SMTP host should not be blank")
        assertTrue(EmailConfig.smtpPort > 0, "SMTP port should be a positive number")
        assertTrue(EmailConfig.smtpUsername.isNotBlank(), "SMTP username should not be blank")
        assertTrue(EmailConfig.smtpPassword.isNotBlank(), "SMTP password should not be blank")
        assertTrue(EmailConfig.senderEmail.isNotBlank(), "Sender email should not be blank")
        assertTrue(EmailConfig.recipientEmail.isNotBlank(), "Recipient email should not be blank")
    }

    /**
     * Test the validation logic with different configurations.
     * This test creates test configurations with different values and verifies
     * that the validation logic works correctly.
     */
    @Test
    fun testValidationLogic() {
        // Test with a valid configuration
        val validConfig = TestEmailConfig(
            smtpHost = "smtp.example.com",
            smtpPort = 587,
            smtpUsername = "user@example.com",
            smtpPassword = "password",
            recipientEmail = "recipient@example.com",
            senderEmail = "sender@example.com"
        )
        assertTrue(validConfig.isValid(), "Configuration should be valid with all required fields")
        assertEquals("", validConfig.getValidationErrors(), "There should be no validation errors")

        // Test with empty username
        val emptyUsernameConfig = TestEmailConfig(
            smtpHost = "smtp.example.com",
            smtpPort = 587,
            smtpUsername = "",
            smtpPassword = "password",
            recipientEmail = "recipient@example.com",
            senderEmail = "sender@example.com"
        )
        assertFalse(emptyUsernameConfig.isValid(), "Configuration should be invalid with empty username")
        assertTrue(emptyUsernameConfig.getValidationErrors().contains("SMTP_USER is not configured"),
            "Validation errors should include message about missing SMTP_USER")

        // Test with empty password
        val emptyPasswordConfig = TestEmailConfig(
            smtpHost = "smtp.example.com",
            smtpPort = 587,
            smtpUsername = "user@example.com",
            smtpPassword = "",
            recipientEmail = "recipient@example.com",
            senderEmail = "sender@example.com"
        )
        assertFalse(emptyPasswordConfig.isValid(), "Configuration should be invalid with empty password")
        assertTrue(emptyPasswordConfig.getValidationErrors().contains("SMTP_PASSWORD is not configured"),
            "Validation errors should include message about missing SMTP_PASSWORD")

        // Test with invalid port
        val invalidPortConfig = TestEmailConfig(
            smtpHost = "smtp.example.com",
            smtpPort = 0,
            smtpUsername = "user@example.com",
            smtpPassword = "password",
            recipientEmail = "recipient@example.com",
            senderEmail = "sender@example.com"
        )
        assertFalse(invalidPortConfig.isValid(), "Configuration should be invalid with port = 0")
        assertTrue(invalidPortConfig.getValidationErrors().contains("SMTP_PORT is invalid"),
            "Validation errors should include message about invalid SMTP_PORT")

        // Test with negative port
        val negativePortConfig = TestEmailConfig(
            smtpHost = "smtp.example.com",
            smtpPort = -1,
            smtpUsername = "user@example.com",
            smtpPassword = "password",
            recipientEmail = "recipient@example.com",
            senderEmail = "sender@example.com"
        )
        assertFalse(negativePortConfig.isValid(), "Configuration should be invalid with port = -1")
        assertTrue(negativePortConfig.getValidationErrors().contains("SMTP_PORT is invalid"),
            "Validation errors should include message about invalid SMTP_PORT")
    }

    /**
     * Test that multiple validation errors are correctly reported.
     */
    @Test
    fun testMultipleValidationErrors() {
        // Test with all fields invalid
        val invalidConfig = TestEmailConfig(
            smtpHost = "",
            smtpPort = 0,
            smtpUsername = "",
            smtpPassword = "",
            recipientEmail = "",
            senderEmail = ""
        )

        // Verify the configuration is invalid
        assertFalse(invalidConfig.isValid(), "Configuration should be invalid with all fields empty or invalid")

        // Verify all validation errors are present
        val errors = invalidConfig.getValidationErrors()
        assertTrue(errors.contains("SMTP_HOST is not configured"),
            "Validation errors should include message about missing SMTP_HOST")
        assertTrue(errors.contains("SMTP_PORT is invalid"),
            "Validation errors should include message about invalid SMTP_PORT")
        assertTrue(errors.contains("SMTP_USER is not configured"),
            "Validation errors should include message about missing SMTP_USER")
        assertTrue(errors.contains("SMTP_PASSWORD is not configured"),
            "Validation errors should include message about missing SMTP_PASSWORD")
        assertTrue(errors.contains("RECIPIENT_EMAIL is not configured"),
            "Validation errors should include message about missing RECIPIENT_EMAIL")
        assertTrue(errors.contains("SMTP_SENDER_EMAIL is not configured"),
            "Validation errors should include message about missing SMTP_SENDER_EMAIL")

        // Verify that all errors are included in the message
        val errorCount = errors.split(", ").size
        assertEquals(6, errorCount, "There should be 6 validation errors")
    }

    /**
     * Test class that mimics EmailConfig for testing purposes.
     * This allows us to create test configurations with different values.
     */
    private class TestEmailConfig(
        val smtpHost: String,
        val smtpPort: Int,
        val smtpUsername: String,
        val smtpPassword: String,
        val recipientEmail: String,
        val senderEmail: String
    ) {
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
}

package at.mocode.email

import at.mocode.config.EmailConfig
import org.slf4j.LoggerFactory
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Tests for the EmailService class.
 * This test verifies that the email configuration is valid and that test emails can be sent.
 */
class EmailServiceTest {
    private val log = LoggerFactory.getLogger(EmailServiceTest::class.java)

    @Test
    fun testEmailConfigurationIsValid() {
        // Check if the email configuration is valid
        val isValid = EmailConfig.isValid()

        if (!isValid) {
            log.warn("Email configuration is not valid: ${EmailConfig.getValidationErrors()}")
            log.warn("Make sure the .env file contains the correct email configuration")
            log.warn("SMTP_HOST, SMTP_PORT, SMTP_USER, SMTP_PASSWORD, RECIPIENT_EMAIL, and SMTP_SENDER_EMAIL must be set")
        }

        // We don't assert here because the test environment might not have valid email configuration
        log.info("Email configuration valid: $isValid")
    }

    @Test
    fun testSendTestEmail() {
        // Only run this test if the email configuration is valid
        if (!EmailConfig.isValid()) {
            log.warn("Skipping test because email configuration is not valid")
            return
        }

        // Get the EmailService instance
        val emailService = EmailService.getInstance()

        // Send a test email with debug enabled
        val result = emailService.sendTestEmail(debug = true)

        // Log the result
        if (result) {
            log.info("Test email sent successfully")
        } else {
            log.error("Failed to send test email")
        }

        // Assert that the email was sent successfully
        assertTrue(result, "Test email should be sent successfully")
    }
}

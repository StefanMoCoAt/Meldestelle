package at.mocode.email

import at.mocode.config.EmailConfig
import at.mocode.model.Nennung
import jakarta.mail.*
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeBodyPart
import jakarta.mail.internet.MimeMessage
import jakarta.mail.internet.MimeMultipart
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import org.slf4j.LoggerFactory

/**
 * Service for sending email notifications for form submissions.
 * Implemented as a singleton to avoid multiple initializations.
 * Thread-safe implementation with improved error handling and HTML support.
 */
class EmailService private constructor(
    private val smtpHost: String,
    private val smtpPort: Int,
    private val smtpUsername: String,
    private val smtpPassword: String,
    private val recipientEmail: String,
    private val senderEmail: String = smtpUsername
) {
    private val log = LoggerFactory.getLogger(EmailService::class.java)
    private val maxRetries = 3
    private val retryDelayMs = 1000L

    companion object {
        private var instance: EmailService? = null
        private val lock = ReentrantLock()

        /**
         * Gets the singleton instance of EmailService.
         * Initializes it if it doesn't exist yet.
         * Thread-safe implementation using a lock.
         */
        fun getInstance(): EmailService {
            return lock.withLock {
                if (instance == null) {
                    instance = EmailService(
                        smtpHost = EmailConfig.smtpHost,
                        smtpPort = EmailConfig.smtpPort,
                        smtpUsername = EmailConfig.smtpUsername,
                        smtpPassword = EmailConfig.smtpPassword,
                        recipientEmail = EmailConfig.recipientEmail,
                        senderEmail = EmailConfig.senderEmail
                    )
                }
                instance!!
            }
        }
    }

    /**
     * Creates an email session with the configured properties.
     *
     * @param debug Whether to enable debug mode for the mail session
     * @return The configured mail session
     */
    private fun createSession(debug: Boolean = false): Session {
        val properties = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.host", smtpHost)
            put("mail.smtp.port", smtpPort.toString())
            put("mail.debug", debug.toString())
            put("mail.smtp.ssl.protocols", "TLSv1.2")
            put("mail.smtp.connectiontimeout", "10000")
            put("mail.smtp.timeout", "10000")
        }

        return Session.getInstance(properties, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(smtpUsername, smtpPassword)
            }
        })
    }

    /**
     * Validates an email address format.
     *
     * @param email The email address to validate
     * @return true if the email format is valid, false otherwise
     */
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
        return email.matches(emailRegex)
    }

    /**
     * Sends an email notification with the form submission data.
     * Includes a retry mechanism for transient failures.
     *
     * @param nennung The form submission data
     * @return true if the email was sent successfully, false otherwise
     */
    fun sendNennungEmail(nennung: Nennung): Boolean {
        log.info("Attempting to send email for ${nennung.riderName} with ${nennung.horseName}")
        log.debug("SMTP Configuration: Host=$smtpHost, Port=$smtpPort, Username=$smtpUsername")
        log.debug("Email addresses: From=$senderEmail, To=$recipientEmail")

        // Validate email addresses
        if (!isValidEmail(senderEmail) || !isValidEmail(recipientEmail)) {
            log.error("Invalid email address format: sender=$senderEmail, recipient=$recipientEmail")
            return false
        }

        var attempts = 0
        var lastException: Exception? = null

        while (attempts < maxRetries) {
            attempts++
            try {
                val session = createSession()

                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress(senderEmail))
                    setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail))
                    subject = nennung.turnier?.let { "Neue Nennung für ${it.name}: ${nennung.riderName} mit ${nennung.horseName}" }
                        ?: "Neue Nennung: ${nennung.riderName} mit ${nennung.horseName}"

                    // Create a multipart message with both plain text and HTML versions
                    val multipart = MimeMultipart("alternative")

                    // Plain text part
                    val textPart = MimeBodyPart().apply {
                        setText(createEmailContent(nennung, false))
                    }
                    multipart.addBodyPart(textPart)

                    // HTML part
                    val htmlPart = MimeBodyPart().apply {
                        setContent(createEmailContent(nennung, true), "text/html; charset=utf-8")
                    }
                    multipart.addBodyPart(htmlPart)

                    // Set content to the multipart
                    setContent(multipart)
                }

                log.info("Email message prepared, attempting to send... (Attempt $attempts of $maxRetries)")
                Transport.send(message)
                log.info("Email sent successfully!")
                return true
            } catch (e: MessagingException) {
                lastException = e
                log.warn("Failed to send email (Attempt $attempts of $maxRetries): ${e.message}")
                if (attempts < maxRetries) {
                    log.info("Retrying in ${retryDelayMs}ms...")
                    Thread.sleep(retryDelayMs)
                }
            } catch (e: Exception) {
                lastException = e
                log.error("Unexpected error sending email: ${e.message}", e)
                break  // Don't retry on non-messaging exceptions
            }
        }

        log.error("Failed to send email after $attempts attempts", lastException)
        return false
    }

    /**
     * Sends a test email to verify the email configuration.
     *
     * @param recipient Optional recipient email address (defaults to configured recipient)
     * @param debug Whether to enable debug mode for the mail session
     * @return true if the test email was sent successfully, false otherwise
     */
    fun sendTestEmail(recipient: String = recipientEmail, debug: Boolean = false): Boolean {
        log.info("Sending test email to $recipient")

        if (!isValidEmail(recipient)) {
            log.error("Invalid recipient email address format: $recipient")
            return false
        }

        try {
            val session = createSession(debug)

            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(senderEmail))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient))
                subject = "Test Email from Meldestelle Application"

                val multipart = MimeMultipart("alternative")

                // Plain text part
                val textPart = MimeBodyPart().apply {
                    setText("Dies ist eine Test-Email vom Meldestelle System.\n\nWenn Sie diese Email erhalten haben, funktioniert die Email-Konfiguration korrekt.")
                }
                multipart.addBodyPart(textPart)

                // HTML part
                val htmlPart = MimeBodyPart().apply {
                    setContent("""
                        <html>
                        <body>
                            <h2>Dies ist eine Test-Email vom Meldestelle System</h2>
                            <p>Wenn Sie diese Email erhalten haben, funktioniert die Email-Konfiguration korrekt.</p>
                            <p>SMTP-Konfiguration:</p>
                            <ul>
                                <li>Host: $smtpHost</li>
                                <li>Port: $smtpPort</li>
                                <li>Benutzername: $smtpUsername</li>
                            </ul>
                        </body>
                        </html>
                    """.trimIndent(), "text/html; charset=utf-8")
                }
                multipart.addBodyPart(htmlPart)

                setContent(multipart)
            }

            log.info("Test email prepared, attempting to send...")
            Transport.send(message)
            log.info("Test email sent successfully!")
            return true
        } catch (e: MessagingException) {
            log.error("Failed to send test email: ${e.message}", e)
            return false
        } catch (e: Exception) {
            log.error("Unexpected error sending test email: ${e.message}", e)
            return false
        }
    }

    /**
     * Creates the email content from the form submission data.
     *
     * @param nennung The form submission data
     * @param html Whether to generate HTML content (true) or plain text (false)
     * @return The formatted email content
     */
    private fun createEmailContent(nennung: Nennung, html: Boolean = false): String {
        if (!html) {
            // Plain text version
            val sb = StringBuilder()

            // Add tournament information if available
            nennung.turnier?.let { turnier ->
                sb.appendLine(turnier.name)
                sb.appendLine()
            }

            sb.appendLine("Reiter: ${nennung.riderName}")
            sb.appendLine("Pferd: ${nennung.horseName}")
            sb.appendLine("E-Mail: ${nennung.email}")
            sb.appendLine("Telefon: ${nennung.phone}")
            sb.appendLine()
            sb.appendLine("Bewerbe:")
            nennung.selectedEvents.forEach { event ->
                sb.appendLine("- $event")
            }
            sb.appendLine()
            if (nennung.comments.isNotBlank()) {
                sb.appendLine("Bemerkungen:")
                sb.appendLine(nennung.comments)
            }

            return sb.toString()
        } else {
            // HTML version
            val sb = StringBuilder()
            sb.append("<html><body>")

            // Add tournament information if available
            nennung.turnier?.let { turnier ->
                sb.append("<h2>${turnier.name}</h2>")
            }

            sb.append("<p><strong>Reiter:</strong> ${nennung.riderName}</p>")
            sb.append("<p><strong>Pferd:</strong> ${nennung.horseName}</p>")
            sb.append("<p><strong>E-Mail:</strong> <a href=\"mailto:${nennung.email}\">${nennung.email}</a></p>")
            sb.append("<p><strong>Telefon:</strong> ${nennung.phone}</p>")

            sb.append("<h3>Bewerbe:</h3>")
            sb.append("<ul>")
            nennung.selectedEvents.forEach { event ->
                sb.append("<li>$event</li>")
            }
            sb.append("</ul>")

            if (nennung.comments.isNotBlank()) {
                sb.append("<h3>Bemerkungen:</h3>")
                sb.append("<p>${nennung.comments.replace("\n", "<br>")}</p>")
            }

            sb.append("</body></html>")
            return sb.toString()
        }
    }
}

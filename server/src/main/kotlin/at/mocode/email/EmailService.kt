package at.mocode.email

import at.mocode.model.Nennung
import jakarta.mail.*
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import java.util.*
import org.slf4j.LoggerFactory

/**
 * Service for sending email notifications for form submissions.
 */
class EmailService(
    private val smtpHost: String,
    private val smtpPort: Int,
    private val smtpUsername: String,
    private val smtpPassword: String,
    private val recipientEmail: String,
    private val senderEmail: String = smtpUsername
) {
    private val log = LoggerFactory.getLogger(EmailService::class.java)

    /**
     * Sends an email notification with the form submission data.
     *
     * @param nennung The form submission data
     * @return true if the email was sent successfully, false otherwise
     */
    fun sendNennungEmail(nennung: Nennung): Boolean {
        log.info("Attempting to send email for ${nennung.riderName} with ${nennung.horseName}")
        log.debug("SMTP Configuration: Host=$smtpHost, Port=$smtpPort, Username=$smtpUsername")
        log.debug("Email addresses: From=$senderEmail, To=$recipientEmail")

        val properties = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.host", smtpHost)
            put("mail.smtp.port", smtpPort.toString())
            // Debug property set to false for production
            put("mail.debug", "false")

            // Simplified SSL/TLS configuration that works with Gmail
            put("mail.smtp.ssl.protocols", "TLSv1.2")

            // Note: The previous configuration with socketFactory was causing SSL/TLS handshake issues
        }

        return try {
            val session = Session.getInstance(properties, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(smtpUsername, smtpPassword)
                }
            })

            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(senderEmail))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail))
                subject = nennung.turnier?.let { "Neue Nennung für ${it.name}: ${nennung.riderName} mit ${nennung.horseName}" }
                    ?: "Neue Nennung: ${nennung.riderName} mit ${nennung.horseName}"
                setText(createEmailContent(nennung))
            }

            log.info("Email message prepared, attempting to send...")
            Transport.send(message)
            log.info("Email sent successfully!")
            true
        } catch (e: MessagingException) {
            log.error("Failed to send email: ${e.message}", e)
            false
        }
    }

    /**
     * Creates the email content from the form submission data.
     *
     * @param nennung The form submission data
     * @return The formatted email content
     */
    private fun createEmailContent(nennung: Nennung): String {
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
    }
}

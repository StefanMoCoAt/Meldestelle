package at.mocode.email

import at.mocode.model.Nennung
import jakarta.mail.*
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import java.util.*

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

    /**
     * Sends an email notification with the form submission data.
     *
     * @param nennung The form submission data
     * @return true if the email was sent successfully, false otherwise
     */
    fun sendNennungEmail(nennung: Nennung): Boolean {
        println("Attempting to send email for ${nennung.riderName} with ${nennung.horseName}")
        println("SMTP Configuration: Host=$smtpHost, Port=$smtpPort, Username=$smtpUsername")
        println("Email addresses: From=$senderEmail, To=$recipientEmail")

        val properties = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.host", smtpHost)
            put("mail.smtp.port", smtpPort.toString())
            // Add debug property
            put("mail.debug", "true")

            // Simplified SSL/TLS configuration that works with Gmail
            put("mail.smtp.ssl.protocols", "TLSv1.2")
            put("mail.smtp.ssl.trust", "*")

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
                subject = "Neue Nennung: ${nennung.riderName} mit ${nennung.horseName}"
                setText(createEmailContent(nennung))
            }

            println("Email message prepared, attempting to send...")
            Transport.send(message)
            println("Email sent successfully!")
            true
        } catch (e: MessagingException) {
            println("Failed to send email: ${e.message}")
            e.printStackTrace()
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
        sb.appendLine("Neue Nennung eingegangen:")
        sb.appendLine()
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

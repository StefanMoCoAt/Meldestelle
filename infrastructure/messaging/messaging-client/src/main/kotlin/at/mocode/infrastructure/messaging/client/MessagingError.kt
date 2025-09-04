package at.mocode.infrastructure.messaging.client

/**
 * Domänenspezifische Fehlertypen für Messaging-Operationen.
 * Folgt den DDD-Richtlinien mit expliziter Fehlerbehandlung über das Result-Pattern.
 */
sealed class MessagingError(
    val code: String,
    override val message: String,
    override val cause: Throwable? = null
) : Exception(message, cause) {

    /**
     * Fehler beim Veröffentlichen aufgrund von Serialisierungsproblemen.
     */
    data class SerializationError(
        override val message: String,
        override val cause: Throwable? = null
    ) : MessagingError("MESSAGING_SERIALIZATION_ERROR", message, cause)

    /**
     * Fehler beim Veröffentlichen aufgrund von Verbindungsproblemen.
     */
    data class ConnectionError(
        override val message: String,
        override val cause: Throwable? = null
    ) : MessagingError("MESSAGING_CONNECTION_ERROR", message, cause)

    /**
     * Fehler beim Veröffentlichen aufgrund von Zeitüberschreitung.
     */
    data class TimeoutError(
        override val message: String,
        override val cause: Throwable? = null
    ) : MessagingError("MESSAGING_TIMEOUT_ERROR", message, cause)

    /**
     * Fehler aufgrund von Authentifizierungs-/Autorisierungsproblemen.
     */
    data class AuthenticationError(
        override val message: String,
        override val cause: Throwable? = null
    ) : MessagingError("MESSAGING_AUTHENTICATION_ERROR", message, cause)

    /**
     * Fehler aufgrund von Topic-Konfigurationsproblemen.
     */
    data class TopicConfigurationError(
        override val message: String,
        override val cause: Throwable? = null
    ) : MessagingError("MESSAGING_TOPIC_CONFIGURATION_ERROR", message, cause)

    /**
     * Fehler beim Empfangen aufgrund von Deserialisierungsproblemen.
     */
    data class DeserializationError(
        override val message: String,
        override val cause: Throwable? = null
    ) : MessagingError("MESSAGING_DESERIALIZATION_ERROR", message, cause)

    /**
     * Generischer Messaging-Fehler für unerwartete Ausfälle.
     */
    data class UnexpectedError(
        override val message: String,
        override val cause: Throwable? = null
    ) : MessagingError("MESSAGING_UNEXPECTED_ERROR", message, cause)
}

package at.mocode.infrastructure.messaging.client

/**
 * Domain-specific error types for messaging operations.
 * Follows the DDD guidelines for explicit error handling using the Result pattern.
 */
sealed class MessagingError(
    val code: String,
    override val message: String,
    override val cause: Throwable? = null
) : Exception(message, cause) {

    /**
     * Error when event publishing fails due to serialization issues.
     */
    data class SerializationError(
        override val message: String,
        override val cause: Throwable? = null
    ) : MessagingError("MESSAGING_SERIALIZATION_ERROR", message, cause)

    /**
     * Error when event publishing fails due to connection issues.
     */
    data class ConnectionError(
        override val message: String,
        override val cause: Throwable? = null
    ) : MessagingError("MESSAGING_CONNECTION_ERROR", message, cause)

    /**
     * Error when event publishing fails due to timeout.
     */
    data class TimeoutError(
        override val message: String,
        override val cause: Throwable? = null
    ) : MessagingError("MESSAGING_TIMEOUT_ERROR", message, cause)

    /**
     * Error when event publishing fails due to authentication/authorization issues.
     */
    data class AuthenticationError(
        override val message: String,
        override val cause: Throwable? = null
    ) : MessagingError("MESSAGING_AUTHENTICATION_ERROR", message, cause)

    /**
     * Error when event publishing fails due to topic configuration issues.
     */
    data class TopicConfigurationError(
        override val message: String,
        override val cause: Throwable? = null
    ) : MessagingError("MESSAGING_TOPIC_CONFIGURATION_ERROR", message, cause)

    /**
     * Error when event consumption fails due to deserialization issues.
     */
    data class DeserializationError(
        override val message: String,
        override val cause: Throwable? = null
    ) : MessagingError("MESSAGING_DESERIALIZATION_ERROR", message, cause)

    /**
     * Generic messaging error for unexpected failures.
     */
    data class UnexpectedError(
        override val message: String,
        override val cause: Throwable? = null
    ) : MessagingError("MESSAGING_UNEXPECTED_ERROR", message, cause)
}

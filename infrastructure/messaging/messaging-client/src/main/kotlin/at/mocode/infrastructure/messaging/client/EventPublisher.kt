package at.mocode.infrastructure.messaging.client

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Interface for publishing domain events to message broker.
 *
 * Follows DDD principles with explicit error handling using domain-specific error types.
 * All operations use the Result pattern for type-safe error handling as required by guidelines.
 */
interface EventPublisher {

    /**
     * Publishes a single event to the specified topic.
     *
     * @param topic The Kafka topic to publish to
     * @param key Optional message key for partitioning
     * @param event The domain event to publish
     * @return Result<Unit> indicating success or MessagingError exception for specific failure reason
     */
    suspend fun publishEvent(topic: String, key: String? = null, event: Any): Result<Unit>

    /**
     * Publishes multiple events to the specified topic in batch.
     *
     * @param topic The Kafka topic to publish to
     * @param events List of key-event pairs to publish
     * @return Result<List<Unit>> with success indicators or MessagingError exception for failure reason
     */
    suspend fun publishEvents(topic: String, events: List<Pair<String?, Any>>): Result<List<Unit>>

    /**
     * Legacy reactive methods for backward compatibility.
     * These will be deprecated in favor of the Result-based methods above.
     */
    @Deprecated("Use suspending publishEvent with Result instead", ReplaceWith("publishEvent(topic, key, event)"))
    fun publishEventReactive(topic: String, key: String? = null, event: Any): Mono<Unit>

    @Deprecated("Use suspending publishEvents with Result instead", ReplaceWith("publishEvents(topic, events)"))
    fun publishEventsReactive(topic: String, events: List<Pair<String?, Any>>): Flux<Unit>
}

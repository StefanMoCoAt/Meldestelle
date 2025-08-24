package at.mocode.infrastructure.messaging.client

import reactor.core.publisher.Flux
import kotlinx.coroutines.flow.Flow

/**
 * A generic interface for consuming events from a message broker.
 *
 * Follows DDD principles with explicit error handling using domain-specific error types.
 * Provides both Result-based methods and reactive streams for flexibility.
 */
interface EventConsumer {

    /**
     * Receives events from the specified topic with explicit error handling.
     *
     * @param T The expected type of the event payload
     * @param topic The topic to subscribe to
     * @param eventType The class type of events to consume
     * @return Flow<Result<T>> where each Result contains either a successful event or MessagingError
     */
    fun <T : Any> receiveEventsWithResult(topic: String, eventType: Class<T>): Flow<Result<T>>

    /**
     * Legacy reactive method for receiving events.
     *
     * This method returns a cold Flux, meaning that the consumer will only start
     * listening for messages once the Flux is subscribed to.
     *
     * @param T The expected type of the event payload.
     * @param topic The topic to subscribe to.
     * @return A reactive stream (Flux) of events of type T.
     */
    @Deprecated("Use receiveEventsWithResult with Flow<Result<T>> instead", ReplaceWith("receiveEventsWithResult(topic, eventType)"))
    fun <T : Any> receiveEvents(topic: String, eventType: Class<T>): Flux<T>
}

/**
 * Kotlin-idiomatic extension function for `receiveEventsWithResult` using reified types.
 *
 * Example: `consumer.receiveEventsWithResult<MyEvent>("my-topic").collect { result -> ... }`
 */
inline fun <reified T : Any> EventConsumer.receiveEventsWithResult(topic: String): Flow<Result<T>> {
    return this.receiveEventsWithResult(topic, T::class.java)
}

/**
 * Kotlin-idiomatic extension function for `receiveEvents` using reified types.
 *
 * Example: `consumer.receiveEvents<MyEvent>("my-topic").subscribe { ... }`
 */
@Deprecated("Use receiveEventsWithResult with Flow<Result<T>> instead", ReplaceWith("receiveEventsWithResult<T>(topic)"))
inline fun <reified T : Any> EventConsumer.receiveEvents(topic: String): Flux<T> {
    return this.receiveEvents(topic, T::class.java)
}

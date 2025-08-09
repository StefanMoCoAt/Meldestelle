package at.mocode.infrastructure.messaging.client

import reactor.core.publisher.Flux

/**
 * A generic, reactive interface for consuming events from a message broker.
 */
interface EventConsumer {

    /**
     * Receives a continuous stream of events from the specified topic.
     *
     * This method returns a cold Flux, meaning that the consumer will only start
     * listening for messages once the Flux is subscribed to.
     *
     * @param T The expected type of the event payload.
     * @param topic The topic to subscribe to.
     * @return A reactive stream (Flux) of events of type T.
     */
    fun <T : Any> receiveEvents(topic: String, eventType: Class<T>): Flux<T>
}

/**
 * Kotlin-idiomatic extension function for `receiveEvents` using reified types.
 *
 * Example: `consumer.receiveEvents<MyEvent>("my-topic").subscribe { ... }`
 */
inline fun <reified T : Any> EventConsumer.receiveEvents(topic: String): Flux<T> {
    return this.receiveEvents(topic, T::class.java)
}

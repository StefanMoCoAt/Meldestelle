package at.mocode.infrastructure.messaging.client

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Interface for publishing domain events to message broker.
 */
interface EventPublisher {

    /**
     * Publishes a single event to the specified topic.
     * Returns a Mono that completes when the send operation is finished.
     */
    fun publishEvent(topic: String, key: String? = null, event: Any): Mono<Void>

    /**
     * Publishes multiple events to the specified topic.
     * Returns a Flux that completes when all send operations are finished.
     */
    fun publishEvents(topic: String, events: List<Pair<String?, Any>>): Flux<Void>
}

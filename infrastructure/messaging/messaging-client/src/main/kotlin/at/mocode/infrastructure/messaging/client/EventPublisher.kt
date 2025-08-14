package at.mocode.infrastructure.messaging.client

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Interface for publishing domain events to message broker.
 */
interface EventPublisher {

    /**
     * Publishes a single event to the specified topic.
     * Returns a Mono that emits Unit when the send operation is finished.
     */
    fun publishEvent(topic: String, key: String? = null, event: Any): Mono<Unit>

    /**
     * Publishes multiple events to the specified topic.
     * Returns a Flux that emits one Unit per successfully published event.
     */
    fun publishEvents(topic: String, events: List<Pair<String?, Any>>): Flux<Unit>
}

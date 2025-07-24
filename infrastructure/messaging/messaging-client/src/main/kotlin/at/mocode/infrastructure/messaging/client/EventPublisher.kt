package at.mocode.infrastructure.messaging.client

/**
 * Interface for publishing domain events to message broker.
 */
interface EventPublisher {

    /**
     * Publishes an event to the specified topic.
     *
     * @param topic The topic to publish to
     * @param key The message key (optional)
     * @param event The event to publish
     */
    suspend fun publishEvent(topic: String, key: String? = null, event: Any)

    /**
     * Publishes multiple events to the specified topic.
     *
     * @param topic The topic to publish to
     * @param events The events to publish with their keys
     */
    suspend fun publishEvents(topic: String, events: List<Pair<String?, Any>>)
}

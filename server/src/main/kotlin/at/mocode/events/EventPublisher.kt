package at.mocode.events

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

/**
 * Interface for handling domain events
 */
interface EventHandler<T : DomainEvent> {
    suspend fun handle(event: T)
}

/**
 * Event publisher that manages event handlers and publishes events
 */
class EventPublisher {
    private val logger = LoggerFactory.getLogger(EventPublisher::class.java)
    private val handlers = mutableMapOf<String, MutableList<EventHandler<DomainEvent>>>()
    private val eventStore = mutableListOf<DomainEvent>()

    /**
     * Register an event handler for a specific event type
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : DomainEvent> registerHandler(eventType: String, handler: EventHandler<T>) {
        handlers.getOrPut(eventType) { mutableListOf() }
            .add(handler as EventHandler<DomainEvent>)
        logger.info("Registered handler for event type: $eventType")
    }

    /**
     * Publish an event to all registered handlers
     */
    suspend fun publish(event: DomainEvent) {
        logger.info("Publishing event: ${event.eventType} with ID: ${event.eventId}")

        // Store the event (simple in-memory event store for now)
        eventStore.add(event)

        // Get handlers for this event type
        val eventHandlers = handlers[event.eventType] ?: emptyList()

        if (eventHandlers.isEmpty()) {
            logger.warn("No handlers registered for event type: ${event.eventType}")
            return
        }

        // Execute handlers asynchronously
        eventHandlers.forEach { handler ->
            CoroutineScope(Dispatchers.Default).launch {
                try {
                    handler.handle(event)
                    logger.debug("Successfully handled event ${event.eventId} with handler ${handler::class.simpleName}")
                } catch (e: Exception) {
                    logger.error("Error handling event ${event.eventId} with handler ${handler::class.simpleName}", e)
                }
            }
        }
    }

    /**
     * Get all events from the event store
     */
    fun getAllEvents(): List<DomainEvent> = eventStore.toList()

    /**
     * Get events by aggregate ID
     */
    fun getEventsByAggregateId(aggregateId: com.benasher44.uuid.Uuid): List<DomainEvent> {
        return eventStore.filter { it.aggregateId == aggregateId }
    }

    /**
     * Get events by type
     */
    fun getEventsByType(eventType: String): List<DomainEvent> {
        return eventStore.filter { it.eventType == eventType }
    }

    /**
     * Clear all events (useful for testing)
     */
    fun clearEvents() {
        eventStore.clear()
        logger.info("Event store cleared")
    }

    /**
     * Clear all handlers (useful for testing)
     */
    fun clearHandlers() {
        handlers.clear()
        logger.info("All event handlers cleared")
    }

    companion object {
        @Volatile
        private var INSTANCE: EventPublisher? = null

        fun getInstance(): EventPublisher {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: EventPublisher().also { INSTANCE = it }
            }
        }
    }
}

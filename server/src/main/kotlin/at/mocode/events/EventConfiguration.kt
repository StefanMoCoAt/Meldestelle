package at.mocode.events

import at.mocode.events.handlers.TurnierAnalyticsHandler
import at.mocode.events.handlers.TurnierAuditHandler
import at.mocode.events.handlers.TurnierCacheHandler
import at.mocode.events.handlers.TurnierNotificationHandler
import org.slf4j.LoggerFactory

/**
 * Configuration class for setting up event-driven architecture.
 * Registers all event handlers with the EventPublisher.
 */
object EventConfiguration {
    private val logger = LoggerFactory.getLogger(EventConfiguration::class.java)

    /**
     * Initialize and configure all event handlers
     */
    fun configureEventHandlers() {
        val eventPublisher = EventPublisher.getInstance()

        logger.info("Configuring event handlers...")

        // Register tournament event handlers
        registerTurnierEventHandlers(eventPublisher)

        logger.info("Event handlers configured successfully")
    }

    /**
     * Register all tournament-related event handlers
     */
    private fun registerTurnierEventHandlers(eventPublisher: EventPublisher) {
        // Audit handler - logs all tournament events
        val auditHandler = TurnierAuditHandler()
        eventPublisher.registerHandler("TurnierCreated", auditHandler)
        eventPublisher.registerHandler("TurnierUpdated", auditHandler)
        eventPublisher.registerHandler("TurnierDeleted", auditHandler)
        eventPublisher.registerHandler("TurnierRegistrationOpened", auditHandler)
        eventPublisher.registerHandler("TurnierRegistrationClosed", auditHandler)
        eventPublisher.registerHandler("TurnierStatusChanged", auditHandler)

        // Notification handler - sends notifications for important events
        val notificationHandler = TurnierNotificationHandler()
        eventPublisher.registerHandler("TurnierCreated", notificationHandler)
        eventPublisher.registerHandler("TurnierRegistrationOpened", notificationHandler)
        eventPublisher.registerHandler("TurnierRegistrationClosed", notificationHandler)
        eventPublisher.registerHandler("TurnierStatusChanged", notificationHandler)

        // Analytics handler - records metrics and analytics
        val analyticsHandler = TurnierAnalyticsHandler()
        eventPublisher.registerHandler("TurnierCreated", analyticsHandler)
        eventPublisher.registerHandler("TurnierRegistrationClosed", analyticsHandler)
        eventPublisher.registerHandler("TurnierStatusChanged", analyticsHandler)

        // Cache handler - invalidates caches when data changes
        val cacheHandler = TurnierCacheHandler()
        eventPublisher.registerHandler("TurnierCreated", cacheHandler)
        eventPublisher.registerHandler("TurnierUpdated", cacheHandler)
        eventPublisher.registerHandler("TurnierDeleted", cacheHandler)

        logger.info("Registered handlers for tournament events")
    }

    /**
     * Clear all event handlers (useful for testing)
     */
    fun clearEventHandlers() {
        EventPublisher.getInstance().clearHandlers()
        logger.info("All event handlers cleared")
    }
}

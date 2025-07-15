package at.mocode.events.handlers

import at.mocode.events.*
import at.mocode.utils.StructuredLogger

/**
 * Handler for tournament audit logging
 */
class TurnierAuditHandler : EventHandler<DomainEvent> {
    private val log = StructuredLogger.getLogger(TurnierAuditHandler::class.java)

    override suspend fun handle(event: DomainEvent) {
        when (event) {
            is TurnierCreatedEvent -> {
                log.logEvent("tournament_created", "Tournament created", mapOf(
                    "handler" to "audit",
                    "turnier_id" to event.turnier.id.toString(),
                    "turnier_titel" to event.turnier.titel,
                    "oeps_turnier_nr" to event.turnier.oepsTurnierNr.toString(),
                    "veranstaltung_id" to event.turnier.veranstaltungId.toString()
                ))
            }
            is TurnierUpdatedEvent -> {
                log.logEvent("tournament_updated", "Tournament updated", mapOf(
                    "handler" to "audit",
                    "turnier_id" to event.updatedTurnier.id.toString(),
                    "turnier_titel" to event.updatedTurnier.titel,
                    "title_changed" to (event.previousTurnier.titel != event.updatedTurnier.titel)
                ))
                if (event.previousTurnier.titel != event.updatedTurnier.titel) {
                    log.logEvent("tournament_title_changed", "Tournament title changed", mapOf(
                        "handler" to "audit",
                        "turnier_id" to event.updatedTurnier.id.toString(),
                        "previous_title" to event.previousTurnier.titel,
                        "new_title" to event.updatedTurnier.titel
                    ))
                }
            }
            is TurnierDeletedEvent -> {
                log.logEvent("tournament_deleted", "Tournament deleted", mapOf(
                    "handler" to "audit",
                    "turnier_id" to event.deletedTurnier.id.toString(),
                    "turnier_titel" to event.deletedTurnier.titel
                ))
            }
            is TurnierRegistrationOpenedEvent -> {
                log.logEvent("tournament_registration_opened", "Tournament registration opened", mapOf(
                    "handler" to "audit",
                    "turnier_id" to event.turnierId.toString(),
                    "turnier_titel" to event.turnierTitel
                ))
            }
            is TurnierRegistrationClosedEvent -> {
                log.logEvent("tournament_registration_closed", "Tournament registration closed", mapOf(
                    "handler" to "audit",
                    "turnier_id" to event.turnierId.toString(),
                    "turnier_titel" to event.turnierTitel,
                    "total_registrations" to event.totalRegistrations
                ))
            }
            is TurnierStatusChangedEvent -> {
                log.logEvent("tournament_status_changed", "Tournament status changed", mapOf(
                    "handler" to "audit",
                    "turnier_id" to event.turnierId.toString(),
                    "turnier_titel" to event.turnierTitel,
                    "previous_status" to event.previousStatus,
                    "new_status" to event.newStatus
                ))
            }
            else -> {
                log.debug("Unhandled event type in audit handler", mapOf(
                    "handler" to "audit",
                    "event_type" to event.eventType
                ))
            }
        }
    }
}

/**
 * Handler for tournament notifications (email, SMS, etc.)
 */
class TurnierNotificationHandler : EventHandler<DomainEvent> {
    private val log = StructuredLogger.getLogger(TurnierNotificationHandler::class.java)

    override suspend fun handle(event: DomainEvent) {
        when (event) {
            is TurnierCreatedEvent -> {
                log.logEvent("tournament_notification_sent", "Sending tournament creation notifications", mapOf(
                    "handler" to "notification",
                    "notification_type" to "tournament_created",
                    "turnier_id" to event.turnier.id.toString(),
                    "turnier_titel" to event.turnier.titel
                ))
                // Here you would integrate with email/SMS services
                sendNotificationToStakeholders(
                    "New Tournament Created",
                    "Tournament '${event.turnier.titel}' has been created for ${event.turnier.datumVon} - ${event.turnier.datumBis}"
                )
            }
            is TurnierRegistrationOpenedEvent -> {
                log.logEvent("tournament_notification_sent", "Sending registration opened notifications", mapOf(
                    "handler" to "notification",
                    "notification_type" to "registration_opened",
                    "turnier_id" to event.turnierId.toString(),
                    "turnier_titel" to event.turnierTitel
                ))
                sendNotificationToParticipants(
                    "Tournament Registration Open",
                    "Registration is now open for tournament '${event.turnierTitel}'. Deadline: ${event.registrationDeadline}"
                )
            }
            is TurnierRegistrationClosedEvent -> {
                log.logEvent("tournament_notification_sent", "Sending registration closed notifications", mapOf(
                    "handler" to "notification",
                    "notification_type" to "registration_closed",
                    "turnier_id" to event.turnierId.toString(),
                    "turnier_titel" to event.turnierTitel,
                    "total_registrations" to event.totalRegistrations
                ))
                sendNotificationToStakeholders(
                    "Tournament Registration Closed",
                    "Registration for tournament '${event.turnierTitel}' is now closed. Total registrations: ${event.totalRegistrations}"
                )
            }
            is TurnierStatusChangedEvent -> {
                if (event.newStatus == "COMPLETED") {
                    log.logEvent("tournament_notification_sent", "Sending tournament completion notifications", mapOf(
                        "handler" to "notification",
                        "notification_type" to "tournament_completed",
                        "turnier_id" to event.turnierId.toString(),
                        "turnier_titel" to event.turnierTitel,
                        "new_status" to event.newStatus
                    ))
                    sendNotificationToParticipants(
                        "Tournament Completed",
                        "Tournament '${event.turnierTitel}' has been completed. Results will be available soon."
                    )
                }
            }
            else -> {
                log.debug("Unhandled event type in notification handler", mapOf(
                    "handler" to "notification",
                    "event_type" to event.eventType
                ))
            }
        }
    }

    private suspend fun sendNotificationToStakeholders(subject: String, message: String) {
        // Mock implementation - in real system this would send emails/SMS
        log.info("Mock notification sent to stakeholders", mapOf(
            "handler" to "notification",
            "recipient_type" to "stakeholders",
            "subject" to subject,
            "message_length" to message.length
        ))
    }

    private suspend fun sendNotificationToParticipants(subject: String, message: String) {
        // Mock implementation - in real system this would send emails/SMS
        log.info("Mock notification sent to participants", mapOf(
            "handler" to "notification",
            "recipient_type" to "participants",
            "subject" to subject,
            "message_length" to message.length
        ))
    }
}

/**
 * Handler for tournament statistics and analytics
 */
class TurnierAnalyticsHandler : EventHandler<DomainEvent> {
    private val log = StructuredLogger.getLogger(TurnierAnalyticsHandler::class.java)

    override suspend fun handle(event: DomainEvent) {
        when (event) {
            is TurnierCreatedEvent -> {
                log.logEvent("tournament_analytics_recorded", "Recording tournament creation metrics", mapOf(
                    "handler" to "analytics",
                    "metric_type" to "tournament_created",
                    "turnier_id" to event.turnier.id.toString(),
                    "veranstaltung_id" to event.turnier.veranstaltungId.toString(),
                    "tournament_type" to "standard"
                ))
                recordMetric("tournament.created", 1, mapOf(
                    "veranstaltung_id" to event.turnier.veranstaltungId.toString(),
                    "tournament_type" to "standard"
                ))
            }
            is TurnierRegistrationClosedEvent -> {
                log.logEvent("tournament_analytics_recorded", "Recording registration metrics", mapOf(
                    "handler" to "analytics",
                    "metric_type" to "tournament_registrations",
                    "turnier_id" to event.turnierId.toString(),
                    "total_registrations" to event.totalRegistrations
                ))
                recordMetric("tournament.registrations", event.totalRegistrations, mapOf(
                    "tournament_id" to event.turnierId.toString()
                ))
            }
            is TurnierStatusChangedEvent -> {
                log.logEvent("tournament_analytics_recorded", "Recording status change metrics", mapOf(
                    "handler" to "analytics",
                    "metric_type" to "tournament_status_change",
                    "turnier_id" to event.turnierId.toString(),
                    "from_status" to event.previousStatus,
                    "to_status" to event.newStatus
                ))
                recordMetric("tournament.status_change", 1, mapOf(
                    "tournament_id" to event.turnierId.toString(),
                    "from_status" to event.previousStatus,
                    "to_status" to event.newStatus
                ))
            }
            else -> {
                log.debug("Unhandled event type in analytics handler", mapOf(
                    "handler" to "analytics",
                    "event_type" to event.eventType
                ))
            }
        }
    }

    private suspend fun recordMetric(metricName: String, value: Int, tags: Map<String, String>) {
        // Mock implementation - in real system this would send to analytics service
        log.info("Mock analytics metric recorded", mapOf(
            "handler" to "analytics",
            "metric_name" to metricName,
            "metric_value" to value,
            "tags" to tags.toString()
        ))
    }
}

/**
 * Handler for tournament cache invalidation
 */
class TurnierCacheHandler : EventHandler<DomainEvent> {
    private val log = StructuredLogger.getLogger(TurnierCacheHandler::class.java)

    override suspend fun handle(event: DomainEvent) {
        when (event) {
            is TurnierCreatedEvent -> {
                log.logEvent("tournament_cache_invalidated", "Cache invalidated for tournament creation", mapOf(
                    "handler" to "cache",
                    "event_type" to "tournament_created",
                    "turnier_id" to event.turnier.id.toString(),
                    "veranstaltung_id" to event.turnier.veranstaltungId.toString()
                ))
                invalidateCache("tournaments:all")
                invalidateCache("tournaments:veranstaltung:${event.turnier.veranstaltungId}")
            }
            is TurnierUpdatedEvent -> {
                log.logEvent("tournament_cache_invalidated", "Cache invalidated for tournament update", mapOf(
                    "handler" to "cache",
                    "event_type" to "tournament_updated",
                    "turnier_id" to event.updatedTurnier.id.toString(),
                    "veranstaltung_id" to event.updatedTurnier.veranstaltungId.toString()
                ))
                invalidateCache("tournaments:all")
                invalidateCache("tournaments:${event.updatedTurnier.id}")
                invalidateCache("tournaments:veranstaltung:${event.updatedTurnier.veranstaltungId}")
            }
            is TurnierDeletedEvent -> {
                log.logEvent("tournament_cache_invalidated", "Cache invalidated for tournament deletion", mapOf(
                    "handler" to "cache",
                    "event_type" to "tournament_deleted",
                    "turnier_id" to event.deletedTurnier.id.toString(),
                    "veranstaltung_id" to event.deletedTurnier.veranstaltungId.toString()
                ))
                invalidateCache("tournaments:all")
                invalidateCache("tournaments:${event.deletedTurnier.id}")
                invalidateCache("tournaments:veranstaltung:${event.deletedTurnier.veranstaltungId}")
            }
            else -> {
                log.debug("Unhandled event type in cache handler", mapOf(
                    "handler" to "cache",
                    "event_type" to event.eventType
                ))
            }
        }
    }

    private suspend fun invalidateCache(cacheKey: String) {
        // Mock implementation - in real system this would invalidate Redis/other cache
        log.info("Mock cache invalidation", mapOf(
            "handler" to "cache",
            "cache_key" to cacheKey,
            "operation" to "invalidate"
        ))
    }
}

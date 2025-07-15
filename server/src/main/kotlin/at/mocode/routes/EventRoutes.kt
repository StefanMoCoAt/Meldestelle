package at.mocode.routes

import at.mocode.events.EventPublisher
import at.mocode.utils.ApiResponse
import com.benasher44.uuid.uuidFrom
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Routes for accessing domain events
 */
fun Route.eventRoutes() {
    val eventPublisher = EventPublisher.getInstance()

    route("/events") {
        // GET /api/events - Get all events
        get {
            try {
                val events = eventPublisher.getAllEvents()
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(
                        success = true,
                        data = events,
                        message = "Retrieved ${events.size} events"
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse<Nothing>(
                        success = false,
                        error = "INTERNAL_ERROR",
                        message = "Failed to retrieve events: ${e.message}"
                    )
                )
            }
        }

        // GET /api/events/aggregate/{aggregateId} - Get events by aggregate ID
        get("/aggregate/{aggregateId}") {
            try {
                val aggregateIdParam = call.parameters["aggregateId"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Nothing>(
                        success = false,
                        error = "MISSING_PARAMETER",
                        message = "Missing aggregate ID parameter"
                    )
                )

                val aggregateId = uuidFrom(aggregateIdParam)
                val events = eventPublisher.getEventsByAggregateId(aggregateId)

                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(
                        success = true,
                        data = events,
                        message = "Retrieved ${events.size} events for aggregate $aggregateId"
                    )
                )
            } catch (_: IllegalArgumentException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Nothing>(
                        success = false,
                        error = "INVALID_UUID",
                        message = "Invalid UUID format for aggregate ID"
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse<Nothing>(
                        success = false,
                        error = "INTERNAL_ERROR",
                        message = "Failed to retrieve events: ${e.message}"
                    )
                )
            }
        }

        // GET /api/events/type/{eventType} - Get events by type
        get("/type/{eventType}") {
            try {
                val eventType = call.parameters["eventType"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<Nothing>(
                        success = false,
                        error = "MISSING_PARAMETER",
                        message = "Missing event type parameter"
                    )
                )

                val events = eventPublisher.getEventsByType(eventType)

                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(
                        success = true,
                        data = events,
                        message = "Retrieved ${events.size} events of type $eventType"
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse<Nothing>(
                        success = false,
                        error = "INTERNAL_ERROR",
                        message = "Failed to retrieve events: ${e.message}"
                    )
                )
            }
        }

        // GET /api/events/stats - Get event statistics
        get("/stats") {
            try {
                val allEvents = eventPublisher.getAllEvents()
                val eventsByType = allEvents.groupBy { it.eventType }
                val stats = mapOf(
                    "totalEvents" to allEvents.size,
                    "eventsByType" to eventsByType.mapValues { it.value.size },
                    "uniqueAggregates" to allEvents.map { it.aggregateId }.distinct().size
                )

                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(
                        success = true,
                        data = stats,
                        message = "Event statistics retrieved successfully"
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiResponse<Nothing>(
                        success = false,
                        error = "INTERNAL_ERROR",
                        message = "Failed to retrieve event statistics: ${e.message}"
                    )
                )
            }
        }
    }
}

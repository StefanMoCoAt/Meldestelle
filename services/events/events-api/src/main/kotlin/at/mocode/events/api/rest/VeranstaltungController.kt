package at.mocode.events.api.rest

import at.mocode.core.domain.model.ApiResponse
import at.mocode.core.domain.model.SparteE
import at.mocode.events.application.usecase.CreateVeranstaltungUseCase
import at.mocode.events.application.usecase.DeleteVeranstaltungUseCase
import at.mocode.events.application.usecase.GetVeranstaltungUseCase
import at.mocode.events.application.usecase.UpdateVeranstaltungUseCase
import at.mocode.events.domain.repository.VeranstaltungRepository
import at.mocode.core.domain.serialization.UuidSerializer
import at.mocode.core.utils.validation.ApiValidationUtils
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/**
 * REST API controller for event management operations.
 *
 * This controller provides HTTP endpoints for all event-related operations
 * following REST conventions and proper HTTP status codes.
 */
class VeranstaltungController(
    private val veranstaltungRepository: VeranstaltungRepository
) {

    private val createVeranstaltungUseCase = CreateVeranstaltungUseCase(veranstaltungRepository)
    private val getVeranstaltungUseCase = GetVeranstaltungUseCase(veranstaltungRepository)
    private val updateVeranstaltungUseCase = UpdateVeranstaltungUseCase(veranstaltungRepository)
    private val deleteVeranstaltungUseCase = DeleteVeranstaltungUseCase(veranstaltungRepository)

    /**
     * Configures the event-related routes.
     */
    fun configureRoutes(routing: Routing) {
        routing.route("/api/events") {

            // GET /api/events - Get all events with optional filtering
            get {
                try {
                    // Validate query parameters
                    val validationErrors = ApiValidationUtils.validateQueryParameters(
                        limit = call.request.queryParameters["limit"],
                        offset = call.request.queryParameters["offset"],
                        startDate = call.request.queryParameters["startDate"],
                        endDate = call.request.queryParameters["endDate"],
                        search = call.request.queryParameters["search"]
                    )

                    if (!ApiValidationUtils.isValid(validationErrors)) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponse.error<Any>(ApiValidationUtils.createErrorMessage(validationErrors))
                        )
                        return@get
                    }

                    val activeOnly = call.request.queryParameters["activeOnly"]?.toBoolean() ?: true
                    val limit = call.request.queryParameters["limit"]?.toInt() ?: 100
                    val offset = call.request.queryParameters["offset"]?.toInt() ?: 0
                    val organizerId = call.request.queryParameters["organizerId"]?.let {
                        ApiValidationUtils.validateUuidString(it) ?: return@get call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponse.error<Any>("Invalid organizerId format")
                        )
                    }
                    val searchTerm = call.request.queryParameters["search"]
                    val publicOnly = call.request.queryParameters["publicOnly"]?.toBoolean() ?: false
                    val startDate = call.request.queryParameters["startDate"]?.let { LocalDate.parse(it) }
                    val endDate = call.request.queryParameters["endDate"]?.let { LocalDate.parse(it) }

                    val events = when {
                        searchTerm != null -> veranstaltungRepository.findByName(searchTerm, limit)
                        organizerId != null -> veranstaltungRepository.findByVeranstalterVereinId(organizerId, activeOnly)
                        publicOnly -> veranstaltungRepository.findPublicEvents(activeOnly)
                        startDate != null && endDate != null -> veranstaltungRepository.findByDateRange(startDate, endDate, activeOnly)
                        startDate != null -> veranstaltungRepository.findByStartDate(startDate, activeOnly)
                        else -> veranstaltungRepository.findAllActive(limit, offset)
                    }

                    call.respond(HttpStatusCode.OK, ApiResponse.success(events))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse.error<Any>("Failed to retrieve events: ${e.message}"))
                }
            }

            // GET /api/events/{id} - Get event by ID
            get("/{id}") {
                try {
                    val eventId = uuidFrom(call.parameters["id"]!!)
                    val request = GetVeranstaltungUseCase.GetVeranstaltungRequest(eventId)
                    val response = getVeranstaltungUseCase.execute(request)

                    if (response.success && response.data != null) {
                        call.respond(HttpStatusCode.OK, ApiResponse.success((response.data as GetVeranstaltungUseCase.GetVeranstaltungResponse).veranstaltung))
                    } else {
                        call.respond(HttpStatusCode.NotFound, ApiResponse.error<Any>("Event not found"))
                    }
                } catch (_: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, ApiResponse.error<Any>("Invalid event ID format"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse.error<Any>("Failed to retrieve event: ${e.message}"))
                }
            }

            // GET /api/events/stats - Get event statistics
            get("/stats") {
                try {
                    val activeCount = veranstaltungRepository.countActive()
                    val publicCount = veranstaltungRepository.findPublicEvents(true).size

                    val stats = EventStats(
                        totalActive = activeCount,
                        totalPublic = publicCount.toLong()
                    )

                    call.respond(HttpStatusCode.OK, ApiResponse.success(stats))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse.error<Any>("Failed to retrieve event statistics: ${e.message}"))
                }
            }

            // POST /api/events - Create new event
            post {
                try {
                    val createRequest = call.receive<CreateEventRequest>()

                    // Validate input using shared validation utilities
                    val validationErrors = ApiValidationUtils.validateEventRequest(
                        name = createRequest.name,
                        ort = createRequest.ort,
                        startDatum = createRequest.startDatum,
                        endDatum = createRequest.endDatum,
                        maxTeilnehmer = createRequest.maxTeilnehmer
                    )

                    if (!ApiValidationUtils.isValid(validationErrors)) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponse.error<Any>(ApiValidationUtils.createErrorMessage(validationErrors))
                        )
                        return@post
                    }

                    val useCaseRequest = CreateVeranstaltungUseCase.CreateVeranstaltungRequest(
                        name = createRequest.name,
                        beschreibung = createRequest.beschreibung,
                        startDatum = createRequest.startDatum,
                        endDatum = createRequest.endDatum,
                        ort = createRequest.ort,
                        veranstalterVereinId = createRequest.veranstalterVereinId,
                        sparten = createRequest.sparten,
                        istAktiv = createRequest.istAktiv,
                        istOeffentlich = createRequest.istOeffentlich,
                        maxTeilnehmer = createRequest.maxTeilnehmer,
                        anmeldeschluss = createRequest.anmeldeschluss
                    )

                    val response = createVeranstaltungUseCase.execute(useCaseRequest)

                    if (response.success && response.data != null) {
                        call.respond(HttpStatusCode.Created, ApiResponse.success((response.data as CreateVeranstaltungUseCase.CreateVeranstaltungResponse).veranstaltung))
                    } else {
                        val statusCode = when (response.error?.code) {
                            "VALIDATION_ERROR" -> HttpStatusCode.BadRequest
                            "DOMAIN_VALIDATION_ERROR" -> HttpStatusCode.BadRequest
                            else -> HttpStatusCode.InternalServerError
                        }
                        call.respond(statusCode, ApiResponse.error<Any>(response.error?.message ?: "Failed to create event"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, ApiResponse.error<Any>("Invalid request data: ${e.message}"))
                }
            }

            // PUT /api/events/{id} - Update event
            put("/{id}") {
                try {
                    val eventId = uuidFrom(call.parameters["id"]!!)
                    val updateRequest = call.receive<UpdateEventRequest>()

                    // Validate input using shared validation utilities
                    val validationErrors = ApiValidationUtils.validateEventRequest(
                        name = updateRequest.name,
                        ort = updateRequest.ort,
                        startDatum = updateRequest.startDatum,
                        endDatum = updateRequest.endDatum,
                        maxTeilnehmer = updateRequest.maxTeilnehmer
                    )

                    if (!ApiValidationUtils.isValid(validationErrors)) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponse.error<Any>(ApiValidationUtils.createErrorMessage(validationErrors))
                        )
                        return@put
                    }

                    val useCaseRequest = UpdateVeranstaltungUseCase.UpdateVeranstaltungRequest(
                        veranstaltungId = eventId,
                        name = updateRequest.name,
                        beschreibung = updateRequest.beschreibung,
                        startDatum = updateRequest.startDatum,
                        endDatum = updateRequest.endDatum,
                        ort = updateRequest.ort,
                        veranstalterVereinId = updateRequest.veranstalterVereinId,
                        sparten = updateRequest.sparten,
                        istAktiv = updateRequest.istAktiv,
                        istOeffentlich = updateRequest.istOeffentlich,
                        maxTeilnehmer = updateRequest.maxTeilnehmer,
                        anmeldeschluss = updateRequest.anmeldeschluss
                    )

                    val response = updateVeranstaltungUseCase.execute(useCaseRequest)

                    if (response.success && response.data != null) {
                        call.respond(HttpStatusCode.OK, ApiResponse.success((response.data as UpdateVeranstaltungUseCase.UpdateVeranstaltungResponse).veranstaltung))
                    } else {
                        val statusCode = when (response.error?.code) {
                            "NOT_FOUND" -> HttpStatusCode.NotFound
                            "VALIDATION_ERROR" -> HttpStatusCode.BadRequest
                            "DOMAIN_VALIDATION_ERROR" -> HttpStatusCode.BadRequest
                            else -> HttpStatusCode.InternalServerError
                        }
                        call.respond(statusCode, ApiResponse.error<Any>(response.error?.message ?: "Failed to update event"))
                    }
                } catch (_: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, ApiResponse.error<Any>("Invalid event ID format"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, ApiResponse.error<Any>("Invalid request data: ${e.message}"))
                }
            }

            // DELETE /api/events/{id} - Delete event
            delete("/{id}") {
                try {
                    val eventId = ApiValidationUtils.validateUuidString(call.parameters["id"])
                        ?: return@delete call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponse.error<Any>("Invalid event ID format")
                        )

                    // Validate force parameter if provided
                    val forceParam = call.request.queryParameters["force"]
                    val forceDelete = if (forceParam != null) {
                        try {
                            forceParam.toBoolean()
                        } catch (_: Exception) {
                            return@delete call.respond(
                                HttpStatusCode.BadRequest,
                                ApiResponse.error<Any>("Invalid force parameter. Must be true or false")
                            )
                        }
                    } else {
                        false
                    }
                    val useCaseRequest = DeleteVeranstaltungUseCase.DeleteVeranstaltungRequest(
                        veranstaltungId = eventId,
                        forceDelete = forceDelete
                    )

                    val response = deleteVeranstaltungUseCase.execute(useCaseRequest)

                    if (response.success) {
                        call.respond(HttpStatusCode.OK, ApiResponse.success(response.data))
                    } else {
                        val statusCode = when (response.error?.code) {
                            "NOT_FOUND" -> HttpStatusCode.NotFound
                            "CANNOT_DELETE_ACTIVE_EVENT" -> HttpStatusCode.Conflict
                            else -> HttpStatusCode.InternalServerError
                        }
                        call.respond(statusCode, ApiResponse.error<Any>(response.error?.message ?: "Failed to delete event"))
                    }
                } catch (_: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, ApiResponse.error<Any>("Invalid event ID format"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse.error<Any>("Failed to delete event: ${e.message}"))
                }
            }
        }
    }

    /**
     * Request DTO for creating events.
     */
    @Serializable
    data class CreateEventRequest(
        val name: String,
        val beschreibung: String? = null,
        val startDatum: LocalDate,
        val endDatum: LocalDate,
        val ort: String,
        @Serializable(with = UuidSerializer::class)
        val veranstalterVereinId: Uuid,
        val sparten: List<SparteE> = emptyList(),
        val istAktiv: Boolean = true,
        val istOeffentlich: Boolean = true,
        val maxTeilnehmer: Int? = null,
        val anmeldeschluss: LocalDate? = null
    )

    /**
     * Request DTO for updating events.
     */
    @Serializable
    data class UpdateEventRequest(
        val name: String,
        val beschreibung: String? = null,
        val startDatum: LocalDate,
        val endDatum: LocalDate,
        val ort: String,
        @Serializable(with = UuidSerializer::class)
        val veranstalterVereinId: Uuid,
        val sparten: List<SparteE> = emptyList(),
        val istAktiv: Boolean = true,
        val istOeffentlich: Boolean = true,
        val maxTeilnehmer: Int? = null,
        val anmeldeschluss: LocalDate? = null
    )

    /**
     * Response DTO for event statistics.
     */
    @Serializable
    data class EventStats(
        val totalActive: Long,
        val totalPublic: Long
    )
}

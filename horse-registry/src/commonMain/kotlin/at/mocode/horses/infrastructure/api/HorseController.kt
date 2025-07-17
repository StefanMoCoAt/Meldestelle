package at.mocode.horses.infrastructure.api

import at.mocode.horses.application.usecase.*
import at.mocode.horses.domain.repository.HorseRepository
import at.mocode.dto.base.BaseDto
import at.mocode.enums.PferdeGeschlechtE
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

/**
 * REST API controller for horse registry operations.
 *
 * This controller provides HTTP endpoints for all horse-related operations
 * following REST conventions and proper HTTP status codes.
 */
class HorseController(
    private val horseRepository: HorseRepository
) {

    private val getHorseUseCase = GetHorseUseCase(horseRepository)
    private val createHorseUseCase = CreateHorseUseCase(horseRepository)
    private val updateHorseUseCase = UpdateHorseUseCase(horseRepository)
    private val deleteHorseUseCase = DeleteHorseUseCase(horseRepository)

    /**
     * Configures the horse-related routes.
     */
    fun configureRoutes(routing: Routing) {
        routing.route("/api/horses") {

            // GET /api/horses - Get all horses with optional filtering
            get {
                try {
                    val activeOnly = call.request.queryParameters["activeOnly"]?.toBoolean() ?: true
                    val limit = call.request.queryParameters["limit"]?.toInt() ?: 100
                    val ownerId = call.request.queryParameters["ownerId"]?.let { uuidFrom(it) }
                    val geschlecht = call.request.queryParameters["geschlecht"]?.let {
                        PferdeGeschlechtE.valueOf(it)
                    }
                    val rasse = call.request.queryParameters["rasse"]
                    val searchTerm = call.request.queryParameters["search"]

                    val horses = when {
                        searchTerm != null -> horseRepository.findByName(searchTerm, limit)
                        ownerId != null -> horseRepository.findByOwnerId(ownerId, activeOnly)
                        geschlecht != null -> horseRepository.findByGeschlecht(geschlecht, activeOnly, limit)
                        rasse != null -> horseRepository.findByRasse(rasse, activeOnly, limit)
                        else -> horseRepository.findAllActive(limit)
                    }

                    call.respond(HttpStatusCode.OK, BaseDto.success(horses))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, BaseDto.error<Any>("Failed to retrieve horses: ${e.message}"))
                }
            }

            // GET /api/horses/{id} - Get horse by ID
            get("/{id}") {
                try {
                    val horseId = uuidFrom(call.parameters["id"]!!)
                    val request = GetHorseUseCase.GetHorseRequest(horseId)
                    val response = getHorseUseCase.execute(request)

                    if (response.success && response.horse != null) {
                        call.respond(HttpStatusCode.OK, BaseDto.success(response.horse))
                    } else {
                        call.respond(HttpStatusCode.NotFound, BaseDto.error<Any>("Horse not found"))
                    }
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, BaseDto.error<Any>("Invalid horse ID format"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, BaseDto.error<Any>("Failed to retrieve horse: ${e.message}"))
                }
            }

            // GET /api/horses/search/lebensnummer/{nummer} - Find by life number
            get("/search/lebensnummer/{nummer}") {
                try {
                    val lebensnummer = call.parameters["nummer"]!!
                    val horse = horseRepository.findByLebensnummer(lebensnummer)

                    if (horse != null) {
                        call.respond(HttpStatusCode.OK, BaseDto.success(horse))
                    } else {
                        call.respond(HttpStatusCode.NotFound, BaseDto.error<Any>("Horse with life number '$lebensnummer' not found"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, BaseDto.error<Any>("Failed to search horse: ${e.message}"))
                }
            }

            // GET /api/horses/search/chip/{nummer} - Find by chip number
            get("/search/chip/{nummer}") {
                try {
                    val chipNummer = call.parameters["nummer"]!!
                    val horse = horseRepository.findByChipNummer(chipNummer)

                    if (horse != null) {
                        call.respond(HttpStatusCode.OK, BaseDto.success(horse))
                    } else {
                        call.respond(HttpStatusCode.NotFound, BaseDto.error<Any>("Horse with chip number '$chipNummer' not found"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, BaseDto.error<Any>("Failed to search horse: ${e.message}"))
                }
            }

            // GET /api/horses/oeps-registered - Get OEPS registered horses
            get("/oeps-registered") {
                try {
                    val activeOnly = call.request.queryParameters["activeOnly"]?.toBoolean() ?: true
                    val horses = horseRepository.findOepsRegistered(activeOnly)
                    call.respond(HttpStatusCode.OK, BaseDto.success(horses))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, BaseDto.error<Any>("Failed to retrieve OEPS horses: ${e.message}"))
                }
            }

            // GET /api/horses/fei-registered - Get FEI registered horses
            get("/fei-registered") {
                try {
                    val activeOnly = call.request.queryParameters["activeOnly"]?.toBoolean() ?: true
                    val horses = horseRepository.findFeiRegistered(activeOnly)
                    call.respond(HttpStatusCode.OK, BaseDto.success(horses))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, BaseDto.error<Any>("Failed to retrieve FEI horses: ${e.message}"))
                }
            }

            // GET /api/horses/stats - Get horse statistics
            get("/stats") {
                try {
                    val activeCount = horseRepository.countActive()
                    val oepsCount = horseRepository.findOepsRegistered(true).size
                    val feiCount = horseRepository.findFeiRegistered(true).size

                    val stats = HorseStats(
                        totalActive = activeCount,
                        oepsRegistered = oepsCount.toLong(),
                        feiRegistered = feiCount.toLong()
                    )

                    call.respond(HttpStatusCode.OK, BaseDto.success(stats))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, BaseDto.error<Any>("Failed to retrieve statistics: ${e.message}"))
                }
            }

            // POST /api/horses - Create new horse
            post {
                try {
                    val createRequest = call.receive<CreateHorseUseCase.CreateHorseRequest>()
                    val response = createHorseUseCase.execute(createRequest)

                    if (response.success) {
                        call.respond(HttpStatusCode.Created, BaseDto.success(response.horse))
                    } else {
                        call.respond(HttpStatusCode.BadRequest, BaseDto.error<Any>("Validation failed", response.errors))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, BaseDto.error<Any>("Failed to create horse: ${e.message}"))
                }
            }

            // PUT /api/horses/{id} - Update horse
            put("/{id}") {
                try {
                    val horseId = uuidFrom(call.parameters["id"]!!)
                    val updateData = call.receive<UpdateHorseRequest>()

                    val updateRequest = UpdateHorseUseCase.UpdateHorseRequest(
                        pferdId = horseId,
                        pferdeName = updateData.pferdeName,
                        geschlecht = updateData.geschlecht,
                        geburtsdatum = updateData.geburtsdatum,
                        rasse = updateData.rasse,
                        farbe = updateData.farbe,
                        besitzerId = updateData.besitzerId,
                        verantwortlichePersonId = updateData.verantwortlichePersonId,
                        zuechterName = updateData.zuechterName,
                        zuchtbuchNummer = updateData.zuchtbuchNummer,
                        lebensnummer = updateData.lebensnummer,
                        chipNummer = updateData.chipNummer,
                        passNummer = updateData.passNummer,
                        oepsNummer = updateData.oepsNummer,
                        feiNummer = updateData.feiNummer,
                        vaterName = updateData.vaterName,
                        mutterName = updateData.mutterName,
                        mutterVaterName = updateData.mutterVaterName,
                        stockmass = updateData.stockmass,
                        istAktiv = updateData.istAktiv,
                        bemerkungen = updateData.bemerkungen,
                        datenQuelle = updateData.datenQuelle
                    )

                    val response = updateHorseUseCase.execute(updateRequest)

                    if (response.success && response.horse != null) {
                        call.respond(HttpStatusCode.OK, BaseDto.success(response.horse))
                    } else {
                        call.respond(HttpStatusCode.BadRequest, BaseDto.error<Any>("Update failed", response.errors))
                    }
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, BaseDto.error<Any>("Invalid horse ID format"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, BaseDto.error<Any>("Failed to update horse: ${e.message}"))
                }
            }

            // DELETE /api/horses/{id} - Delete horse
            delete("/{id}") {
                try {
                    val horseId = uuidFrom(call.parameters["id"]!!)
                    val forceDelete = call.request.queryParameters["force"]?.toBoolean() ?: false

                    val deleteRequest = DeleteHorseUseCase.DeleteHorseRequest(horseId, forceDelete)
                    val response = deleteHorseUseCase.execute(deleteRequest)

                    if (response.success) {
                        call.respond(HttpStatusCode.OK, BaseDto.success("Horse deleted successfully", response.warnings))
                    } else {
                        call.respond(HttpStatusCode.BadRequest, BaseDto.error<Any>("Delete failed", response.errors))
                    }
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, BaseDto.error<Any>("Invalid horse ID format"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, BaseDto.error<Any>("Failed to delete horse: ${e.message}"))
                }
            }

            // POST /api/horses/{id}/soft-delete - Soft delete horse (mark as inactive)
            post("/{id}/soft-delete") {
                try {
                    val horseId = uuidFrom(call.parameters["id"]!!)
                    val response = deleteHorseUseCase.softDelete(horseId)

                    if (response.success) {
                        call.respond(HttpStatusCode.OK, BaseDto.success("Horse marked as inactive", response.warnings))
                    } else {
                        call.respond(HttpStatusCode.BadRequest, BaseDto.error<Any>("Soft delete failed", response.errors))
                    }
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, BaseDto.error<Any>("Invalid horse ID format"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, BaseDto.error<Any>("Failed to soft delete horse: ${e.message}"))
                }
            }

            // POST /api/horses/batch-delete - Batch delete multiple horses
            post("/batch-delete") {
                try {
                    val batchRequest = call.receive<BatchDeleteRequest>()
                    val response = deleteHorseUseCase.batchDelete(batchRequest.horseIds, batchRequest.forceDelete)

                    val statusCode = if (response.overallSuccess) HttpStatusCode.OK else HttpStatusCode.PartialContent
                    call.respond(statusCode, BaseDto.success(response))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, BaseDto.error<Any>("Failed to batch delete horses: ${e.message}"))
                }
            }
        }
    }

    /**
     * DTO for updating horse data via API.
     */
    @Serializable
    data class UpdateHorseRequest(
        val pferdeName: String,
        val geschlecht: PferdeGeschlechtE,
        val geburtsdatum: kotlinx.datetime.LocalDate? = null,
        val rasse: String? = null,
        val farbe: String? = null,
        val besitzerId: Uuid? = null,
        val verantwortlichePersonId: Uuid? = null,
        val zuechterName: String? = null,
        val zuchtbuchNummer: String? = null,
        val lebensnummer: String? = null,
        val chipNummer: String? = null,
        val passNummer: String? = null,
        val oepsNummer: String? = null,
        val feiNummer: String? = null,
        val vaterName: String? = null,
        val mutterName: String? = null,
        val mutterVaterName: String? = null,
        val stockmass: Int? = null,
        val istAktiv: Boolean = true,
        val bemerkungen: String? = null,
        val datenQuelle: at.mocode.enums.DatenQuelleE = at.mocode.enums.DatenQuelleE.MANUAL
    )

    /**
     * DTO for batch delete request.
     */
    @Serializable
    data class BatchDeleteRequest(
        val horseIds: List<Uuid>,
        val forceDelete: Boolean = false
    )

    /**
     * DTO for horse statistics.
     */
    @Serializable
    data class HorseStats(
        val totalActive: Long,
        val oepsRegistered: Long,
        val feiRegistered: Long
    )
}

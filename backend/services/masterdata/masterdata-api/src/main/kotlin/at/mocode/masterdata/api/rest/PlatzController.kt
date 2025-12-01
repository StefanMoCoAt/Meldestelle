@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)
package at.mocode.masterdata.api.rest

import at.mocode.core.domain.model.ApiResponse
import at.mocode.core.domain.model.PlatzTypE
import at.mocode.masterdata.application.usecase.CreatePlatzUseCase
import at.mocode.masterdata.application.usecase.GetPlatzUseCase
import at.mocode.masterdata.domain.model.Platz
import at.mocode.core.utils.validation.ApiValidationUtils
import kotlin.uuid.Uuid
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

/**
 * REST API controller for venue/arena management operations.
 *
 * This controller provides HTTP endpoints for the master-data context's
 * venue functionality, following REST conventions and proper error handling.
 */
class PlatzController(
    private val getPlatzUseCase: GetPlatzUseCase,
    private val createPlatzUseCase: CreatePlatzUseCase
) {

    /**
     * DTO for venue API responses.
     */
    @Serializable
    data class PlatzDto(
        val id: String,
        val turnierId: String,
        val name: String,
        val dimension: String? = null,
        val boden: String? = null,
        val typ: String,
        val istAktiv: Boolean = true,
        val sortierReihenfolge: Int? = null,
        val createdAt: String,
        val updatedAt: String
    )

    /**
     * DTO for creating a new venue.
     */
    @Serializable
    data class CreatePlatzDto(
        val turnierId: String,
        val name: String,
        val dimension: String? = null,
        val boden: String? = null,
        val typ: String,
        val istAktiv: Boolean = true,
        val sortierReihenfolge: Int? = null
    )

    /**
     * DTO for updating an existing venue.
     */
    @Serializable
    data class UpdatePlatzDto(
        val turnierId: String,
        val name: String,
        val dimension: String? = null,
        val boden: String? = null,
        val typ: String,
        val istAktiv: Boolean = true,
        val sortierReihenfolge: Int? = null
    )

    /**
     * Configures the routing for venue endpoints.
     */
    fun configureRouting(routing: Routing) {
        routing.route("/api/masterdata/plaetze") {

            // GET /api/masterdata/plaetze/{id} - Get venue by ID
            get("/{id}") {
                try {
                    val platzId = call.parameters["id"]?.let { Uuid.parse(it) }
                        ?: return@get call.respond(HttpStatusCode.BadRequest, ApiResponse.error<PlatzDto>("Invalid venue ID"))

                    val platz = getPlatzUseCase.getById(platzId)
                    if (platz != null) {
                        call.respond(HttpStatusCode.OK, ApiResponse.success(platz.toDto()))
                    } else {
                        call.respond(HttpStatusCode.NotFound, ApiResponse.error<PlatzDto>("Venue not found"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse.error<PlatzDto>("Failed to retrieve venue: ${e.message}"))
                }
            }

            // GET /api/masterdata/plaetze/tournament/{turnierId} - Get venues by tournament
            get("/tournament/{turnierId}") {
                try {
                    val turnierId = call.parameters["turnierId"]?.let { Uuid.parse(it) }
                        ?: return@get call.respond(HttpStatusCode.BadRequest, ApiResponse.error<List<PlatzDto>>("Invalid tournament ID"))

                    val activeOnlyParam = call.request.queryParameters["activeOnly"]
                    val activeOnly = activeOnlyParam?.toBoolean() ?: true

                    val orderBySortierungParam = call.request.queryParameters["orderBySortierung"]
                    val orderBySortierung = orderBySortierungParam?.toBoolean() ?: true

                    val plaetze = getPlatzUseCase.getByTournament(turnierId, activeOnly, orderBySortierung)
                    val platzDtos = plaetze.map { it.toDto() }
                    call.respond(HttpStatusCode.OK, ApiResponse.success(platzDtos))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse.error<List<PlatzDto>>("Failed to retrieve venues: ${e.message}"))
                }
            }

            // GET /api/masterdata/plaetze/search - Search venues by name
            get("/search") {
                try {
                    val validationErrors = ApiValidationUtils.validateQueryParameters(
                        limit = call.request.queryParameters["limit"],
                        q = call.request.queryParameters["q"]
                    )

                    if (!ApiValidationUtils.isValid(validationErrors)) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponse.error<List<PlatzDto>>(ApiValidationUtils.createErrorMessage(validationErrors))
                        )
                        return@get
                    }

                    val searchTerm = call.request.queryParameters["q"]
                        ?: return@get call.respond(HttpStatusCode.BadRequest, ApiResponse.error<List<PlatzDto>>("Search term 'q' is required"))

                    val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 50
                    val turnierIdParam = call.request.queryParameters["turnierId"]
                    val turnierId = turnierIdParam?.let { uuidFrom(it) }

                    val plaetze = getPlatzUseCase.searchByName(searchTerm, turnierId, limit)
                    val platzDtos = plaetze.map { it.toDto() }
                    call.respond(HttpStatusCode.OK, ApiResponse.success(platzDtos))
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, ApiResponse.error<List<PlatzDto>>(e.message ?: "Invalid search parameters"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse.error<List<PlatzDto>>("Failed to search venues: ${e.message}"))
                }
            }

            // GET /api/masterdata/plaetze/type/{typ} - Get venues by type
            get("/type/{typ}") {
                try {
                    val typParam = call.parameters["typ"]
                        ?: return@get call.respond(HttpStatusCode.BadRequest, ApiResponse.error<List<PlatzDto>>("Venue type is required"))

                    val typ = try {
                        PlatzTypE.valueOf(typParam.uppercase())
                    } catch (_: Exception) {
                        return@get call.respond(HttpStatusCode.BadRequest, ApiResponse.error<List<PlatzDto>>("Invalid venue type: $typParam"))
                    }

                    val turnierIdParam = call.request.queryParameters["turnierId"]
                    val turnierId = turnierIdParam?.let { uuidFrom(it) }

                    val activeOnlyParam = call.request.queryParameters["activeOnly"]
                    val activeOnly = activeOnlyParam?.toBoolean() ?: true

                    val plaetze = getPlatzUseCase.getByType(typ, turnierId, activeOnly)
                    val platzDtos = plaetze.map { it.toDto() }
                    call.respond(HttpStatusCode.OK, ApiResponse.success(platzDtos))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse.error<List<PlatzDto>>("Failed to retrieve venues: ${e.message}"))
                }
            }

            // GET /api/masterdata/plaetze/ground/{boden} - Get venues by ground type
            get("/ground/{boden}") {
                try {
                    val boden = call.parameters["boden"]
                        ?: return@get call.respond(HttpStatusCode.BadRequest, ApiResponse.error<List<PlatzDto>>("Ground type is required"))

                    val turnierIdParam = call.request.queryParameters["turnierId"]
                    val turnierId = turnierIdParam?.let { uuidFrom(it) }

                    val activeOnlyParam = call.request.queryParameters["activeOnly"]
                    val activeOnly = activeOnlyParam?.toBoolean() ?: true

                    val plaetze = getPlatzUseCase.getByGroundType(boden, turnierId, activeOnly)
                    val platzDtos = plaetze.map { it.toDto() }
                    call.respond(HttpStatusCode.OK, ApiResponse.success(platzDtos))
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, ApiResponse.error<List<PlatzDto>>(e.message ?: "Invalid ground type"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse.error<List<PlatzDto>>("Failed to retrieve venues: ${e.message}"))
                }
            }

            // GET /api/masterdata/plaetze/dimension/{dimension} - Get venues by dimensions
            get("/dimension/{dimension}") {
                try {
                    val dimension = call.parameters["dimension"]
                        ?: return@get call.respond(HttpStatusCode.BadRequest, ApiResponse.error<List<PlatzDto>>("Dimension is required"))

                    val turnierIdParam = call.request.queryParameters["turnierId"]
                    val turnierId = turnierIdParam?.let { uuidFrom(it) }

                    val activeOnlyParam = call.request.queryParameters["activeOnly"]
                    val activeOnly = activeOnlyParam?.toBoolean() ?: true

                    val plaetze = getPlatzUseCase.getByDimensions(dimension, turnierId, activeOnly)
                    val platzDtos = plaetze.map { it.toDto() }
                    call.respond(HttpStatusCode.OK, ApiResponse.success(platzDtos))
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, ApiResponse.error<List<PlatzDto>>(e.message ?: "Invalid dimension"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse.error<List<PlatzDto>>("Failed to retrieve venues: ${e.message}"))
                }
            }

            // GET /api/masterdata/plaetze/suitable - Get venues suitable for discipline
            get("/suitable") {
                try {
                    val typParam = call.request.queryParameters["typ"]
                        ?: return@get call.respond(HttpStatusCode.BadRequest, ApiResponse.error<List<PlatzDto>>("Required venue type parameter is missing"))

                    val requiredType = try {
                        PlatzTypE.valueOf(typParam.uppercase())
                    } catch (_: Exception) {
                        return@get call.respond(HttpStatusCode.BadRequest, ApiResponse.error<List<PlatzDto>>("Invalid venue type: $typParam"))
                    }

                    val requiredDimensions = call.request.queryParameters["dimension"]
                    val turnierIdParam = call.request.queryParameters["turnierId"]
                    val turnierId = turnierIdParam?.let { uuidFrom(it) }

                    val plaetze = getPlatzUseCase.getSuitableForDiscipline(requiredType, requiredDimensions, turnierId)
                    val platzDtos = plaetze.map { it.toDto() }
                    call.respond(HttpStatusCode.OK, ApiResponse.success(platzDtos))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse.error<List<PlatzDto>>("Failed to retrieve suitable venues: ${e.message}"))
                }
            }

            // POST /api/masterdata/plaetze - Create new venue
            post {
                try {
                    val createDto = call.receive<CreatePlatzDto>()

                    // Basic validation
                    if (createDto.name.isBlank()) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponse.error<PlatzDto>("Name is required")
                        )
                        return@post
                    }

                    val turnierId = try {
                        uuidFrom(createDto.turnierId)
                    } catch (_: Exception) {
                        return@post call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponse.error<PlatzDto>("Invalid tournament ID format")
                        )
                    }

                    val typ = try {
                        PlatzTypE.valueOf(createDto.typ.uppercase())
                    } catch (_: Exception) {
                        return@post call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponse.error<PlatzDto>("Invalid venue type: ${createDto.typ}")
                        )
                    }

                    val request = CreatePlatzUseCase.CreatePlatzRequest(
                        turnierId = turnierId,
                        name = createDto.name,
                        dimension = createDto.dimension,
                        boden = createDto.boden,
                        typ = typ,
                        istAktiv = createDto.istAktiv,
                        sortierReihenfolge = createDto.sortierReihenfolge
                    )

                    val result = createPlatzUseCase.createPlatz(request)
                    if (result.success) {
                        call.respond(HttpStatusCode.Created, ApiResponse.success(result.platz!!.toDto()))
                    } else {
                        call.respond(HttpStatusCode.BadRequest, ApiResponse.error<PlatzDto>("Validation failed: ${result.errors.joinToString(", ")}"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse.error<PlatzDto>("Failed to create venue: ${e.message}"))
                }
            }

            // PUT /api/masterdata/plaetze/{id} - Update existing venue
            put("/{id}") {
                try {
                    val platzId = call.parameters["id"]?.let { uuidFrom(it) }
                        ?: return@put call.respond(HttpStatusCode.BadRequest, ApiResponse.error<PlatzDto>("Invalid venue ID"))

                    val updateDto = call.receive<UpdatePlatzDto>()

                    // Basic validation
                    if (updateDto.name.isBlank()) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponse.error<PlatzDto>("Name is required")
                        )
                        return@put
                    }

                    val turnierId = try {
                        uuidFrom(updateDto.turnierId)
                    } catch (_: Exception) {
                        return@put call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponse.error<PlatzDto>("Invalid tournament ID format")
                        )
                    }

                    val typ = try {
                        PlatzTypE.valueOf(updateDto.typ.uppercase())
                    } catch (_: Exception) {
                        return@put call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponse.error<PlatzDto>("Invalid venue type: ${updateDto.typ}")
                        )
                    }

                    val request = CreatePlatzUseCase.UpdatePlatzRequest(
                        platzId = platzId,
                        turnierId = turnierId,
                        name = updateDto.name,
                        dimension = updateDto.dimension,
                        boden = updateDto.boden,
                        typ = typ,
                        istAktiv = updateDto.istAktiv,
                        sortierReihenfolge = updateDto.sortierReihenfolge
                    )

                    val result = createPlatzUseCase.updatePlatz(request)
                    if (result.success) {
                        call.respond(HttpStatusCode.OK, ApiResponse.success(result.platz!!.toDto()))
                    } else {
                        call.respond(HttpStatusCode.BadRequest, ApiResponse.error<PlatzDto>("Validation failed: ${result.errors.joinToString(", ")}"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse.error<PlatzDto>("Failed to update venue: ${e.message}"))
                }
            }

            // DELETE /api/masterdata/plaetze/{id} - Delete venue
            delete("/{id}") {
                try {
                    val platzId = call.parameters["id"]?.let { uuidFrom(it) }
                        ?: return@delete call.respond(HttpStatusCode.BadRequest, ApiResponse.error<Unit>("Invalid venue ID"))

                    val result = createPlatzUseCase.deletePlatz(platzId)
                    if (result.success) {
                        call.respond(HttpStatusCode.NoContent)
                    } else {
                        call.respond(HttpStatusCode.NotFound, ApiResponse.error<Unit>("Venue not found: ${result.errors.joinToString(", ")}"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse.error<Unit>("Failed to delete venue: ${e.message}"))
                }
            }

            // GET /api/masterdata/plaetze/count/tournament/{turnierId} - Count venues by tournament
            get("/count/tournament/{turnierId}") {
                try {
                    val turnierId = call.parameters["turnierId"]?.let { uuidFrom(it) }
                        ?: return@get call.respond(HttpStatusCode.BadRequest, ApiResponse.error<Long>("Invalid tournament ID"))

                    val count = getPlatzUseCase.countActiveByTournament(turnierId)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(count))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse.error<Long>("Failed to count venues: ${e.message}"))
                }
            }

            // GET /api/masterdata/plaetze/count/type/{typ}/tournament/{turnierId} - Count venues by type and tournament
            get("/count/type/{typ}/tournament/{turnierId}") {
                try {
                    val typParam = call.parameters["typ"]
                        ?: return@get call.respond(HttpStatusCode.BadRequest, ApiResponse.error<Long>("Venue type is required"))

                    val typ = try {
                        PlatzTypE.valueOf(typParam.uppercase())
                    } catch (_: Exception) {
                        return@get call.respond(HttpStatusCode.BadRequest, ApiResponse.error<Long>("Invalid venue type: $typParam"))
                    }

                    val turnierId = call.parameters["turnierId"]?.let { uuidFrom(it) }
                        ?: return@get call.respond(HttpStatusCode.BadRequest, ApiResponse.error<Long>("Invalid tournament ID"))

                    val activeOnlyParam = call.request.queryParameters["activeOnly"]
                    val activeOnly = activeOnlyParam?.toBoolean() ?: true

                    val count = getPlatzUseCase.countByTypeAndTournament(typ, turnierId, activeOnly)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(count))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse.error<Long>("Failed to count venues: ${e.message}"))
                }
            }

            // GET /api/masterdata/plaetze/grouped/tournament/{turnierId} - Get venues grouped by type
            get("/grouped/tournament/{turnierId}") {
                try {
                    val turnierId = call.parameters["turnierId"]?.let { uuidFrom(it) }
                        ?: return@get call.respond(HttpStatusCode.BadRequest, ApiResponse.error<Map<String, List<PlatzDto>>>("Invalid tournament ID"))

                    val activeOnlyParam = call.request.queryParameters["activeOnly"]
                    val activeOnly = activeOnlyParam?.toBoolean() ?: true

                    val groupedVenues = getPlatzUseCase.getGroupedByTypeForTournament(turnierId, activeOnly)
                    val groupedDtos = groupedVenues.mapKeys { it.key.name }.mapValues { entry ->
                        entry.value.map { it.toDto() }
                    }
                    call.respond(HttpStatusCode.OK, ApiResponse.success(groupedDtos))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse.error<Map<String, List<PlatzDto>>>("Failed to retrieve grouped venues: ${e.message}"))
                }
            }

            // GET /api/masterdata/plaetze/validate/{id} - Validate venue suitability
            get("/validate/{id}") {
                try {
                    val platzId = call.parameters["id"]?.let { uuidFrom(it) }
                        ?: return@get call.respond(HttpStatusCode.BadRequest, ApiResponse.error<Map<String, Any>>("Invalid venue ID"))

                    val requiredTypeParam = call.request.queryParameters["requiredType"]
                    val requiredType = requiredTypeParam?.let {
                        try {
                            PlatzTypE.valueOf(it.uppercase())
                        } catch (_: Exception) {
                            return@get call.respond(HttpStatusCode.BadRequest, ApiResponse.error<Map<String, Any>>("Invalid required type: $it"))
                        }
                    }

                    val requiredDimensions = call.request.queryParameters["requiredDimensions"]
                    val requiredGroundType = call.request.queryParameters["requiredGroundType"]

                    val (isValid, reasons) = getPlatzUseCase.validateVenueSuitability(platzId, requiredType, requiredDimensions, requiredGroundType)
                    val response = mapOf(
                        "isValid" to isValid,
                        "reasons" to reasons
                    )
                    call.respond(HttpStatusCode.OK, ApiResponse.success(response))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse.error<Map<String, Any>>("Failed to validate venue: ${e.message}"))
                }
            }
        }
    }

    /**
     * Extension function to convert Platz domain object to PlatzDto.
     */
    private fun Platz.toDto(): PlatzDto {
        return PlatzDto(
            id = this.id.toString(),
            turnierId = this.turnierId.toString(),
            name = this.name,
            dimension = this.dimension,
            boden = this.boden,
            typ = this.typ.name,
            istAktiv = this.istAktiv,
            sortierReihenfolge = this.sortierReihenfolge,
            createdAt = this.createdAt.toString(),
            updatedAt = this.updatedAt.toString()
        )
    }
}

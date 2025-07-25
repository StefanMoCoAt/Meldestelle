package at.mocode.masterdata.api.rest

import at.mocode.core.domain.model.ApiResponse
import at.mocode.masterdata.application.usecase.CreateBundeslandUseCase
import at.mocode.masterdata.application.usecase.GetBundeslandUseCase
import at.mocode.masterdata.domain.model.BundeslandDefinition
import at.mocode.core.utils.validation.ApiValidationUtils
import com.benasher44.uuid.uuidFrom
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

/**
 * REST API controller for federal state management operations.
 *
 * This controller provides HTTP endpoints for the master-data context's
 * federal state functionality, following REST conventions and proper error handling.
 */
class BundeslandController(
    private val getBundeslandUseCase: GetBundeslandUseCase,
    private val createBundeslandUseCase: CreateBundeslandUseCase
) {

    /**
     * DTO for federal state API responses.
     */
    @Serializable
    data class BundeslandDto(
        val bundeslandId: String,
        val landId: String,
        val oepsCode: String? = null,
        val iso3166_2_Code: String? = null,
        val name: String,
        val kuerzel: String? = null,
        val wappenUrl: String? = null,
        val istAktiv: Boolean = true,
        val sortierReihenfolge: Int? = null,
        val createdAt: String,
        val updatedAt: String
    )

    /**
     * DTO for creating a new federal state.
     */
    @Serializable
    data class CreateBundeslandDto(
        val landId: String,
        val oepsCode: String? = null,
        val iso3166_2_Code: String? = null,
        val name: String,
        val kuerzel: String? = null,
        val wappenUrl: String? = null,
        val istAktiv: Boolean = true,
        val sortierReihenfolge: Int? = null
    )

    /**
     * DTO for updating an existing federal state.
     */
    @Serializable
    data class UpdateBundeslandDto(
        val landId: String,
        val oepsCode: String? = null,
        val iso3166_2_Code: String? = null,
        val name: String,
        val kuerzel: String? = null,
        val wappenUrl: String? = null,
        val istAktiv: Boolean = true,
        val sortierReihenfolge: Int? = null
    )

    /**
     * Configures the routing for federal state endpoints.
     */
    fun configureRouting(routing: Routing) {
        routing.route("/api/masterdata/bundeslaender") {

            // GET /api/masterdata/bundeslaender - Get all active federal states
            get {
                try {
                    val orderBySortierungParam = call.request.queryParameters["orderBySortierung"]
                    val orderBySortierung = if (orderBySortierungParam != null) {
                        try {
                            orderBySortierungParam.toBoolean()
                        } catch (_: Exception) {
                            return@get call.respond(
                                HttpStatusCode.BadRequest,
                                ApiResponse.error<List<BundeslandDto>>("Invalid orderBySortierung parameter. Must be true or false")
                            )
                        }
                    } else {
                        true
                    }

                    val bundeslaender = getBundeslandUseCase.getAllActive(orderBySortierung)
                    val bundeslandDtos = bundeslaender.map { it.toDto() }
                    call.respond(HttpStatusCode.OK, ApiResponse.success(bundeslandDtos))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse.error<List<BundeslandDto>>("Failed to retrieve federal states: ${e.message}"))
                }
            }

            // GET /api/masterdata/bundeslaender/{id} - Get federal state by ID
            get("/{id}") {
                try {
                    val bundeslandId = call.parameters["id"]?.let { uuidFrom(it) }
                        ?: return@get call.respond(HttpStatusCode.BadRequest, ApiResponse.error<BundeslandDto>("Invalid federal state ID"))

                    val bundesland = getBundeslandUseCase.getById(bundeslandId)
                    if (bundesland != null) {
                        call.respond(HttpStatusCode.OK, ApiResponse.success(bundesland.toDto()))
                    } else {
                        call.respond(HttpStatusCode.NotFound, ApiResponse.error<BundeslandDto>("Federal state not found"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse.error<BundeslandDto>("Failed to retrieve federal state: ${e.message}"))
                }
            }

            // GET /api/masterdata/bundeslaender/oeps/{code} - Get federal state by OEPS code
            get("/oeps/{code}") {
                try {
                    val oepsCode = call.parameters["code"]
                        ?: return@get call.respond(HttpStatusCode.BadRequest, ApiResponse.error<BundeslandDto>("OEPS code is required"))

                    val landIdParam = call.request.queryParameters["landId"]
                        ?: return@get call.respond(HttpStatusCode.BadRequest, ApiResponse.error<BundeslandDto>("Country ID (landId) is required"))

                    val landId = try {
                        uuidFrom(landIdParam)
                    } catch (_: Exception) {
                        return@get call.respond(HttpStatusCode.BadRequest, ApiResponse.error<BundeslandDto>("Invalid country ID format"))
                    }

                    val bundesland = getBundeslandUseCase.getByOepsCode(oepsCode, landId)
                    if (bundesland != null) {
                        call.respond(HttpStatusCode.OK, ApiResponse.success(bundesland.toDto()))
                    } else {
                        call.respond(HttpStatusCode.NotFound, ApiResponse.error<BundeslandDto>("Federal state not found"))
                    }
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, ApiResponse.error<BundeslandDto>(e.message ?: "Invalid OEPS code"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse.error<BundeslandDto>("Failed to retrieve federal state: ${e.message}"))
                }
            }

            // GET /api/masterdata/bundeslaender/iso/{code} - Get federal state by ISO 3166-2 code
            get("/iso/{code}") {
                try {
                    val isoCode = call.parameters["code"]
                        ?: return@get call.respond(HttpStatusCode.BadRequest, ApiResponse.error<BundeslandDto>("ISO 3166-2 code is required"))

                    val bundesland = getBundeslandUseCase.getByIso3166_2_Code(isoCode)
                    if (bundesland != null) {
                        call.respond(HttpStatusCode.OK, ApiResponse.success(bundesland.toDto()))
                    } else {
                        call.respond(HttpStatusCode.NotFound, ApiResponse.error<BundeslandDto>("Federal state not found"))
                    }
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, ApiResponse.error<BundeslandDto>(e.message ?: "Invalid ISO code"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse.error<BundeslandDto>("Failed to retrieve federal state: ${e.message}"))
                }
            }

            // GET /api/masterdata/bundeslaender/country/{countryId} - Get federal states by country
            get("/country/{countryId}") {
                try {
                    val landId = call.parameters["countryId"]?.let { uuidFrom(it) }
                        ?: return@get call.respond(HttpStatusCode.BadRequest, ApiResponse.error<List<BundeslandDto>>("Invalid country ID"))

                    val activeOnlyParam = call.request.queryParameters["activeOnly"]
                    val activeOnly = activeOnlyParam?.toBoolean() ?: true

                    val orderBySortierungParam = call.request.queryParameters["orderBySortierung"]
                    val orderBySortierung = orderBySortierungParam?.toBoolean() ?: true

                    val bundeslaender = getBundeslandUseCase.getByCountry(landId, activeOnly, orderBySortierung)
                    val bundeslandDtos = bundeslaender.map { it.toDto() }
                    call.respond(HttpStatusCode.OK, ApiResponse.success(bundeslandDtos))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse.error<List<BundeslandDto>>("Failed to retrieve federal states: ${e.message}"))
                }
            }

            // GET /api/masterdata/bundeslaender/search - Search federal states by name
            get("/search") {
                try {
                    val validationErrors = ApiValidationUtils.validateQueryParameters(
                        limit = call.request.queryParameters["limit"],
                        q = call.request.queryParameters["q"]
                    )

                    if (!ApiValidationUtils.isValid(validationErrors)) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponse.error<List<BundeslandDto>>(ApiValidationUtils.createErrorMessage(validationErrors))
                        )
                        return@get
                    }

                    val searchTerm = call.request.queryParameters["q"]
                        ?: return@get call.respond(HttpStatusCode.BadRequest, ApiResponse.error<List<BundeslandDto>>("Search term 'q' is required"))

                    val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 50
                    val landIdParam = call.request.queryParameters["landId"]
                    val landId = landIdParam?.let { uuidFrom(it) }

                    val bundeslaender = getBundeslandUseCase.searchByName(searchTerm, landId, limit)
                    val bundeslandDtos = bundeslaender.map { it.toDto() }
                    call.respond(HttpStatusCode.OK, ApiResponse.success(bundeslandDtos))
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, ApiResponse.error<List<BundeslandDto>>(e.message ?: "Invalid search parameters"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse.error<List<BundeslandDto>>("Failed to search federal states: ${e.message}"))
                }
            }

            // POST /api/masterdata/bundeslaender - Create new federal state
            post {
                try {
                    val createDto = call.receive<CreateBundeslandDto>()

                    // Basic validation
                    if (createDto.name.isBlank()) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponse.error<BundeslandDto>("Name is required")
                        )
                        return@post
                    }

                    try {
                        uuidFrom(createDto.landId)
                    } catch (_: Exception) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponse.error<BundeslandDto>("Invalid country ID format")
                        )
                        return@post
                    }

                    val request = CreateBundeslandUseCase.CreateBundeslandRequest(
                        landId = uuidFrom(createDto.landId),
                        oepsCode = createDto.oepsCode,
                        iso3166_2_Code = createDto.iso3166_2_Code,
                        name = createDto.name,
                        kuerzel = createDto.kuerzel,
                        wappenUrl = createDto.wappenUrl,
                        istAktiv = createDto.istAktiv,
                        sortierReihenfolge = createDto.sortierReihenfolge
                    )

                    val result = createBundeslandUseCase.createBundesland(request)
                    if (result.success) {
                        call.respond(HttpStatusCode.Created, ApiResponse.success(result.bundesland!!.toDto()))
                    } else {
                        call.respond(HttpStatusCode.BadRequest, ApiResponse.error<BundeslandDto>("Validation failed: ${result.errors.joinToString(", ")}"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse.error<BundeslandDto>("Failed to create federal state: ${e.message}"))
                }
            }

            // PUT /api/masterdata/bundeslaender/{id} - Update existing federal state
            put("/{id}") {
                try {
                    val bundeslandId = call.parameters["id"]?.let { uuidFrom(it) }
                        ?: return@put call.respond(HttpStatusCode.BadRequest, ApiResponse.error<BundeslandDto>("Invalid federal state ID"))

                    val updateDto = call.receive<UpdateBundeslandDto>()

                    // Basic validation
                    if (updateDto.name.isBlank()) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponse.error<BundeslandDto>("Name is required")
                        )
                        return@put
                    }

                    try {
                        uuidFrom(updateDto.landId)
                    } catch (_: Exception) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponse.error<BundeslandDto>("Invalid country ID format")
                        )
                        return@put
                    }

                    val request = CreateBundeslandUseCase.UpdateBundeslandRequest(
                        bundeslandId = bundeslandId,
                        landId = uuidFrom(updateDto.landId),
                        oepsCode = updateDto.oepsCode,
                        iso3166_2_Code = updateDto.iso3166_2_Code,
                        name = updateDto.name,
                        kuerzel = updateDto.kuerzel,
                        wappenUrl = updateDto.wappenUrl,
                        istAktiv = updateDto.istAktiv,
                        sortierReihenfolge = updateDto.sortierReihenfolge
                    )

                    val result = createBundeslandUseCase.updateBundesland(request)
                    if (result.success) {
                        call.respond(HttpStatusCode.OK, ApiResponse.success(result.bundesland!!.toDto()))
                    } else {
                        call.respond(HttpStatusCode.BadRequest, ApiResponse.error<BundeslandDto>("Validation failed: ${result.errors.joinToString(", ")}"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse.error<BundeslandDto>("Failed to update federal state: ${e.message}"))
                }
            }

            // DELETE /api/masterdata/bundeslaender/{id} - Delete federal state
            delete("/{id}") {
                try {
                    val bundeslandId = call.parameters["id"]?.let { uuidFrom(it) }
                        ?: return@delete call.respond(HttpStatusCode.BadRequest, ApiResponse.error<Unit>("Invalid federal state ID"))

                    val result = createBundeslandUseCase.deleteBundesland(bundeslandId)
                    if (result.success) {
                        call.respond(HttpStatusCode.NoContent)
                    } else {
                        call.respond(HttpStatusCode.NotFound, ApiResponse.error<Unit>("Federal state not found: ${result.errors.joinToString(", ")}"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse.error<Unit>("Failed to delete federal state: ${e.message}"))
                }
            }

            // GET /api/masterdata/bundeslaender/count/{countryId} - Count active federal states by country
            get("/count/{countryId}") {
                try {
                    val landId = call.parameters["countryId"]?.let { uuidFrom(it) }
                        ?: return@get call.respond(HttpStatusCode.BadRequest, ApiResponse.error<Long>("Invalid country ID"))

                    val count = getBundeslandUseCase.countActiveByCountry(landId)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(count))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse.error<Long>("Failed to count federal states: ${e.message}"))
                }
            }
        }
    }

    /**
     * Extension function to convert BundeslandDefinition domain object to BundeslandDto.
     */
    private fun BundeslandDefinition.toDto(): BundeslandDto {
        return BundeslandDto(
            bundeslandId = this.bundeslandId.toString(),
            landId = this.landId.toString(),
            oepsCode = this.oepsCode,
            iso3166_2_Code = this.iso3166_2_Code,
            name = this.name,
            kuerzel = this.kuerzel,
            wappenUrl = this.wappenUrl,
            istAktiv = this.istAktiv,
            sortierReihenfolge = this.sortierReihenfolge,
            createdAt = this.createdAt.toString(),
            updatedAt = this.updatedAt.toString()
        )
    }
}

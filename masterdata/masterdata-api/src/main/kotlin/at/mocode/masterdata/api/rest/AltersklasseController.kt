package at.mocode.masterdata.api.rest

import at.mocode.core.domain.model.ApiResponse
import at.mocode.core.domain.model.SparteE
import at.mocode.masterdata.application.usecase.CreateAltersklasseUseCase
import at.mocode.masterdata.application.usecase.GetAltersklasseUseCase
import at.mocode.masterdata.domain.model.AltersklasseDefinition
import at.mocode.core.utils.validation.ApiValidationUtils
import com.benasher44.uuid.uuidFrom
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

/**
 * REST API controller for age class management operations.
 *
 * This controller provides HTTP endpoints for the master-data context's
 * age class functionality, following REST conventions and proper error handling.
 */
class AltersklasseController(
    private val getAltersklasseUseCase: GetAltersklasseUseCase,
    private val createAltersklasseUseCase: CreateAltersklasseUseCase
) {

    /**
     * DTO for age class API responses.
     */
    @Serializable
    data class AltersklasseDto(
        val altersklasseId: String,
        val altersklasseCode: String,
        val bezeichnung: String,
        val minAlter: Int? = null,
        val maxAlter: Int? = null,
        val stichtagRegelText: String? = null,
        val sparteFilter: String? = null,
        val geschlechtFilter: String? = null,
        val oetoRegelReferenzId: String? = null,
        val istAktiv: Boolean = true,
        val createdAt: String,
        val updatedAt: String
    )

    /**
     * DTO for creating a new age class.
     */
    @Serializable
    data class CreateAltersklasseDto(
        val altersklasseCode: String,
        val bezeichnung: String,
        val minAlter: Int? = null,
        val maxAlter: Int? = null,
        val stichtagRegelText: String? = "31.12. des laufenden Kalenderjahres",
        val sparteFilter: String? = null,
        val geschlechtFilter: String? = null,
        val oetoRegelReferenzId: String? = null,
        val istAktiv: Boolean = true
    )

    /**
     * DTO for updating an existing age class.
     */
    @Serializable
    data class UpdateAltersklasseDto(
        val altersklasseCode: String,
        val bezeichnung: String,
        val minAlter: Int? = null,
        val maxAlter: Int? = null,
        val stichtagRegelText: String? = "31.12. des laufenden Kalenderjahres",
        val sparteFilter: String? = null,
        val geschlechtFilter: String? = null,
        val oetoRegelReferenzId: String? = null,
        val istAktiv: Boolean = true
    )

    /**
     * Configures the routing for age class endpoints.
     */
    fun configureRouting(routing: Routing) {
        routing.route("/api/masterdata/altersklassen") {

            // GET /api/masterdata/altersklassen - Get all active age classes
            get {
                try {
                    val sparteFilterParam = call.request.queryParameters["sparte"]
                    val sparteFilter = sparteFilterParam?.let {
                        try {
                            SparteE.valueOf(it.uppercase())
                        } catch (_: Exception) {
                            return@get call.respond(
                                HttpStatusCode.BadRequest,
                                ApiResponse.error<List<AltersklasseDto>>("Invalid sparte parameter: $it")
                            )
                        }
                    }

                    val geschlechtFilterParam = call.request.queryParameters["geschlecht"]
                    val geschlechtFilter = geschlechtFilterParam?.let { gender ->
                        if (gender.length == 1 && (gender == "M" || gender == "W")) {
                            gender[0]
                        } else {
                            return@get call.respond(
                                HttpStatusCode.BadRequest,
                                ApiResponse.error<List<AltersklasseDto>>("Invalid geschlecht parameter. Must be 'M' or 'W'")
                            )
                        }
                    }

                    val altersklassen = getAltersklasseUseCase.getAllActive(sparteFilter, geschlechtFilter)
                    val altersklasseDtos = altersklassen.map { it.toDto() }
                    call.respond(HttpStatusCode.OK, ApiResponse.success(altersklasseDtos))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse.error<List<AltersklasseDto>>("Failed to retrieve age classes: ${e.message}"))
                }
            }

            // GET /api/masterdata/altersklassen/{id} - Get age class by ID
            get("/{id}") {
                try {
                    val altersklasseId = call.parameters["id"]?.let { uuidFrom(it) }
                        ?: return@get call.respond(HttpStatusCode.BadRequest, ApiResponse.error<AltersklasseDto>("Invalid age class ID"))

                    val altersklasse = getAltersklasseUseCase.getById(altersklasseId)
                    if (altersklasse != null) {
                        call.respond(HttpStatusCode.OK, ApiResponse.success(altersklasse.toDto()))
                    } else {
                        call.respond(HttpStatusCode.NotFound, ApiResponse.error<AltersklasseDto>("Age class not found"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse.error<AltersklasseDto>("Failed to retrieve age class: ${e.message}"))
                }
            }

            // GET /api/masterdata/altersklassen/code/{code} - Get age class by code
            get("/code/{code}") {
                try {
                    val altersklasseCode = call.parameters["code"]
                        ?: return@get call.respond(HttpStatusCode.BadRequest, ApiResponse.error<AltersklasseDto>("Age class code is required"))

                    val altersklasse = getAltersklasseUseCase.getByCode(altersklasseCode)
                    if (altersklasse != null) {
                        call.respond(HttpStatusCode.OK, ApiResponse.success(altersklasse.toDto()))
                    } else {
                        call.respond(HttpStatusCode.NotFound, ApiResponse.error<AltersklasseDto>("Age class not found"))
                    }
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, ApiResponse.error<AltersklasseDto>(e.message ?: "Invalid age class code"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse.error<AltersklasseDto>("Failed to retrieve age class: ${e.message}"))
                }
            }

            // GET /api/masterdata/altersklassen/search - Search age classes by name
            get("/search") {
                try {
                    val validationErrors = ApiValidationUtils.validateQueryParameters(
                        limit = call.request.queryParameters["limit"],
                        q = call.request.queryParameters["q"]
                    )

                    if (!ApiValidationUtils.isValid(validationErrors)) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponse.error<List<AltersklasseDto>>(ApiValidationUtils.createErrorMessage(validationErrors))
                        )
                        return@get
                    }

                    val searchTerm = call.request.queryParameters["q"]
                        ?: return@get call.respond(HttpStatusCode.BadRequest, ApiResponse.error<List<AltersklasseDto>>("Search term 'q' is required"))

                    val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 50

                    val altersklassen = getAltersklasseUseCase.searchByName(searchTerm, limit)
                    val altersklasseDtos = altersklassen.map { it.toDto() }
                    call.respond(HttpStatusCode.OK, ApiResponse.success(altersklasseDtos))
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, ApiResponse.error<List<AltersklasseDto>>(e.message ?: "Invalid search parameters"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse.error<List<AltersklasseDto>>("Failed to search age classes: ${e.message}"))
                }
            }

            // GET /api/masterdata/altersklassen/age/{age} - Get age classes applicable for specific age
            get("/age/{age}") {
                try {
                    val age = call.parameters["age"]?.toIntOrNull()
                        ?: return@get call.respond(HttpStatusCode.BadRequest, ApiResponse.error<List<AltersklasseDto>>("Invalid age parameter"))

                    if (age < 0) {
                        return@get call.respond(HttpStatusCode.BadRequest, ApiResponse.error<List<AltersklasseDto>>("Age must be non-negative"))
                    }

                    val sparteFilterParam = call.request.queryParameters["sparte"]
                    val sparteFilter = sparteFilterParam?.let { SparteE.valueOf(it.uppercase()) }

                    val geschlechtFilterParam = call.request.queryParameters["geschlecht"]
                    val geschlechtFilter = geschlechtFilterParam?.let { gender ->
                        if (gender.length == 1 && (gender == "M" || gender == "W")) {
                            gender[0]
                        } else {
                            return@get call.respond(
                                HttpStatusCode.BadRequest,
                                ApiResponse.error<List<AltersklasseDto>>("Invalid geschlecht parameter. Must be 'M' or 'W'")
                            )
                        }
                    }

                    val altersklassen = getAltersklasseUseCase.getApplicableForAge(age, sparteFilter, geschlechtFilter)
                    val altersklasseDtos = altersklassen.map { it.toDto() }
                    call.respond(HttpStatusCode.OK, ApiResponse.success(altersklasseDtos))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse.error<List<AltersklasseDto>>("Failed to retrieve age classes: ${e.message}"))
                }
            }

            // GET /api/masterdata/altersklassen/sparte/{sparte} - Get age classes by sport type
            get("/sparte/{sparte}") {
                try {
                    val sparteParam = call.parameters["sparte"]
                        ?: return@get call.respond(HttpStatusCode.BadRequest, ApiResponse.error<List<AltersklasseDto>>("Sport type is required"))

                    val sparte = try {
                        SparteE.valueOf(sparteParam.uppercase())
                    } catch (_: Exception) {
                        return@get call.respond(HttpStatusCode.BadRequest, ApiResponse.error<List<AltersklasseDto>>("Invalid sport type: $sparteParam"))
                    }

                    val activeOnlyParam = call.request.queryParameters["activeOnly"]
                    val activeOnly = activeOnlyParam?.toBoolean() ?: true

                    val altersklassen = getAltersklasseUseCase.getBySparte(sparte, activeOnly)
                    val altersklasseDtos = altersklassen.map { it.toDto() }
                    call.respond(HttpStatusCode.OK, ApiResponse.success(altersklasseDtos))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse.error<List<AltersklasseDto>>("Failed to retrieve age classes: ${e.message}"))
                }
            }

            // POST /api/masterdata/altersklassen - Create new age class
            post {
                try {
                    val createDto = call.receive<CreateAltersklasseDto>()

                    // Basic validation
                    if (createDto.altersklasseCode.isBlank()) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponse.error<AltersklasseDto>("Age class code is required")
                        )
                        return@post
                    }

                    if (createDto.bezeichnung.isBlank()) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponse.error<AltersklasseDto>("Bezeichnung is required")
                        )
                        return@post
                    }

                    val sparteFilter = createDto.sparteFilter?.let {
                        try {
                            SparteE.valueOf(it.uppercase())
                        } catch (_: Exception) {
                            return@post call.respond(
                                HttpStatusCode.BadRequest,
                                ApiResponse.error<AltersklasseDto>("Invalid sparte filter: $it")
                            )
                        }
                    }

                    val geschlechtFilter = createDto.geschlechtFilter?.let { gender ->
                        if (gender.length == 1 && (gender == "M" || gender == "W")) {
                            gender[0]
                        } else {
                            return@post call.respond(
                                HttpStatusCode.BadRequest,
                                ApiResponse.error<AltersklasseDto>("Invalid geschlecht filter. Must be 'M' or 'W'")
                            )
                        }
                    }

                    val oetoRegelReferenzId = createDto.oetoRegelReferenzId?.let {
                        try {
                            uuidFrom(it)
                        } catch (_: Exception) {
                            return@post call.respond(
                                HttpStatusCode.BadRequest,
                                ApiResponse.error<AltersklasseDto>("Invalid OETO regel referenz ID format")
                            )
                        }
                    }

                    val request = CreateAltersklasseUseCase.CreateAltersklasseRequest(
                        altersklasseCode = createDto.altersklasseCode,
                        bezeichnung = createDto.bezeichnung,
                        minAlter = createDto.minAlter,
                        maxAlter = createDto.maxAlter,
                        stichtagRegelText = createDto.stichtagRegelText,
                        sparteFilter = sparteFilter,
                        geschlechtFilter = geschlechtFilter,
                        oetoRegelReferenzId = oetoRegelReferenzId,
                        istAktiv = createDto.istAktiv
                    )

                    val result = createAltersklasseUseCase.createAltersklasse(request)
                    if (result.success) {
                        call.respond(HttpStatusCode.Created, ApiResponse.success(result.altersklasse!!.toDto()))
                    } else {
                        call.respond(HttpStatusCode.BadRequest, ApiResponse.error<AltersklasseDto>("Validation failed: ${result.errors.joinToString(", ")}"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse.error<AltersklasseDto>("Failed to create age class: ${e.message}"))
                }
            }

            // PUT /api/masterdata/altersklassen/{id} - Update existing age class
            put("/{id}") {
                try {
                    val altersklasseId = call.parameters["id"]?.let { uuidFrom(it) }
                        ?: return@put call.respond(HttpStatusCode.BadRequest, ApiResponse.error<AltersklasseDto>("Invalid age class ID"))

                    val updateDto = call.receive<UpdateAltersklasseDto>()

                    // Basic validation
                    if (updateDto.altersklasseCode.isBlank()) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponse.error<AltersklasseDto>("Age class code is required")
                        )
                        return@put
                    }

                    if (updateDto.bezeichnung.isBlank()) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ApiResponse.error<AltersklasseDto>("Bezeichnung is required")
                        )
                        return@put
                    }

                    val sparteFilter = updateDto.sparteFilter?.let {
                        try {
                            SparteE.valueOf(it.uppercase())
                        } catch (_: Exception) {
                            return@put call.respond(
                                HttpStatusCode.BadRequest,
                                ApiResponse.error<AltersklasseDto>("Invalid sparte filter: $it")
                            )
                        }
                    }

                    val geschlechtFilter = updateDto.geschlechtFilter?.let { gender ->
                        if (gender.length == 1 && (gender == "M" || gender == "W")) {
                            gender[0]
                        } else {
                            return@put call.respond(
                                HttpStatusCode.BadRequest,
                                ApiResponse.error<AltersklasseDto>("Invalid geschlecht filter. Must be 'M' or 'W'")
                            )
                        }
                    }

                    val oetoRegelReferenzId = updateDto.oetoRegelReferenzId?.let {
                        try {
                            uuidFrom(it)
                        } catch (_: Exception) {
                            return@put call.respond(
                                HttpStatusCode.BadRequest,
                                ApiResponse.error<AltersklasseDto>("Invalid OETO regel referenz ID format")
                            )
                        }
                    }

                    val request = CreateAltersklasseUseCase.UpdateAltersklasseRequest(
                        altersklasseId = altersklasseId,
                        altersklasseCode = updateDto.altersklasseCode,
                        bezeichnung = updateDto.bezeichnung,
                        minAlter = updateDto.minAlter,
                        maxAlter = updateDto.maxAlter,
                        stichtagRegelText = updateDto.stichtagRegelText,
                        sparteFilter = sparteFilter,
                        geschlechtFilter = geschlechtFilter,
                        oetoRegelReferenzId = oetoRegelReferenzId,
                        istAktiv = updateDto.istAktiv
                    )

                    val result = createAltersklasseUseCase.updateAltersklasse(request)
                    if (result.success) {
                        call.respond(HttpStatusCode.OK, ApiResponse.success(result.altersklasse!!.toDto()))
                    } else {
                        call.respond(HttpStatusCode.BadRequest, ApiResponse.error<AltersklasseDto>("Validation failed: ${result.errors.joinToString(", ")}"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse.error<AltersklasseDto>("Failed to update age class: ${e.message}"))
                }
            }

            // DELETE /api/masterdata/altersklassen/{id} - Delete age class
            delete("/{id}") {
                try {
                    val altersklasseId = call.parameters["id"]?.let { uuidFrom(it) }
                        ?: return@delete call.respond(HttpStatusCode.BadRequest, ApiResponse.error<Unit>("Invalid age class ID"))

                    val result = createAltersklasseUseCase.deleteAltersklasse(altersklasseId)
                    if (result.success) {
                        call.respond(HttpStatusCode.NoContent)
                    } else {
                        call.respond(HttpStatusCode.NotFound, ApiResponse.error<Unit>("Age class not found: ${result.errors.joinToString(", ")}"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse.error<Unit>("Failed to delete age class: ${e.message}"))
                }
            }

            // GET /api/masterdata/altersklassen/eligible/{id} - Check eligibility for age class
            get("/eligible/{id}") {
                try {
                    val altersklasseId = call.parameters["id"]?.let { uuidFrom(it) }
                        ?: return@get call.respond(HttpStatusCode.BadRequest, ApiResponse.error<Boolean>("Invalid age class ID"))

                    val ageParam = call.request.queryParameters["age"]?.toIntOrNull()
                        ?: return@get call.respond(HttpStatusCode.BadRequest, ApiResponse.error<Boolean>("Age parameter is required"))

                    val geschlechtParam = call.request.queryParameters["geschlecht"]
                        ?: return@get call.respond(HttpStatusCode.BadRequest, ApiResponse.error<Boolean>("Gender parameter is required"))

                    if (geschlechtParam.length != 1 || (geschlechtParam != "M" && geschlechtParam != "W")) {
                        return@get call.respond(HttpStatusCode.BadRequest, ApiResponse.error<Boolean>("Gender must be 'M' or 'W'"))
                    }

                    val isEligible = getAltersklasseUseCase.isEligible(altersklasseId, ageParam, geschlechtParam[0])
                    call.respond(HttpStatusCode.OK, ApiResponse.success(isEligible))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ApiResponse.error<Boolean>("Failed to check eligibility: ${e.message}"))
                }
            }
        }
    }

    /**
     * Extension function to convert AltersklasseDefinition domain object to AltersklasseDto.
     */
    private fun AltersklasseDefinition.toDto(): AltersklasseDto {
        return AltersklasseDto(
            altersklasseId = this.altersklasseId.toString(),
            altersklasseCode = this.altersklasseCode,
            bezeichnung = this.bezeichnung,
            minAlter = this.minAlter,
            maxAlter = this.maxAlter,
            stichtagRegelText = this.stichtagRegelText,
            sparteFilter = this.sparteFilter?.name,
            geschlechtFilter = this.geschlechtFilter?.toString(),
            oetoRegelReferenzId = this.oetoRegelReferenzId?.toString(),
            istAktiv = this.istAktiv,
            createdAt = this.createdAt.toString(),
            updatedAt = this.updatedAt.toString()
        )
    }
}

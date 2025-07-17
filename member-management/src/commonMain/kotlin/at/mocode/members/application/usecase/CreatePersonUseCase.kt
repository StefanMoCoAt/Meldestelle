package at.mocode.members.application.usecase

import at.mocode.dto.base.ApiResponse
import at.mocode.dto.base.ErrorDto
import at.mocode.members.domain.model.DomPerson
import at.mocode.members.domain.repository.PersonRepository
import at.mocode.members.domain.repository.VereinRepository
import at.mocode.members.domain.service.MasterDataService
import kotlinx.datetime.Clock

/**
 * Use case for creating a new person in the member management context.
 *
 * This use case handles the business logic for person creation including:
 * - Validation of input data
 * - Checking for duplicate OEPS Satznummer
 * - Validation of referenced entities (club, country)
 * - Person creation and persistence
 */
class CreatePersonUseCase(
    private val personRepository: PersonRepository,
    private val vereinRepository: VereinRepository,
    private val masterDataService: MasterDataService
) {

    /**
     * Request data for creating a person.
     */
    data class CreatePersonRequest(
        val oepsSatzNr: String?,
        val nachname: String,
        val vorname: String,
        val titel: String? = null,
        val geburtsdatum: kotlinx.datetime.LocalDate? = null,
        val geschlechtE: at.mocode.enums.GeschlechtE? = null,
        val nationalitaetLandId: com.benasher44.uuid.Uuid? = null,
        val feiId: String? = null,
        val telefon: String? = null,
        val email: String? = null,
        val strasse: String? = null,
        val plz: String? = null,
        val ort: String? = null,
        val adresszusatzZusatzinfo: String? = null,
        val stammVereinId: com.benasher44.uuid.Uuid? = null,
        val mitgliedsNummerBeiStammVerein: String? = null,
        val istGesperrt: Boolean = false,
        val sperrGrund: String? = null,
        val altersklasseOepsCodeRaw: String? = null,
        val istJungerReiterOepsFlag: Boolean = false,
        val kaderStatusOepsRaw: String? = null,
        val datenQuelle: at.mocode.enums.DatenQuelleE = at.mocode.enums.DatenQuelleE.MANUELL,
        val notizenIntern: String? = null
    )

    /**
     * Response data for person creation.
     */
    data class CreatePersonResponse(
        val person: DomPerson
    )

    /**
     * Executes the create person use case.
     *
     * @param request The person creation request
     * @return ApiResponse containing the created person or error information
     */
    suspend fun execute(request: CreatePersonRequest): ApiResponse<CreatePersonResponse> {
        try {
            // Validate required fields
            val validationErrors = validateRequest(request)
            if (validationErrors.isNotEmpty()) {
                return ApiResponse(
                    success = false,
                    error = ErrorDto(
                        code = "VALIDATION_ERROR",
                        message = "Invalid input data",
                        details = validationErrors
                    )
                )
            }

            // Check for duplicate OEPS Satznummer
            if (request.oepsSatzNr != null) {
                if (personRepository.existsByOepsSatzNr(request.oepsSatzNr)) {
                    return ApiResponse(
                        success = false,
                        error = ErrorDto(
                            code = "DUPLICATE_OEPS_SATZNR",
                            message = "A person with this OEPS Satznummer already exists"
                        )
                    )
                }
            }

            // Validate referenced entities
            val entityValidationErrors = validateReferencedEntities(request)
            if (entityValidationErrors.isNotEmpty()) {
                return ApiResponse(
                    success = false,
                    error = ErrorDto(
                        code = "INVALID_REFERENCES",
                        message = "Referenced entities not found",
                        details = entityValidationErrors
                    )
                )
            }

            // Create the person
            val person = DomPerson(
                oepsSatzNr = request.oepsSatzNr,
                nachname = request.nachname,
                vorname = request.vorname,
                titel = request.titel,
                geburtsdatum = request.geburtsdatum,
                geschlechtE = request.geschlechtE,
                nationalitaetLandId = request.nationalitaetLandId,
                feiId = request.feiId,
                telefon = request.telefon,
                email = request.email,
                strasse = request.strasse,
                plz = request.plz,
                ort = request.ort,
                adresszusatzZusatzinfo = request.adresszusatzZusatzinfo,
                stammVereinId = request.stammVereinId,
                mitgliedsNummerBeiStammVerein = request.mitgliedsNummerBeiStammVerein,
                istGesperrt = request.istGesperrt,
                sperrGrund = request.sperrGrund,
                altersklasseOepsCodeRaw = request.altersklasseOepsCodeRaw,
                istJungerReiterOepsFlag = request.istJungerReiterOepsFlag,
                kaderStatusOepsRaw = request.kaderStatusOepsRaw,
                datenQuelle = request.datenQuelle,
                notizenIntern = request.notizenIntern,
                createdAt = Clock.System.now(),
                updatedAt = Clock.System.now()
            )

            // Save the person
            val savedPerson = personRepository.save(person)

            return ApiResponse(
                success = true,
                data = CreatePersonResponse(savedPerson)
            )

        } catch (e: Exception) {
            return ApiResponse(
                success = false,
                error = ErrorDto(
                    code = "INTERNAL_ERROR",
                    message = "An error occurred while creating the person: ${e.message}"
                )
            )
        }
    }

    private fun validateRequest(request: CreatePersonRequest): Map<String, String> {
        val errors = mutableMapOf<String, String>()

        if (request.nachname.isBlank()) {
            errors["nachname"] = "Last name is required"
        }

        if (request.vorname.isBlank()) {
            errors["vorname"] = "First name is required"
        }

        if (request.oepsSatzNr != null && request.oepsSatzNr.length != 6) {
            errors["oepsSatzNr"] = "OEPS Satznummer must be exactly 6 digits"
        }

        if (request.email != null && !isValidEmail(request.email)) {
            errors["email"] = "Invalid email format"
        }

        return errors
    }

    private suspend fun validateReferencedEntities(request: CreatePersonRequest): Map<String, String> {
        val errors = mutableMapOf<String, String>()

        // Validate club reference
        if (request.stammVereinId != null) {
            val verein = vereinRepository.findById(request.stammVereinId)
            if (verein == null) {
                errors["stammVereinId"] = "Referenced club not found"
            }
        }

        // Validate country reference
        if (request.nationalitaetLandId != null) {
            if (!masterDataService.countryExists(request.nationalitaetLandId)) {
                errors["nationalitaetLandId"] = "Referenced country not found"
            }
        }

        return errors
    }

    private fun isValidEmail(email: String): Boolean {
        return email.contains("@") && email.contains(".")
    }
}

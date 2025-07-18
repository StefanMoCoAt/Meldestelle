package at.mocode.members.application.usecase

import at.mocode.dto.base.ApiResponse
import at.mocode.dto.base.ErrorDto
import at.mocode.members.domain.model.DomPerson
import at.mocode.members.domain.repository.PersonRepository
import at.mocode.members.domain.repository.VereinRepository
import at.mocode.members.domain.service.MasterDataService
import at.mocode.validation.ValidationUtils
import at.mocode.validation.ValidationResult
import at.mocode.validation.ValidationError
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
            val validationResult = validateRequest(request)
            if (!validationResult.isValid()) {
                val errors = (validationResult as ValidationResult.Invalid).errors
                return ApiResponse(
                    success = false,
                    error = ErrorDto(
                        code = "VALIDATION_ERROR",
                        message = "Invalid input data",
                        details = errors.associate { it.field to it.message }
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
            val entityValidationResult = validateReferencedEntities(request)
            if (!entityValidationResult.isValid()) {
                val errors = (entityValidationResult as ValidationResult.Invalid).errors
                return ApiResponse(
                    success = false,
                    error = ErrorDto(
                        code = "INVALID_REFERENCES",
                        message = "Referenced entities not found",
                        details = errors.associate { it.field to it.message }
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

    private fun validateRequest(request: CreatePersonRequest): ValidationResult {
        val errors = mutableListOf<ValidationError>()

        // Validate required fields using ValidationUtils
        ValidationUtils.validateNotBlank(request.nachname, "nachname")?.let { error ->
            errors.add(error)
        }

        ValidationUtils.validateNotBlank(request.vorname, "vorname")?.let { error ->
            errors.add(error)
        }

        // Validate OEPS Satz number using ValidationUtils
        ValidationUtils.validateOepsSatzNr(request.oepsSatzNr, "oepsSatzNr")?.let { error ->
            errors.add(error)
        }

        // Validate email using ValidationUtils
        ValidationUtils.validateEmail(request.email, "email")?.let { error ->
            errors.add(error)
        }

        // Validate phone number using ValidationUtils
        ValidationUtils.validatePhoneNumber(request.telefon, "telefon")?.let { error ->
            errors.add(error)
        }

        // Validate postal code using ValidationUtils
        ValidationUtils.validatePostalCode(request.plz, "plz")?.let { error ->
            errors.add(error)
        }

        // Validate birth date using ValidationUtils
        ValidationUtils.validateBirthDate(request.geburtsdatum, "geburtsdatum")?.let { error ->
            errors.add(error)
        }

        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }

    private suspend fun validateReferencedEntities(request: CreatePersonRequest): ValidationResult {
        val errors = mutableListOf<ValidationError>()

        // Validate club reference
        if (request.stammVereinId != null) {
            val verein = vereinRepository.findById(request.stammVereinId)
            if (verein == null) {
                errors.add(ValidationError("stammVereinId", "Referenced club not found", "NOT_FOUND"))
            }
        }

        // Validate country reference
        if (request.nationalitaetLandId != null) {
            if (!masterDataService.countryExists(request.nationalitaetLandId)) {
                errors.add(ValidationError("nationalitaetLandId", "Referenced country not found", "NOT_FOUND"))
            }
        }

        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }

}

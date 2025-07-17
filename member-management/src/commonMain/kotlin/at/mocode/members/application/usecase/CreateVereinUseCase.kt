package at.mocode.members.application.usecase

import at.mocode.dto.base.ApiResponse
import at.mocode.dto.base.ErrorDto
import at.mocode.members.domain.model.DomVerein
import at.mocode.members.domain.repository.VereinRepository
import at.mocode.members.domain.service.MasterDataService
import kotlinx.datetime.Clock

/**
 * Use case for creating a new club/association in the member management context.
 *
 * This use case handles the business logic for club creation including:
 * - Validation of input data
 * - Checking for duplicate OEPS Vereinsnummer
 * - Validation of referenced entities (country, state)
 * - Club creation and persistence
 */
class CreateVereinUseCase(
    private val vereinRepository: VereinRepository,
    private val masterDataService: MasterDataService
) {

    /**
     * Request data for creating a club.
     */
    data class CreateVereinRequest(
        val oepsVereinsNr: String?,
        val name: String,
        val kuerzel: String? = null,
        val adresseStrasse: String? = null,
        val plz: String? = null,
        val ort: String? = null,
        val bundeslandId: com.benasher44.uuid.Uuid? = null,
        val landId: com.benasher44.uuid.Uuid,
        val emailAllgemein: String? = null,
        val telefonAllgemein: String? = null,
        val webseiteUrl: String? = null,
        val datenQuelle: at.mocode.enums.DatenQuelleE = at.mocode.enums.DatenQuelleE.MANUELL,
        val notizenIntern: String? = null
    )

    /**
     * Response data for club creation.
     */
    data class CreateVereinResponse(
        val verein: DomVerein
    )

    /**
     * Executes the create club use case.
     *
     * @param request The club creation request
     * @return ApiResponse containing the created club or error information
     */
    suspend fun execute(request: CreateVereinRequest): ApiResponse<CreateVereinResponse> {
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

            // Check for duplicate OEPS Vereinsnummer
            if (request.oepsVereinsNr != null) {
                if (vereinRepository.existsByOepsVereinsNr(request.oepsVereinsNr)) {
                    return ApiResponse(
                        success = false,
                        error = ErrorDto(
                            code = "DUPLICATE_OEPS_VEREINSNR",
                            message = "A club with this OEPS Vereinsnummer already exists"
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

            // Create the club
            val verein = DomVerein(
                oepsVereinsNr = request.oepsVereinsNr,
                name = request.name,
                kuerzel = request.kuerzel,
                adresseStrasse = request.adresseStrasse,
                plz = request.plz,
                ort = request.ort,
                bundeslandId = request.bundeslandId,
                landId = request.landId,
                emailAllgemein = request.emailAllgemein,
                telefonAllgemein = request.telefonAllgemein,
                webseiteUrl = request.webseiteUrl,
                datenQuelle = request.datenQuelle,
                notizenIntern = request.notizenIntern,
                createdAt = Clock.System.now(),
                updatedAt = Clock.System.now()
            )

            // Save the club
            val savedVerein = vereinRepository.save(verein)

            return ApiResponse(
                success = true,
                data = CreateVereinResponse(savedVerein)
            )

        } catch (e: Exception) {
            return ApiResponse(
                success = false,
                error = ErrorDto(
                    code = "INTERNAL_ERROR",
                    message = "An error occurred while creating the club: ${e.message}"
                )
            )
        }
    }

    private fun validateRequest(request: CreateVereinRequest): Map<String, String> {
        val errors = mutableMapOf<String, String>()

        if (request.name.isBlank()) {
            errors["name"] = "Club name is required"
        }

        if (request.oepsVereinsNr != null && request.oepsVereinsNr.length != 4) {
            errors["oepsVereinsNr"] = "OEPS Vereinsnummer must be exactly 4 digits"
        }

        if (request.emailAllgemein != null && !isValidEmail(request.emailAllgemein)) {
            errors["emailAllgemein"] = "Invalid email format"
        }

        if (request.webseiteUrl != null && !isValidUrl(request.webseiteUrl)) {
            errors["webseiteUrl"] = "Invalid URL format"
        }

        return errors
    }

    private suspend fun validateReferencedEntities(request: CreateVereinRequest): Map<String, String> {
        val errors = mutableMapOf<String, String>()

        // Validate country reference (required)
        if (!masterDataService.countryExists(request.landId)) {
            errors["landId"] = "Referenced country not found"
        }

        // Validate state reference (optional)
        if (request.bundeslandId != null) {
            if (!masterDataService.stateExists(request.bundeslandId)) {
                errors["bundeslandId"] = "Referenced state not found"
            }
        }

        return errors
    }

    private fun isValidEmail(email: String): Boolean {
        return email.contains("@") && email.contains(".")
    }

    private fun isValidUrl(url: String): Boolean {
        return url.startsWith("http://") || url.startsWith("https://")
    }
}

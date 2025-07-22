package at.mocode.events.application.usecase

import at.mocode.core.domain.model.ApiResponse
import at.mocode.core.domain.model.ErrorDto
import at.mocode.events.domain.model.Veranstaltung
import at.mocode.events.domain.repository.VeranstaltungRepository
import at.mocode.core.domain.model.SparteE
import at.mocode.core.utils.validation.ValidationResult
import at.mocode.core.utils.validation.ValidationError
import com.benasher44.uuid.Uuid
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate

/**
 * Use case for updating existing events (Veranstaltung).
 *
 * This use case handles the business logic for updating events,
 * including validation and persistence.
 */
class UpdateVeranstaltungUseCase(
    private val veranstaltungRepository: VeranstaltungRepository
) {

    /**
     * Request data for updating an event.
     */
    data class UpdateVeranstaltungRequest(
        val veranstaltungId: Uuid,
        val name: String,
        val beschreibung: String? = null,
        val startDatum: LocalDate,
        val endDatum: LocalDate,
        val ort: String,
        val veranstalterVereinId: Uuid,
        val sparten: List<SparteE> = emptyList(),
        val istAktiv: Boolean = true,
        val istOeffentlich: Boolean = true,
        val maxTeilnehmer: Int? = null,
        val anmeldeschluss: LocalDate? = null
    )

    /**
     * Response data containing the updated event.
     */
    data class UpdateVeranstaltungResponse(
        val veranstaltung: Veranstaltung
    )

    /**
     * Executes the update event use case.
     *
     * @param request The request containing updated event data
     * @return ApiResponse with the updated event or error information
     */
    suspend fun execute(request: UpdateVeranstaltungRequest): ApiResponse<UpdateVeranstaltungResponse> {
        return try {
            // Check if event exists
            val existingVeranstaltung = veranstaltungRepository.findById(request.veranstaltungId)
            if (existingVeranstaltung == null) {
                return ApiResponse(
                    success = false,
                    error = ErrorDto(
                        code = "NOT_FOUND",
                        message = "Event not found"
                    )
                )
            }

            // Validate the request
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

            // Create updated domain object
            val updatedVeranstaltung = existingVeranstaltung.copy(
                name = request.name.trim(),
                beschreibung = request.beschreibung?.trim(),
                startDatum = request.startDatum,
                endDatum = request.endDatum,
                ort = request.ort.trim(),
                veranstalterVereinId = request.veranstalterVereinId,
                sparten = request.sparten,
                istAktiv = request.istAktiv,
                istOeffentlich = request.istOeffentlich,
                maxTeilnehmer = request.maxTeilnehmer,
                anmeldeschluss = request.anmeldeschluss,
                updatedAt = Clock.System.now()
            )

            // Validate the domain object
            val domainValidationErrors = updatedVeranstaltung.validate()
            if (domainValidationErrors.isNotEmpty()) {
                return ApiResponse(
                    success = false,
                    error = ErrorDto(
                        code = "DOMAIN_VALIDATION_ERROR",
                        message = "Domain validation failed",
                        details = domainValidationErrors.mapIndexed { index, error ->
                            "error_$index" to error
                        }.toMap()
                    )
                )
            }

            // Save the updated event
            val savedVeranstaltung = veranstaltungRepository.save(updatedVeranstaltung)

            ApiResponse(
                success = true,
                data = UpdateVeranstaltungResponse(savedVeranstaltung)
            )

        } catch (e: Exception) {
            ApiResponse(
                success = false,
                error = ErrorDto(
                    code = "INTERNAL_ERROR",
                    message = "Failed to update event: ${e.message}"
                )
            )
        }
    }

    /**
     * Validates the update event request.
     */
    private fun validateRequest(request: UpdateVeranstaltungRequest): ValidationResult {
        val errors = mutableListOf<ValidationError>()

        // Validate name
        if (request.name.isBlank()) {
            errors.add(ValidationError("name", "Event name is required"))
        } else if (request.name.length > 255) {
            errors.add(ValidationError("name", "Event name must not exceed 255 characters"))
        }

        // Validate location
        if (request.ort.isBlank()) {
            errors.add(ValidationError("ort", "Event location is required"))
        } else if (request.ort.length > 255) {
            errors.add(ValidationError("ort", "Event location must not exceed 255 characters"))
        }

        // Validate dates
        if (request.endDatum < request.startDatum) {
            errors.add(ValidationError("endDatum", "End date cannot be before start date"))
        }

        // Validate registration deadline
        request.anmeldeschluss?.let { deadline ->
            if (deadline > request.startDatum) {
                errors.add(ValidationError("anmeldeschluss", "Registration deadline cannot be after event start date"))
            }
        }

        // Validate max participants
        request.maxTeilnehmer?.let { max ->
            if (max <= 0) {
                errors.add(ValidationError("maxTeilnehmer", "Maximum participants must be positive"))
            }
        }

        // Validate description length
        request.beschreibung?.let { desc ->
            if (desc.length > 5000) {
                errors.add(ValidationError("beschreibung", "Description must not exceed 5000 characters"))
            }
        }

        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
}

package at.mocode.events.application.usecase

import at.mocode.dto.base.ApiResponse
import at.mocode.dto.base.ErrorDto
import at.mocode.events.domain.model.Veranstaltung
import at.mocode.events.domain.repository.VeranstaltungRepository
import at.mocode.enums.SparteE
import at.mocode.validation.ValidationResult
import at.mocode.validation.ValidationError
import com.benasher44.uuid.Uuid
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate

/**
 * Use case for creating new events (Veranstaltung).
 *
 * This use case handles the business logic for creating events,
 * including validation and persistence.
 */
class CreateVeranstaltungUseCase(
    private val veranstaltungRepository: VeranstaltungRepository
) {

    /**
     * Request data for creating a new event.
     */
    data class CreateVeranstaltungRequest(
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
     * Response data containing the created event.
     */
    data class CreateVeranstaltungResponse(
        val veranstaltung: Veranstaltung
    )

    /**
     * Executes the create event use case.
     *
     * @param request The request containing event data
     * @return ApiResponse with the created event or error information
     */
    suspend fun execute(request: CreateVeranstaltungRequest): ApiResponse<CreateVeranstaltungResponse> {
        return try {
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

            // Create the domain object
            val veranstaltung = Veranstaltung(
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
                createdAt = Clock.System.now(),
                updatedAt = Clock.System.now()
            )

            // Validate the domain object
            val domainValidationErrors = veranstaltung.validate()
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

            // Save the event
            val savedVeranstaltung = veranstaltungRepository.save(veranstaltung)

            ApiResponse(
                success = true,
                data = CreateVeranstaltungResponse(savedVeranstaltung)
            )

        } catch (e: Exception) {
            ApiResponse(
                success = false,
                error = ErrorDto(
                    code = "INTERNAL_ERROR",
                    message = "Failed to create event: ${e.message}"
                )
            )
        }
    }

    /**
     * Validates the create event request.
     */
    private fun validateRequest(request: CreateVeranstaltungRequest): ValidationResult {
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

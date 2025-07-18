package at.mocode.members.application.usecase

import at.mocode.dto.base.ApiResponse
import at.mocode.dto.base.ErrorDto
import at.mocode.enums.BerechtigungE
import at.mocode.members.domain.model.DomBerechtigung
import at.mocode.members.domain.repository.BerechtigungRepository
import at.mocode.validation.ValidationUtils
import at.mocode.validation.ValidationResult
import at.mocode.validation.ValidationError
import kotlinx.datetime.Clock

/**
 * Use case for creating new permissions (Berechtigungen) in the system.
 */
class CreateBerechtigungUseCase(
    private val berechtigungRepository: BerechtigungRepository
) {

    data class CreateBerechtigungRequest(
        val berechtigungTyp: BerechtigungE,
        val name: String,
        val beschreibung: String? = null,
        val ressource: String,
        val aktion: String,
        val istSystemBerechtigung: Boolean = false
    )

    data class CreateBerechtigungResponse(
        val berechtigung: DomBerechtigung
    )

    suspend fun execute(request: CreateBerechtigungRequest): ApiResponse<CreateBerechtigungResponse> {
        try {
            // Validate request
            val validationResult = validateRequest(request)
            if (!validationResult.isValid()) {
                val errors = (validationResult as ValidationResult.Invalid).errors
                return ApiResponse(
                    success = false,
                    error = ErrorDto(
                        code = "VALIDATION_ERROR",
                        message = "Validation failed",
                        details = errors.associate { it.field to it.message }
                    )
                )
            }

            // Check if permission with this type already exists
            val existingBerechtigung = berechtigungRepository.findByTyp(request.berechtigungTyp)
            if (existingBerechtigung != null) {
                return ApiResponse(
                    success = false,
                    error = ErrorDto(
                        code = "BERECHTIGUNG_ALREADY_EXISTS",
                        message = "A permission with this type already exists",
                        details = mapOf("berechtigungTyp" to request.berechtigungTyp.toString())
                    )
                )
            }

            // Create new permission
            val berechtigung = DomBerechtigung(
                berechtigungTyp = request.berechtigungTyp,
                name = request.name,
                beschreibung = request.beschreibung,
                ressource = request.ressource,
                aktion = request.aktion,
                istSystemBerechtigung = request.istSystemBerechtigung,
                createdAt = Clock.System.now(),
                updatedAt = Clock.System.now()
            )

            // Save to repository
            val savedBerechtigung = berechtigungRepository.save(berechtigung)

            return ApiResponse(
                success = true,
                data = CreateBerechtigungResponse(savedBerechtigung)
            )
        } catch (e: Exception) {
            return ApiResponse(
                success = false,
                error = ErrorDto(
                    code = "INTERNAL_ERROR",
                    message = "An error occurred while creating the permission",
                    details = mapOf("error" to e.message.orEmpty())
                )
            )
        }
    }

    private fun validateRequest(request: CreateBerechtigungRequest): ValidationResult {
        val errors = mutableListOf<ValidationError>()

        // Validate name
        ValidationUtils.validateNotBlank(request.name, "name")?.let { error ->
            errors.add(error)
        }

        // Validate ressource
        ValidationUtils.validateNotBlank(request.ressource, "ressource")?.let { error ->
            errors.add(error)
        }

        // Validate aktion
        ValidationUtils.validateNotBlank(request.aktion, "aktion")?.let { error ->
            errors.add(error)
        }

        // Validate name length
        if (request.name.length > 100) {
            errors.add(ValidationError("name", "Name must not exceed 100 characters"))
        }

        // Validate ressource length
        if (request.ressource.length > 50) {
            errors.add(ValidationError("ressource", "Ressource must not exceed 50 characters"))
        }

        // Validate aktion length
        if (request.aktion.length > 50) {
            errors.add(ValidationError("aktion", "Aktion must not exceed 50 characters"))
        }

        return ValidationResult(errors)
    }
}

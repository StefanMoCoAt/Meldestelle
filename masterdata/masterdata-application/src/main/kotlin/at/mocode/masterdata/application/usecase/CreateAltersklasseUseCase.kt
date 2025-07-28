package at.mocode.masterdata.application.usecase

import at.mocode.core.domain.model.SparteE
import at.mocode.masterdata.domain.model.AltersklasseDefinition
import at.mocode.masterdata.domain.repository.AltersklasseRepository
import at.mocode.core.utils.validation.ValidationResult
import at.mocode.core.utils.validation.ValidationError
import com.benasher44.uuid.Uuid
import kotlinx.datetime.Clock

/**
 * Use case for creating and updating age class information.
 *
 * This use case encapsulates the business logic for age class management
 * including validation, duplicate checking, and persistence.
 */
class CreateAltersklasseUseCase(
    private val altersklasseRepository: AltersklasseRepository
) {

    /**
     * Request data for creating a new age class.
     */
    data class CreateAltersklasseRequest(
        val altersklasseCode: String,
        val bezeichnung: String,
        val minAlter: Int? = null,
        val maxAlter: Int? = null,
        val stichtagRegelText: String? = "31.12. des laufenden Kalenderjahres",
        val sparteFilter: SparteE? = null,
        val geschlechtFilter: Char? = null,
        val oetoRegelReferenzId: Uuid? = null,
        val istAktiv: Boolean = true
    )

    /**
     * Request data for updating an existing age class.
     */
    data class UpdateAltersklasseRequest(
        val altersklasseId: Uuid,
        val altersklasseCode: String,
        val bezeichnung: String,
        val minAlter: Int? = null,
        val maxAlter: Int? = null,
        val stichtagRegelText: String? = "31.12. des laufenden Kalenderjahres",
        val sparteFilter: SparteE? = null,
        val geschlechtFilter: Char? = null,
        val oetoRegelReferenzId: Uuid? = null,
        val istAktiv: Boolean = true
    )

    /**
     * Response data for age class creation.
     */
    data class CreateAltersklasseResponse(
        val altersklasse: AltersklasseDefinition?,
        val success: Boolean,
        val errors: List<String> = emptyList()
    )

    /**
     * Response data for age class update.
     */
    data class UpdateAltersklasseResponse(
        val altersklasse: AltersklasseDefinition?,
        val success: Boolean,
        val errors: List<String> = emptyList()
    )

    /**
     * Response data for age class deletion.
     */
    data class DeleteAltersklasseResponse(
        val success: Boolean,
        val errors: List<String> = emptyList()
    )

    /**
     * Creates a new age class after validation.
     *
     * @param request The age class creation request
     * @return CreateAltersklasseResponse with the created age class or validation errors
     */
    suspend fun createAltersklasse(request: CreateAltersklasseRequest): CreateAltersklasseResponse {
        // Validate the request
        val validationResult = validateCreateRequest(request)
        if (!validationResult.isValid()) {
            val errors = (validationResult as ValidationResult.Invalid).errors.map { it.message }
            return CreateAltersklasseResponse(
                altersklasse = null,
                success = false,
                errors = errors
            )
        }

        // Check for duplicates
        val duplicateCheck = checkForDuplicates(request.altersklasseCode)
        if (!duplicateCheck.isValid()) {
            val errors = (duplicateCheck as ValidationResult.Invalid).errors.map { it.message }
            return CreateAltersklasseResponse(
                altersklasse = null,
                success = false,
                errors = errors
            )
        }

        // Create the domain object
        val now = Clock.System.now()
        val altersklasse = AltersklasseDefinition(
            altersklasseCode = request.altersklasseCode.trim().uppercase(),
            bezeichnung = request.bezeichnung.trim(),
            minAlter = request.minAlter,
            maxAlter = request.maxAlter,
            stichtagRegelText = request.stichtagRegelText?.trim(),
            sparteFilter = request.sparteFilter,
            geschlechtFilter = request.geschlechtFilter,
            oetoRegelReferenzId = request.oetoRegelReferenzId,
            istAktiv = request.istAktiv,
            createdAt = now,
            updatedAt = now
        )

        // Save to repository
        val savedAltersklasse = altersklasseRepository.save(altersklasse)
        return CreateAltersklasseResponse(
            altersklasse = savedAltersklasse,
            success = true
        )
    }

    /**
     * Updates an existing age class after validation.
     *
     * @param request The age class update request
     * @return UpdateAltersklasseResponse containing the updated age class or validation errors
     */
    suspend fun updateAltersklasse(request: UpdateAltersklasseRequest): UpdateAltersklasseResponse {
        // Check if age class exists
        val existingAltersklasse = altersklasseRepository.findById(request.altersklasseId)
        if (existingAltersklasse == null) {
            return UpdateAltersklasseResponse(
                altersklasse = null,
                success = false,
                errors = listOf("Age class with ID ${request.altersklasseId} not found")
            )
        }

        // Validate the request
        val validationResult = validateUpdateRequest(request)
        if (!validationResult.isValid()) {
            val errors = (validationResult as ValidationResult.Invalid).errors.map { it.message }
            return UpdateAltersklasseResponse(
                altersklasse = null,
                success = false,
                errors = errors
            )
        }

        // Check for duplicates (excluding current age class)
        val duplicateCheck = checkForDuplicatesExcluding(request.altersklasseCode, request.altersklasseId)
        if (!duplicateCheck.isValid()) {
            val errors = (duplicateCheck as ValidationResult.Invalid).errors.map { it.message }
            return UpdateAltersklasseResponse(
                altersklasse = null,
                success = false,
                errors = errors
            )
        }

        // Update the domain object
        val updatedAltersklasse = existingAltersklasse.copy(
            altersklasseCode = request.altersklasseCode.trim().uppercase(),
            bezeichnung = request.bezeichnung.trim(),
            minAlter = request.minAlter,
            maxAlter = request.maxAlter,
            stichtagRegelText = request.stichtagRegelText?.trim(),
            sparteFilter = request.sparteFilter,
            geschlechtFilter = request.geschlechtFilter,
            oetoRegelReferenzId = request.oetoRegelReferenzId,
            istAktiv = request.istAktiv,
            updatedAt = Clock.System.now()
        )

        // Save to repository
        val savedAltersklasse = altersklasseRepository.save(updatedAltersklasse)
        return UpdateAltersklasseResponse(
            altersklasse = savedAltersklasse,
            success = true
        )
    }

    /**
     * Deletes an age class by ID.
     *
     * @param altersklasseId The unique identifier of the age class to delete
     * @return DeleteAltersklasseResponse indicating success or failure
     */
    suspend fun deleteAltersklasse(altersklasseId: Uuid): DeleteAltersklasseResponse {
        val deleted = altersklasseRepository.delete(altersklasseId)
        return if (deleted) {
            DeleteAltersklasseResponse(success = true)
        } else {
            DeleteAltersklasseResponse(
                success = false,
                errors = listOf("Age class with ID $altersklasseId not found or could not be deleted")
            )
        }
    }

    /**
     * Validates a create age class request.
     */
    private fun validateCreateRequest(request: CreateAltersklasseRequest): ValidationResult {
        val errors = mutableListOf<ValidationError>()

        // Age class code validation
        if (request.altersklasseCode.isBlank()) {
            errors.add(ValidationError("altersklasseCode", "Age class code is required", "REQUIRED")) // "REQUIRED"
        } else if (request.altersklasseCode.length > 50) {
            errors.add(ValidationError("altersklasseCode", "Age class code must not exceed 50 characters", "MAX_LENGTH"))
        } else if (!request.altersklasseCode.matches(Regex("^[A-Z0-9_]+$"))) {
            errors.add(ValidationError("altersklasseCode", "Age class code must contain only uppercase letters, numbers, and underscores", "INVALID_FORMAT"))
        }

        // Bezeichnung validation
        if (request.bezeichnung.isBlank()) {
            errors.add(ValidationError("bezeichnung", "Bezeichnung is required", "REQUIRED"))
        } else if (request.bezeichnung.length > 200) {
            errors.add(ValidationError("bezeichnung", "Bezeichnung must not exceed 200 characters", "MAX_LENGTH"))
        }

        // Age range validation
        request.minAlter?.let { min ->
            if (min < 0) {
                errors.add(ValidationError("minAlter", "Minimum age must be non-negative", "INVALID_VALUE"))
            }
        }

        request.maxAlter?.let { max ->
            if (max < 0) {
                errors.add(ValidationError("maxAlter", "Maximum age must be non-negative", "INVALID_VALUE"))
            }
            request.minAlter?.let { min ->
                if (max < min) {
                    errors.add(ValidationError("maxAlter", "Maximum age must be greater than or equal to minimum age", "INVALID_RANGE"))
                }
            }
        }

        // Stichtag regel text validation
        request.stichtagRegelText?.let { text ->
            if (text.length > 500) {
                errors.add(ValidationError("stichtagRegelText", "Stichtag regel text must not exceed 500 characters", "MAX_LENGTH"))
            }
        }

        // Gender filter validation
        request.geschlechtFilter?.let { gender ->
            if (gender != 'M' && gender != 'W') {
                errors.add(ValidationError("geschlechtFilter", "Gender filter must be 'M' or 'W'", "INVALID_VALUE"))
            }
        }

        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }

    /**
     * Validates an update age class request.
     */
    private fun validateUpdateRequest(request: UpdateAltersklasseRequest): ValidationResult {
        // Use the same validation logic as create request
        val createRequest = CreateAltersklasseRequest(
            altersklasseCode = request.altersklasseCode,
            bezeichnung = request.bezeichnung,
            minAlter = request.minAlter,
            maxAlter = request.maxAlter,
            stichtagRegelText = request.stichtagRegelText,
            sparteFilter = request.sparteFilter,
            geschlechtFilter = request.geschlechtFilter,
            oetoRegelReferenzId = request.oetoRegelReferenzId,
            istAktiv = request.istAktiv
        )
        return validateCreateRequest(createRequest)
    }

    /**
     * Checks for duplicate age class codes.
     */
    private suspend fun checkForDuplicates(altersklasseCode: String): ValidationResult {
        val errors = mutableListOf<ValidationError>()

        if (altersklasseRepository.existsByCode(altersklasseCode.trim().uppercase())) {
            errors.add(ValidationError("altersklasseCode", "Age class with code '${altersklasseCode.uppercase()}' already exists", "DUPLICATE"))
        }

        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }

    /**
     * Checks for duplicate age class codes excluding a specific age class ID.
     */
    private suspend fun checkForDuplicatesExcluding(altersklasseCode: String, excludeId: Uuid): ValidationResult {
        val errors = mutableListOf<ValidationError>()

        // Check code
        val existing = altersklasseRepository.findByCode(altersklasseCode.trim().uppercase())
        if (existing != null && existing.altersklasseId != excludeId) {
            errors.add(ValidationError("altersklasseCode", "Age class with code '${altersklasseCode.uppercase()}' already exists", "DUPLICATE"))
        }

        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }

    /**
     * Validates age eligibility for a specific age class and participant.
     * This is a business logic method that can be used by other parts of the application.
     *
     * @param altersklasseId The age class ID
     * @param participantAge The participant's age
     * @param participantGender The participant's gender ('M', 'W')
     * @param participantSparte The participant's sport type
     * @return ValidationResult indicating eligibility or reasons for ineligibility
     */
    suspend fun validateEligibility(
        altersklasseId: Uuid,
        participantAge: Int,
        participantGender: Char,
        participantSparte: SparteE
    ): ValidationResult {
        val errors = mutableListOf<ValidationError>()

        // Get the age class
        val altersklasse = altersklasseRepository.findById(altersklasseId)
        if (altersklasse == null) {
            errors.add(ValidationError("altersklasseId", "Age class not found", "NOT_FOUND"))
            return ValidationResult.Invalid(errors)
        }

        // Check if age class is active
        if (!altersklasse.istAktiv) {
            errors.add(ValidationError("altersklasse", "Age class is not active", "INACTIVE"))
        }

        // Check age eligibility
        altersklasse.minAlter?.let { min ->
            if (participantAge < min) {
                errors.add(ValidationError("age", "Participant is too young for this age class (minimum age: $min)", "AGE_TOO_LOW"))
            }
        }

        altersklasse.maxAlter?.let { max ->
            if (participantAge > max) {
                errors.add(ValidationError("age", "Participant is too old for this age class (maximum age: $max)", "AGE_TOO_HIGH"))
            }
        }

        // Check gender eligibility
        altersklasse.geschlechtFilter?.let { requiredGender ->
            if (participantGender != requiredGender) {
                val genderName = if (requiredGender == 'M') "male" else "female"
                errors.add(ValidationError("gender", "This age class is only for $genderName participants", "GENDER_MISMATCH"))
            }
        }

        // Check sport eligibility
        altersklasse.sparteFilter?.let { requiredSparte ->
            if (participantSparte != requiredSparte) {
                errors.add(ValidationError("sparte", "This age class is only for ${requiredSparte.name} sport", "SPORT_MISMATCH"))
            }
        }

        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
}

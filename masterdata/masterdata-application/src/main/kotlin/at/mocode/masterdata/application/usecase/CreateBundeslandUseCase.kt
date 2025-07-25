package at.mocode.masterdata.application.usecase

import at.mocode.masterdata.domain.model.BundeslandDefinition
import at.mocode.masterdata.domain.repository.BundeslandRepository
import at.mocode.core.utils.validation.ValidationResult
import at.mocode.core.utils.validation.ValidationError
import com.benasher44.uuid.Uuid
import kotlinx.datetime.Clock

/**
 * Use case for creating and updating federal state information.
 *
 * This use case encapsulates the business logic for federal state management
 * including validation, duplicate checking, and persistence.
 */
class CreateBundeslandUseCase(
    private val bundeslandRepository: BundeslandRepository
) {

    /**
     * Request data for creating a new federal state.
     */
    data class CreateBundeslandRequest(
        val landId: Uuid,
        val oepsCode: String? = null,
        val iso3166_2_Code: String? = null,
        val name: String,
        val kuerzel: String? = null,
        val wappenUrl: String? = null,
        val istAktiv: Boolean = true,
        val sortierReihenfolge: Int? = null
    )

    /**
     * Request data for updating an existing federal state.
     */
    data class UpdateBundeslandRequest(
        val bundeslandId: Uuid,
        val landId: Uuid,
        val oepsCode: String? = null,
        val iso3166_2_Code: String? = null,
        val name: String,
        val kuerzel: String? = null,
        val wappenUrl: String? = null,
        val istAktiv: Boolean = true,
        val sortierReihenfolge: Int? = null
    )

    /**
     * Response data for federal state creation.
     */
    data class CreateBundeslandResponse(
        val bundesland: BundeslandDefinition?,
        val success: Boolean,
        val errors: List<String> = emptyList()
    )

    /**
     * Response data for federal state update.
     */
    data class UpdateBundeslandResponse(
        val bundesland: BundeslandDefinition?,
        val success: Boolean,
        val errors: List<String> = emptyList()
    )

    /**
     * Response data for federal state deletion.
     */
    data class DeleteBundeslandResponse(
        val success: Boolean,
        val errors: List<String> = emptyList()
    )

    /**
     * Creates a new federal state after validation.
     *
     * @param request The federal state creation request
     * @return CreateBundeslandResponse with the created federal state or validation errors
     */
    suspend fun createBundesland(request: CreateBundeslandRequest): CreateBundeslandResponse {
        // Validate the request
        val validationResult = validateCreateRequest(request)
        if (!validationResult.isValid()) {
            val errors = (validationResult as ValidationResult.Invalid).errors.map { it.message }
            return CreateBundeslandResponse(
                bundesland = null,
                success = false,
                errors = errors
            )
        }

        // Check for duplicates
        val duplicateCheck = checkForDuplicates(request.oepsCode, request.iso3166_2_Code, request.landId)
        if (!duplicateCheck.isValid()) {
            val errors = (duplicateCheck as ValidationResult.Invalid).errors.map { it.message }
            return CreateBundeslandResponse(
                bundesland = null,
                success = false,
                errors = errors
            )
        }

        // Create the domain object
        val now = Clock.System.now()
        val bundesland = BundeslandDefinition(
            landId = request.landId,
            oepsCode = request.oepsCode?.trim(),
            iso3166_2_Code = request.iso3166_2_Code?.trim()?.uppercase(),
            name = request.name.trim(),
            kuerzel = request.kuerzel?.trim(),
            wappenUrl = request.wappenUrl?.trim(),
            istAktiv = request.istAktiv,
            sortierReihenfolge = request.sortierReihenfolge,
            createdAt = now,
            updatedAt = now
        )

        // Save to repository
        val savedBundesland = bundeslandRepository.save(bundesland)
        return CreateBundeslandResponse(
            bundesland = savedBundesland,
            success = true
        )
    }

    /**
     * Updates an existing federal state after validation.
     *
     * @param request The federal state update request
     * @return UpdateBundeslandResponse containing the updated federal state or validation errors
     */
    suspend fun updateBundesland(request: UpdateBundeslandRequest): UpdateBundeslandResponse {
        // Check if federal state exists
        val existingBundesland = bundeslandRepository.findById(request.bundeslandId)
        if (existingBundesland == null) {
            return UpdateBundeslandResponse(
                bundesland = null,
                success = false,
                errors = listOf("Federal state with ID ${request.bundeslandId} not found")
            )
        }

        // Validate the request
        val validationResult = validateUpdateRequest(request)
        if (!validationResult.isValid()) {
            val errors = (validationResult as ValidationResult.Invalid).errors.map { it.message }
            return UpdateBundeslandResponse(
                bundesland = null,
                success = false,
                errors = errors
            )
        }

        // Check for duplicates (excluding current federal state)
        val duplicateCheck = checkForDuplicatesExcluding(
            request.oepsCode,
            request.iso3166_2_Code,
            request.landId,
            request.bundeslandId
        )
        if (!duplicateCheck.isValid()) {
            val errors = (duplicateCheck as ValidationResult.Invalid).errors.map { it.message }
            return UpdateBundeslandResponse(
                bundesland = null,
                success = false,
                errors = errors
            )
        }

        // Update the domain object
        val updatedBundesland = existingBundesland.copy(
            landId = request.landId,
            oepsCode = request.oepsCode?.trim(),
            iso3166_2_Code = request.iso3166_2_Code?.trim()?.uppercase(),
            name = request.name.trim(),
            kuerzel = request.kuerzel?.trim(),
            wappenUrl = request.wappenUrl?.trim(),
            istAktiv = request.istAktiv,
            sortierReihenfolge = request.sortierReihenfolge,
            updatedAt = Clock.System.now()
        )

        // Save to repository
        val savedBundesland = bundeslandRepository.save(updatedBundesland)
        return UpdateBundeslandResponse(
            bundesland = savedBundesland,
            success = true
        )
    }

    /**
     * Deletes a federal state by ID.
     *
     * @param bundeslandId The unique identifier of the federal state to delete
     * @return DeleteBundeslandResponse indicating success or failure
     */
    suspend fun deleteBundesland(bundeslandId: Uuid): DeleteBundeslandResponse {
        val deleted = bundeslandRepository.delete(bundeslandId)
        return if (deleted) {
            DeleteBundeslandResponse(success = true)
        } else {
            DeleteBundeslandResponse(
                success = false,
                errors = listOf("Federal state with ID $bundeslandId not found or could not be deleted")
            )
        }
    }

    /**
     * Validates a create federal state request.
     */
    private fun validateCreateRequest(request: CreateBundeslandRequest): ValidationResult {
        val errors = mutableListOf<ValidationError>()

        // Name validation
        if (request.name.isBlank()) {
            errors.add(ValidationError("name", "Name is required", "REQUIRED"))
        } else if (request.name.length > 100) {
            errors.add(ValidationError("name", "Name must not exceed 100 characters", "MAX_LENGTH"))
        }

        // OEPS code validation
        request.oepsCode?.let { code ->
            if (code.isBlank()) {
                errors.add(ValidationError("oepsCode", "OEPS code cannot be empty if provided", "INVALID_FORMAT"))
            } else if (code.length > 10) {
                errors.add(ValidationError("oepsCode", "OEPS code must not exceed 10 characters", "MAX_LENGTH"))
            }
        }

        // ISO 3166-2 code validation
        request.iso3166_2_Code?.let { code ->
            if (code.isBlank()) {
                errors.add(ValidationError("iso3166_2_Code", "ISO 3166-2 code cannot be empty if provided", "INVALID_FORMAT"))
            } else if (code.length > 10) {
                errors.add(ValidationError("iso3166_2_Code", "ISO 3166-2 code must not exceed 10 characters", "MAX_LENGTH"))
            }
        }

        // Kuerzel validation
        request.kuerzel?.let { kuerzel ->
            if (kuerzel.length > 10) {
                errors.add(ValidationError("kuerzel", "Kuerzel must not exceed 10 characters", "MAX_LENGTH"))
            }
        }

        // Sorting order validation
        request.sortierReihenfolge?.let { order ->
            if (order < 0) {
                errors.add(ValidationError("sortierReihenfolge", "Sorting order must be non-negative", "INVALID_VALUE"))
            }
        }

        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }

    /**
     * Validates an update federal state request.
     */
    private fun validateUpdateRequest(request: UpdateBundeslandRequest): ValidationResult {
        // Use the same validation logic as create request
        val createRequest = CreateBundeslandRequest(
            landId = request.landId,
            oepsCode = request.oepsCode,
            iso3166_2_Code = request.iso3166_2_Code,
            name = request.name,
            kuerzel = request.kuerzel,
            wappenUrl = request.wappenUrl,
            istAktiv = request.istAktiv,
            sortierReihenfolge = request.sortierReihenfolge
        )
        return validateCreateRequest(createRequest)
    }

    /**
     * Checks for duplicate codes.
     */
    private suspend fun checkForDuplicates(oepsCode: String?, iso3166_2_Code: String?, landId: Uuid): ValidationResult {
        val errors = mutableListOf<ValidationError>()

        oepsCode?.let { code ->
            if (bundeslandRepository.existsByOepsCode(code.trim(), landId)) {
                errors.add(ValidationError("oepsCode", "Federal state with OEPS code '$code' already exists for this country", "DUPLICATE"))
            }
        }

        iso3166_2_Code?.let { code ->
            if (bundeslandRepository.existsByIso3166_2_Code(code.trim().uppercase())) {
                errors.add(ValidationError("iso3166_2_Code", "Federal state with ISO 3166-2 code '${code.uppercase()}' already exists", "DUPLICATE"))
            }
        }

        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }

    /**
     * Checks for duplicate codes excluding a specific federal state ID.
     */
    private suspend fun checkForDuplicatesExcluding(
        oepsCode: String?,
        iso3166_2_Code: String?,
        landId: Uuid,
        excludeId: Uuid
    ): ValidationResult {
        val errors = mutableListOf<ValidationError>()

        // Check OEPS code
        oepsCode?.let { code ->
            val existing = bundeslandRepository.findByOepsCode(code.trim(), landId)
            if (existing != null && existing.bundeslandId != excludeId) {
                errors.add(ValidationError("oepsCode", "Federal state with OEPS code '$code' already exists for this country", "DUPLICATE"))
            }
        }

        // Check ISO 3166-2 code
        iso3166_2_Code?.let { code ->
            val existing = bundeslandRepository.findByIso3166_2_Code(code.trim().uppercase())
            if (existing != null && existing.bundeslandId != excludeId) {
                errors.add(ValidationError("iso3166_2_Code", "Federal state with ISO 3166-2 code '${code.uppercase()}' already exists", "DUPLICATE"))
            }
        }

        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
}

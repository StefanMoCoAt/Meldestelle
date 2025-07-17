package at.mocode.masterdata.application.usecase

import at.mocode.masterdata.domain.model.LandDefinition
import at.mocode.masterdata.domain.repository.LandRepository
import at.mocode.validation.ValidationResult
import com.benasher44.uuid.Uuid
import kotlinx.datetime.Clock

/**
 * Use case for creating and updating country information.
 *
 * This use case encapsulates the business logic for country management
 * including validation, duplicate checking, and persistence.
 */
class CreateCountryUseCase(
    private val landRepository: LandRepository
) {

    /**
     * Request data for creating a new country.
     */
    data class CreateCountryRequest(
        val isoAlpha2Code: String,
        val isoAlpha3Code: String,
        val isoNumerischerCode: String? = null,
        val nameDeutsch: String,
        val nameEnglisch: String? = null,
        val wappenUrl: String? = null,
        val istEuMitglied: Boolean? = null,
        val istEwrMitglied: Boolean? = null,
        val istAktiv: Boolean = true,
        val sortierReihenfolge: Int? = null
    )

    /**
     * Request data for updating an existing country.
     */
    data class UpdateCountryRequest(
        val landId: Uuid,
        val isoAlpha2Code: String,
        val isoAlpha3Code: String,
        val isoNumerischerCode: String? = null,
        val nameDeutsch: String,
        val nameEnglisch: String? = null,
        val wappenUrl: String? = null,
        val istEuMitglied: Boolean? = null,
        val istEwrMitglied: Boolean? = null,
        val istAktiv: Boolean = true,
        val sortierReihenfolge: Int? = null
    )

    /**
     * Creates a new country after validation.
     *
     * @param request The country creation request
     * @return ValidationResult containing the created country or validation errors
     */
    suspend fun createCountry(request: CreateCountryRequest): ValidationResult<LandDefinition> {
        // Validate the request
        val validationResult = validateCreateRequest(request)
        if (!validationResult.isValid) {
            return ValidationResult.failure(validationResult.errors)
        }

        // Check for duplicates
        val duplicateCheck = checkForDuplicates(request.isoAlpha2Code, request.isoAlpha3Code)
        if (!duplicateCheck.isValid) {
            return ValidationResult.failure(duplicateCheck.errors)
        }

        // Create the domain object
        val now = Clock.System.now()
        val country = LandDefinition(
            isoAlpha2Code = request.isoAlpha2Code.uppercase(),
            isoAlpha3Code = request.isoAlpha3Code.uppercase(),
            isoNumerischerCode = request.isoNumerischerCode,
            nameDeutsch = request.nameDeutsch.trim(),
            nameEnglisch = request.nameEnglisch?.trim(),
            wappenUrl = request.wappenUrl?.trim(),
            istEuMitglied = request.istEuMitglied,
            istEwrMitglied = request.istEwrMitglied,
            istAktiv = request.istAktiv,
            sortierReihenfolge = request.sortierReihenfolge,
            createdAt = now,
            updatedAt = now
        )

        // Save to repository
        val savedCountry = landRepository.save(country)
        return ValidationResult.success(savedCountry)
    }

    /**
     * Updates an existing country after validation.
     *
     * @param request The country update request
     * @return ValidationResult containing the updated country or validation errors
     */
    suspend fun updateCountry(request: UpdateCountryRequest): ValidationResult<LandDefinition> {
        // Check if country exists
        val existingCountry = landRepository.findById(request.landId)
            ?: return ValidationResult.failure(listOf("Country with ID ${request.landId} not found"))

        // Validate the request
        val validationResult = validateUpdateRequest(request)
        if (!validationResult.isValid) {
            return ValidationResult.failure(validationResult.errors)
        }

        // Check for duplicates (excluding current country)
        val duplicateCheck = checkForDuplicatesExcluding(
            request.isoAlpha2Code,
            request.isoAlpha3Code,
            request.landId
        )
        if (!duplicateCheck.isValid) {
            return ValidationResult.failure(duplicateCheck.errors)
        }

        // Update the domain object
        val updatedCountry = existingCountry.copy(
            isoAlpha2Code = request.isoAlpha2Code.uppercase(),
            isoAlpha3Code = request.isoAlpha3Code.uppercase(),
            isoNumerischerCode = request.isoNumerischerCode,
            nameDeutsch = request.nameDeutsch.trim(),
            nameEnglisch = request.nameEnglisch?.trim(),
            wappenUrl = request.wappenUrl?.trim(),
            istEuMitglied = request.istEuMitglied,
            istEwrMitglied = request.istEwrMitglied,
            istAktiv = request.istAktiv,
            sortierReihenfolge = request.sortierReihenfolge,
            updatedAt = Clock.System.now()
        )

        // Save to repository
        val savedCountry = landRepository.save(updatedCountry)
        return ValidationResult.success(savedCountry)
    }

    /**
     * Deletes a country by ID.
     *
     * @param countryId The unique identifier of the country to delete
     * @return ValidationResult indicating success or failure
     */
    suspend fun deleteCountry(countryId: Uuid): ValidationResult<Unit> {
        val deleted = landRepository.delete(countryId)
        return if (deleted) {
            ValidationResult.success(Unit)
        } else {
            ValidationResult.failure(listOf("Country with ID $countryId not found or could not be deleted"))
        }
    }

    /**
     * Validates a create country request.
     */
    private fun validateCreateRequest(request: CreateCountryRequest): ValidationResult<Unit> {
        val errors = mutableListOf<String>()

        // ISO Alpha-2 Code validation
        if (request.isoAlpha2Code.isBlank()) {
            errors.add("ISO Alpha-2 code is required")
        } else if (request.isoAlpha2Code.length != 2) {
            errors.add("ISO Alpha-2 code must be exactly 2 characters")
        } else if (!request.isoAlpha2Code.all { it.isLetter() }) {
            errors.add("ISO Alpha-2 code must contain only letters")
        }

        // ISO Alpha-3 Code validation
        if (request.isoAlpha3Code.isBlank()) {
            errors.add("ISO Alpha-3 code is required")
        } else if (request.isoAlpha3Code.length != 3) {
            errors.add("ISO Alpha-3 code must be exactly 3 characters")
        } else if (!request.isoAlpha3Code.all { it.isLetter() }) {
            errors.add("ISO Alpha-3 code must contain only letters")
        }

        // German name validation
        if (request.nameDeutsch.isBlank()) {
            errors.add("German name is required")
        } else if (request.nameDeutsch.length > 100) {
            errors.add("German name must not exceed 100 characters")
        }

        // English name validation
        request.nameEnglisch?.let { name ->
            if (name.length > 100) {
                errors.add("English name must not exceed 100 characters")
            }
        }

        // Sorting order validation
        request.sortierReihenfolge?.let { order ->
            if (order < 0) {
                errors.add("Sorting order must be non-negative")
            }
        }

        return if (errors.isEmpty()) {
            ValidationResult.success(Unit)
        } else {
            ValidationResult.failure(errors)
        }
    }

    /**
     * Validates an update country request.
     */
    private fun validateUpdateRequest(request: UpdateCountryRequest): ValidationResult<Unit> {
        // Use the same validation logic as create request
        val createRequest = CreateCountryRequest(
            isoAlpha2Code = request.isoAlpha2Code,
            isoAlpha3Code = request.isoAlpha3Code,
            isoNumerischerCode = request.isoNumerischerCode,
            nameDeutsch = request.nameDeutsch,
            nameEnglisch = request.nameEnglisch,
            wappenUrl = request.wappenUrl,
            istEuMitglied = request.istEuMitglied,
            istEwrMitglied = request.istEwrMitglied,
            istAktiv = request.istAktiv,
            sortierReihenfolge = request.sortierReihenfolge
        )
        return validateCreateRequest(createRequest)
    }

    /**
     * Checks for duplicate ISO codes.
     */
    private suspend fun checkForDuplicates(isoAlpha2Code: String, isoAlpha3Code: String): ValidationResult<Unit> {
        val errors = mutableListOf<String>()

        if (landRepository.existsByIsoAlpha2Code(isoAlpha2Code.uppercase())) {
            errors.add("Country with ISO Alpha-2 code '${isoAlpha2Code.uppercase()}' already exists")
        }

        if (landRepository.existsByIsoAlpha3Code(isoAlpha3Code.uppercase())) {
            errors.add("Country with ISO Alpha-3 code '${isoAlpha3Code.uppercase()}' already exists")
        }

        return if (errors.isEmpty()) {
            ValidationResult.success(Unit)
        } else {
            ValidationResult.failure(errors)
        }
    }

    /**
     * Checks for duplicate ISO codes excluding a specific country ID.
     */
    private suspend fun checkForDuplicatesExcluding(
        isoAlpha2Code: String,
        isoAlpha3Code: String,
        excludeId: Uuid
    ): ValidationResult<Unit> {
        val errors = mutableListOf<String>()

        // Check Alpha-2 code
        val existingAlpha2 = landRepository.findByIsoAlpha2Code(isoAlpha2Code.uppercase())
        if (existingAlpha2 != null && existingAlpha2.landId != excludeId) {
            errors.add("Country with ISO Alpha-2 code '${isoAlpha2Code.uppercase()}' already exists")
        }

        // Check Alpha-3 code
        val existingAlpha3 = landRepository.findByIsoAlpha3Code(isoAlpha3Code.uppercase())
        if (existingAlpha3 != null && existingAlpha3.landId != excludeId) {
            errors.add("Country with ISO Alpha-3 code '${isoAlpha3Code.uppercase()}' already exists")
        }

        return if (errors.isEmpty()) {
            ValidationResult.success(Unit)
        } else {
            ValidationResult.failure(errors)
        }
    }
}

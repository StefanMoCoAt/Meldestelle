package at.mocode.masterdata.application.usecase

import at.mocode.core.domain.model.PlatzTypE
import at.mocode.masterdata.domain.model.Platz
import at.mocode.masterdata.domain.repository.PlatzRepository
import at.mocode.core.domain.model.ValidationResult
import at.mocode.core.domain.model.ValidationError
import com.benasher44.uuid.Uuid
import kotlinx.datetime.Clock

/**
 * Use case for creating and updating venue/arena information.
 *
 * This use case encapsulates the business logic for venue management
 * including validation, duplicate checking, and persistence.
 */
class CreatePlatzUseCase(
    private val platzRepository: PlatzRepository
) {

    /**
     * Request data for creating a new venue.
     */
    data class CreatePlatzRequest(
        val turnierId: Uuid,
        val name: String,
        val dimension: String? = null,
        val boden: String? = null,
        val typ: PlatzTypE,
        val istAktiv: Boolean = true,
        val sortierReihenfolge: Int? = null
    )

    /**
     * Request data for updating an existing venue.
     */
    data class UpdatePlatzRequest(
        val platzId: Uuid,
        val turnierId: Uuid,
        val name: String,
        val dimension: String? = null,
        val boden: String? = null,
        val typ: PlatzTypE,
        val istAktiv: Boolean = true,
        val sortierReihenfolge: Int? = null
    )

    /**
     * Response data for venue creation.
     */
    data class CreatePlatzResponse(
        val platz: Platz?,
        val success: Boolean,
        val errors: List<String> = emptyList()
    )

    /**
     * Response data for venue update.
     */
    data class UpdatePlatzResponse(
        val platz: Platz?,
        val success: Boolean,
        val errors: List<String> = emptyList()
    )

    /**
     * Response data for venue deletion.
     */
    data class DeletePlatzResponse(
        val success: Boolean,
        val errors: List<String> = emptyList()
    )

    /**
     * Creates a new venue after validation.
     *
     * @param request The venue creation request
     * @return CreatePlatzResponse with the created venue or validation errors
     */
    suspend fun createPlatz(request: CreatePlatzRequest): CreatePlatzResponse {
        // Validate the request
        val validationResult = validateCreateRequest(request)
        if (!validationResult.isValid()) {
            val errors = (validationResult as ValidationResult.Invalid).errors.map { it.message }
            return CreatePlatzResponse(
                platz = null,
                success = false,
                errors = errors
            )
        }

        // Check for duplicates
        val duplicateCheck = checkForDuplicates(request.name, request.turnierId)
        if (!duplicateCheck.isValid()) {
            val errors = (duplicateCheck as ValidationResult.Invalid).errors.map { it.message }
            return CreatePlatzResponse(
                platz = null,
                success = false,
                errors = errors
            )
        }

        // Create the domain object
        val now = Clock.System.now()
        val platz = Platz(
            turnierId = request.turnierId,
            name = request.name.trim(),
            dimension = request.dimension?.trim(),
            boden = request.boden?.trim(),
            typ = request.typ,
            istAktiv = request.istAktiv,
            sortierReihenfolge = request.sortierReihenfolge,
            createdAt = now,
            updatedAt = now
        )

        // Save to repository
        val savedPlatz = platzRepository.save(platz)
        return CreatePlatzResponse(
            platz = savedPlatz,
            success = true
        )
    }

    /**
     * Updates an existing venue after validation.
     *
     * @param request The venue update request
     * @return UpdatePlatzResponse containing the updated venue or validation errors
     */
    suspend fun updatePlatz(request: UpdatePlatzRequest): UpdatePlatzResponse {
        // Check if venue exists
        val existingPlatz = platzRepository.findById(request.platzId)
        if (existingPlatz == null) {
            return UpdatePlatzResponse(
                platz = null,
                success = false,
                errors = listOf("Venue with ID ${request.platzId} not found")
            )
        }

        // Validate the request
        val validationResult = validateUpdateRequest(request)
        if (!validationResult.isValid()) {
            val errors = (validationResult as ValidationResult.Invalid).errors.map { it.message }
            return UpdatePlatzResponse(
                platz = null,
                success = false,
                errors = errors
            )
        }

        // Check for duplicates (excluding current venue)
        val duplicateCheck = checkForDuplicatesExcluding(request.name, request.turnierId, request.platzId)
        if (!duplicateCheck.isValid()) {
            val errors = (duplicateCheck as ValidationResult.Invalid).errors.map { it.message }
            return UpdatePlatzResponse(
                platz = null,
                success = false,
                errors = errors
            )
        }

        // Update the domain object
        val updatedPlatz = existingPlatz.copy(
            turnierId = request.turnierId,
            name = request.name.trim(),
            dimension = request.dimension?.trim(),
            boden = request.boden?.trim(),
            typ = request.typ,
            istAktiv = request.istAktiv,
            sortierReihenfolge = request.sortierReihenfolge,
            updatedAt = Clock.System.now()
        )

        // Save to repository
        val savedPlatz = platzRepository.save(updatedPlatz)
        return UpdatePlatzResponse(
            platz = savedPlatz,
            success = true
        )
    }

    /**
     * Deletes a venue by ID.
     *
     * @param platzId The unique identifier of the venue to delete
     * @return DeletePlatzResponse indicating success or failure
     */
    suspend fun deletePlatz(platzId: Uuid): DeletePlatzResponse {
        val deleted = platzRepository.delete(platzId)
        return if (deleted) {
            DeletePlatzResponse(success = true)
        } else {
            DeletePlatzResponse(
                success = false,
                errors = listOf("Venue with ID $platzId not found or could not be deleted")
            )
        }
    }

    /**
     * Validates a create venue request.
     */
    private fun validateCreateRequest(request: CreatePlatzRequest): ValidationResult {
        val errors = mutableListOf<ValidationError>()

        // Name validation
        if (request.name.isBlank()) {
            errors.add(ValidationError("name", "Name is required", "REQUIRED"))
        } else if (request.name.length > 200) {
            errors.add(ValidationError("name", "Name must not exceed 200 characters", "MAX_LENGTH"))
        }

        // Dimension validation
        request.dimension?.let { dimension ->
            if (dimension.isBlank()) {
                errors.add(ValidationError("dimension", "Dimension cannot be empty if provided", "INVALID_FORMAT"))
            } else if (dimension.length > 50) {
                errors.add(ValidationError("dimension", "Dimension must not exceed 50 characters", "MAX_LENGTH"))
            } else if (!dimension.matches(Regex("^\\d+x\\d+m?$"))) {
                errors.add(ValidationError("dimension", "Dimension must be in format like '20x60m' or '20x40'", "INVALID_FORMAT"))
            }
        }

        // Ground type validation
        request.boden?.let { boden ->
            if (boden.isBlank()) {
                errors.add(ValidationError("boden", "Ground type cannot be empty if provided", "INVALID_FORMAT"))
            } else if (boden.length > 100) {
                errors.add(ValidationError("boden", "Ground type must not exceed 100 characters", "MAX_LENGTH"))
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
     * Validates an update venue request.
     */
    private fun validateUpdateRequest(request: UpdatePlatzRequest): ValidationResult {
        // Use the same validation logic as create request
        val createRequest = CreatePlatzRequest(
            turnierId = request.turnierId,
            name = request.name,
            dimension = request.dimension,
            boden = request.boden,
            typ = request.typ,
            istAktiv = request.istAktiv,
            sortierReihenfolge = request.sortierReihenfolge
        )
        return validateCreateRequest(createRequest)
    }

    /**
     * Checks for duplicate venue names within a tournament.
     */
    private suspend fun checkForDuplicates(name: String, turnierId: Uuid): ValidationResult {
        val errors = mutableListOf<ValidationError>()

        if (platzRepository.existsByNameAndTournament(name.trim(), turnierId)) {
            errors.add(ValidationError("name", "Venue with name '$name' already exists for this tournament", "DUPLICATE"))
        }

        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }

    /**
     * Checks for duplicate venue names excluding a specific venue ID.
     */
    private suspend fun checkForDuplicatesExcluding(name: String, turnierId: Uuid, excludeId: Uuid): ValidationResult {
        val errors = mutableListOf<ValidationError>()

        // Get all venues with the same name and tournament
        val existingVenues = platzRepository.findByName(name.trim(), turnierId, 10)
        val duplicateExists = existingVenues.any { it.id != excludeId }

        if (duplicateExists) {
            errors.add(ValidationError("name", "Venue with name '$name' already exists for this tournament", "DUPLICATE"))
        }

        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }

    /**
     * Validates venue configuration for specific discipline requirements.
     * This is a business logic method that can be used by other parts of the application.
     *
     * @param platzId The venue ID
     * @param requiredType The required venue type for the discipline
     * @param requiredDimensions Optional required dimensions
     * @param requiredGroundType Optional required ground type
     * @return ValidationResult indicating suitability or reasons for unsuitability
     */
    suspend fun validateVenueForDiscipline(
        platzId: Uuid,
        requiredType: PlatzTypE,
        requiredDimensions: String? = null,
        requiredGroundType: String? = null
    ): ValidationResult {
        val errors = mutableListOf<ValidationError>()

        // Get the venue
        val platz = platzRepository.findById(platzId)
        if (platz == null) {
            errors.add(ValidationError("platzId", "Venue not found", "NOT_FOUND"))
            return ValidationResult.Invalid(errors)
        }

        // Check if venue is active
        if (!platz.istAktiv) {
            errors.add(ValidationError("platz", "Venue is not active", "INACTIVE"))
        }

        // Check venue type
        if (platz.typ != requiredType) {
            errors.add(ValidationError("typ", "Venue type ${platz.typ} does not match required type $requiredType", "TYPE_MISMATCH"))
        }

        // Check dimensions if required
        requiredDimensions?.let { required ->
            if (platz.dimension != required.trim()) {
                errors.add(ValidationError("dimension", "Venue dimensions '${platz.dimension}' do not match required dimensions '$required'", "DIMENSION_MISMATCH"))
            }
        }

        // Check ground type if required
        requiredGroundType?.let { required ->
            if (platz.boden != required.trim()) {
                errors.add(ValidationError("boden", "Venue ground type '${platz.boden}' does not match required ground type '$required'", "GROUND_TYPE_MISMATCH"))
            }
        }

        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }

    /**
     * Creates multiple venues for a tournament in batch.
     * This is a convenience method for setting up tournament venues efficiently.
     *
     * @param turnierId The tournament ID
     * @param venueRequests List of venue creation requests
     * @return List of creation responses for each venue
     */
    suspend fun createMultipleVenues(turnierId: Uuid, venueRequests: List<CreatePlatzRequest>): List<CreatePlatzResponse> {
        val responses = mutableListOf<CreatePlatzResponse>()

        for (request in venueRequests) {
            // Ensure all requests are for the same tournament
            val adjustedRequest = request.copy(turnierId = turnierId)
            val response = createPlatz(adjustedRequest)
            responses.add(response)
        }

        return responses
    }

    /**
     * Validates venue capacity and setup for tournament requirements.
     * This method performs comprehensive checks for tournament venue setup.
     *
     * @param turnierId The tournament ID
     * @param requiredVenueTypes Map of venue type to minimum count required
     * @return ValidationResult indicating if the tournament has adequate venue setup
     */
    suspend fun validateTournamentVenueSetup(
        turnierId: Uuid,
        requiredVenueTypes: Map<PlatzTypE, Int>
    ): ValidationResult {
        val errors = mutableListOf<ValidationError>()

        // Get all active venues for the tournament
        val venues = platzRepository.findByTournament(turnierId, activeOnly = true, orderBySortierung = false)
        val venuesByType = venues.groupBy { it.typ }

        // Check if each required venue type has sufficient count
        for ((requiredType, requiredCount) in requiredVenueTypes) {
            val availableCount = venuesByType[requiredType]?.size ?: 0

            if (availableCount < requiredCount) {
                errors.add(ValidationError(
                    "venues",
                    "Tournament requires $requiredCount venues of type $requiredType but only has $availableCount",
                    "INSUFFICIENT_VENUES"
                ))
            }
        }

        // Check if tournament has any venues at all
        if (venues.isEmpty()) {
            errors.add(ValidationError("venues", "Tournament has no active venues configured", "NO_VENUES"))
        }

        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }

    /**
     * Optimizes venue sorting order for a tournament.
     * This method automatically assigns sorting orders based on venue type and name.
     *
     * @param turnierId The tournament ID
     * @return Number of venues updated
     */
    suspend fun optimizeVenueSorting(turnierId: Uuid): Int {
        val venues = platzRepository.findByTournament(turnierId, activeOnly = false, orderBySortierung = false)

        // Sort venues by type first, then by name
        val sortedVenues = venues.sortedWith(compareBy<Platz> { it.typ.ordinal }.thenBy { it.name })

        var updatedCount = 0

        // Assign new sorting orders
        sortedVenues.forEachIndexed { index, venue ->
            val newSortOrder = (index + 1) * 10 // Leave gaps for future insertions

            if (venue.sortierReihenfolge != newSortOrder) {
                val updatedVenue = venue.copy(
                    sortierReihenfolge = newSortOrder,
                    updatedAt = Clock.System.now()
                )
                platzRepository.save(updatedVenue)
                updatedCount++
            }
        }

        return updatedCount
    }
}

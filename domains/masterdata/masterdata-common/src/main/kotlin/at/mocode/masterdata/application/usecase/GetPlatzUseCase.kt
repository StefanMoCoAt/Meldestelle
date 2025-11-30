@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)
package at.mocode.masterdata.application.usecase

import at.mocode.core.domain.model.PlatzTypE
import at.mocode.masterdata.domain.model.Platz
import at.mocode.masterdata.domain.repository.PlatzRepository
import kotlin.uuid.Uuid

/**
 * Use case for retrieving venue/arena information.
 *
 * This use case encapsulates the business logic for fetching venue data
 * and provides a clean interface for the application layer.
 */
class GetPlatzUseCase(
    private val platzRepository: PlatzRepository
) {

    /**
     * Retrieves a venue by its unique ID.
     *
     * @param platzId The unique identifier of the venue
     * @return The venue if found, null otherwise
     */
    suspend fun getById(platzId: Uuid): Platz? {
        return platzRepository.findById(platzId)
    }

    /**
     * Retrieves all venues for a specific tournament.
     *
     * @param turnierId The tournament ID
     * @param activeOnly Whether to return only active venues (default: true)
     * @param orderBySortierung Whether to order by sortierReihenfolge field (default: true)
     * @return List of venues for the tournament
     */
    suspend fun getByTournament(turnierId: Uuid, activeOnly: Boolean = true, orderBySortierung: Boolean = true): List<Platz> {
        return platzRepository.findByTournament(turnierId, activeOnly, orderBySortierung)
    }

    /**
     * Searches for venues by name (partial match).
     *
     * @param searchTerm The search term to match against venue names
     * @param turnierId Optional tournament ID to limit search
     * @param limit Maximum number of results to return (default: 50)
     * @return List of matching venues
     */
    suspend fun searchByName(searchTerm: String, turnierId: Uuid? = null, limit: Int = 50): List<Platz> {
        require(searchTerm.isNotBlank()) { "Search term cannot be blank" }
        require(limit > 0) { "Limit must be positive" }
        return platzRepository.findByName(searchTerm.trim(), turnierId, limit)
    }

    /**
     * Retrieves venues by type.
     *
     * @param typ The venue type
     * @param turnierId Optional tournament ID to limit search
     * @param activeOnly Whether to return only active venues (default: true)
     * @return List of venues of the specified type
     */
    suspend fun getByType(typ: PlatzTypE, turnierId: Uuid? = null, activeOnly: Boolean = true): List<Platz> {
        return platzRepository.findByType(typ, turnierId, activeOnly)
    }

    /**
     * Retrieves venues by ground type.
     *
     * @param boden The ground type (e.g., "Sand", "Gras", "Kunststoff")
     * @param turnierId Optional tournament ID to limit search
     * @param activeOnly Whether to return only active venues (default: true)
     * @return List of venues with the specified ground type
     */
    suspend fun getByGroundType(boden: String, turnierId: Uuid? = null, activeOnly: Boolean = true): List<Platz> {
        require(boden.isNotBlank()) { "Ground type cannot be blank" }
        return platzRepository.findByGroundType(boden.trim(), turnierId, activeOnly)
    }

    /**
     * Retrieves venues by dimensions.
     *
     * @param dimension The venue dimensions (e.g., "20x60m", "20x40m")
     * @param turnierId Optional tournament ID to limit search
     * @param activeOnly Whether to return only active venues (default: true)
     * @return List of venues with the specified dimensions
     */
    suspend fun getByDimensions(dimension: String, turnierId: Uuid? = null, activeOnly: Boolean = true): List<Platz> {
        require(dimension.isNotBlank()) { "Dimension cannot be blank" }
        return platzRepository.findByDimensions(dimension.trim(), turnierId, activeOnly)
    }

    /**
     * Retrieves all active venues.
     *
     * @param orderBySortierung Whether to order by sortierReihenfolge field (default: true)
     * @return List of active venues
     */
    suspend fun getAllActive(orderBySortierung: Boolean = true): List<Platz> {
        return platzRepository.findAllActive(orderBySortierung)
    }

    /**
     * Finds venues suitable for a specific discipline based on type and dimensions.
     *
     * @param requiredType The required venue type
     * @param requiredDimensions Optional required dimensions
     * @param turnierId Optional tournament ID to limit search
     * @return List of suitable venues
     */
    suspend fun getSuitableForDiscipline(
        requiredType: PlatzTypE,
        requiredDimensions: String? = null,
        turnierId: Uuid? = null
    ): List<Platz> {
        requiredDimensions?.let { dimensions ->
            require(dimensions.isNotBlank()) { "Required dimensions cannot be blank if provided" }
        }
        return platzRepository.findSuitableForDiscipline(requiredType, requiredDimensions?.trim(), turnierId)
    }

    /**
     * Checks if a venue with the given name exists for a tournament.
     *
     * @param name The venue name to check
     * @param turnierId The tournament ID
     * @return true if a venue with this name exists, false otherwise
     */
    suspend fun existsByNameAndTournament(name: String, turnierId: Uuid): Boolean {
        require(name.isNotBlank()) { "Venue name cannot be blank" }
        return platzRepository.existsByNameAndTournament(name.trim(), turnierId)
    }

    /**
     * Counts the total number of active venues for a tournament.
     *
     * @param turnierId The tournament ID
     * @return The total count of active venues
     */
    suspend fun countActiveByTournament(turnierId: Uuid): Long {
        return platzRepository.countActiveByTournament(turnierId)
    }

    /**
     * Counts venues by type for a tournament.
     *
     * @param typ The venue type
     * @param turnierId The tournament ID
     * @param activeOnly Whether to count only active venues (default: true)
     * @return The count of venues of the specified type
     */
    suspend fun countByTypeAndTournament(typ: PlatzTypE, turnierId: Uuid, activeOnly: Boolean = true): Long {
        return platzRepository.countByTypeAndTournament(typ, turnierId, activeOnly)
    }

    /**
     * Finds available venues for a specific time slot.
     * This method can be extended when venue scheduling functionality is added.
     *
     * @param turnierId The tournament ID
     * @param startTime The start time (placeholder for future scheduling feature)
     * @param endTime The end time (placeholder for future scheduling feature)
     * @return List of available venues (currently returns all active venues)
     */
    suspend fun getAvailableForTimeSlot(turnierId: Uuid, startTime: String? = null, endTime: String? = null): List<Platz> {
        return platzRepository.findAvailableForTimeSlot(turnierId, startTime, endTime)
    }

    /**
     * Retrieves venues grouped by type for a tournament.
     * This is a convenience method that provides venues organized by their type.
     *
     * @param turnierId The tournament ID
     * @param activeOnly Whether to include only active venues (default: true)
     * @return Map of venue type to list of venues
     */
    suspend fun getGroupedByTypeForTournament(turnierId: Uuid, activeOnly: Boolean = true): Map<PlatzTypE, List<Platz>> {
        val venues = platzRepository.findByTournament(turnierId, activeOnly, true)
        return venues.groupBy { it.typ }
    }

    /**
     * Retrieves venues with specific characteristics for discipline matching.
     * This method combines multiple filters to find venues suitable for specific disciplines.
     *
     * @param turnierId The tournament ID
     * @param requiredType The required venue type
     * @param preferredDimensions Preferred dimensions (optional)
     * @param preferredGroundType Preferred ground type (optional)
     * @param activeOnly Whether to include only active venues (default: true)
     * @return List of venues matching the criteria, sorted by preference
     */
    suspend fun getForDisciplineRequirements(
        turnierId: Uuid,
        requiredType: PlatzTypE,
        preferredDimensions: String? = null,
        preferredGroundType: String? = null,
        activeOnly: Boolean = true
    ): List<Platz> {
        // Start with venues of the required type
        val typeMatches = platzRepository.findByType(requiredType, turnierId, activeOnly)

        // If no specific preferences, return all type matches
        if (preferredDimensions == null && preferredGroundType == null) {
            return typeMatches
        }

        // Filter and sort by preferences
        val exactMatches = mutableListOf<Platz>()
        val partialMatches = mutableListOf<Platz>()
        val otherMatches = mutableListOf<Platz>()

        for (venue in typeMatches) {
            val dimensionMatch = preferredDimensions == null || venue.dimension == preferredDimensions.trim()
            val groundMatch = preferredGroundType == null || venue.boden == preferredGroundType.trim()

            when {
                dimensionMatch && groundMatch -> exactMatches.add(venue)
                dimensionMatch || groundMatch -> partialMatches.add(venue)
                else -> otherMatches.add(venue)
            }
        }

        // Return sorted by preference: exact matches first, then partial, then others
        return exactMatches + partialMatches + otherMatches
    }

    /**
     * Validates venue availability and suitability for a specific use case.
     * This method performs comprehensive checks for venue usage.
     *
     * @param platzId The venue ID
     * @param requiredType Optional required venue type
     * @param requiredDimensions Optional required dimensions
     * @param requiredGroundType Optional required ground type
     * @return Pair of (isValid, reasons) where reasons contains any validation issues
     */
    suspend fun validateVenueSuitability(
        platzId: Uuid,
        requiredType: PlatzTypE? = null,
        requiredDimensions: String? = null,
        requiredGroundType: String? = null
    ): Pair<Boolean, List<String>> {
        val venue = platzRepository.findById(platzId)
        val issues = mutableListOf<String>()

        if (venue == null) {
            issues.add("Venue not found")
            return Pair(false, issues)
        }

        if (!venue.istAktiv) {
            issues.add("Venue is not active")
        }

        requiredType?.let { type ->
            if (venue.typ != type) {
                issues.add("Venue type ${venue.typ} does not match required type $type")
            }
        }

        requiredDimensions?.let { dimensions ->
            if (venue.dimension != dimensions.trim()) {
                issues.add("Venue dimensions '${venue.dimension}' do not match required dimensions '$dimensions'")
            }
        }

        requiredGroundType?.let { groundType ->
            if (venue.boden != groundType.trim()) {
                issues.add("Venue ground type '${venue.boden}' does not match required ground type '$groundType'")
            }
        }

        return Pair(issues.isEmpty(), issues)
    }
}

@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)
package at.mocode.masterdata.domain.repository

import at.mocode.core.domain.model.PlatzTypE
import at.mocode.masterdata.domain.model.Platz
import kotlin.uuid.Uuid

/**
 * Repository interface for Platz (Venue/Arena) domain operations.
 *
 * This interface defines the contract for venue/arena data access operations
 * without depending on specific implementation details (database, etc.).
 * Following the hexagonal architecture pattern, this interface belongs
 * to the domain layer and will be implemented in the infrastructure layer.
 */
interface PlatzRepository {

    /**
     * Finds a venue by its unique ID.
     *
     * @param id The unique identifier of the venue
     * @return The venue if found, null otherwise
     */
    suspend fun findById(id: Uuid): Platz?

    /**
     * Finds all venues for a specific tournament.
     *
     * @param turnierId The tournament ID
     * @param activeOnly Whether to return only active venues
     * @param orderBySortierung Whether to order by sortierReihenfolge field
     * @return List of venues for the tournament
     */
    suspend fun findByTournament(turnierId: Uuid, activeOnly: Boolean = true, orderBySortierung: Boolean = true): List<Platz>

    /**
     * Finds venues by name (partial match).
     *
     * @param searchTerm The search term to match against venue names
     * @param turnierId Optional tournament ID to limit search
     * @param limit Maximum number of results to return
     * @return List of matching venues
     */
    suspend fun findByName(searchTerm: String, turnierId: Uuid? = null, limit: Int = 50): List<Platz>

    /**
     * Finds venues by type.
     *
     * @param typ The venue type
     * @param turnierId Optional tournament ID to limit search
     * @param activeOnly Whether to return only active venues
     * @return List of venues of the specified type
     */
    suspend fun findByType(typ: PlatzTypE, turnierId: Uuid? = null, activeOnly: Boolean = true): List<Platz>

    /**
     * Finds venues by ground type.
     *
     * @param boden The ground type (e.g., "Sand", "Gras", "Kunststoff")
     * @param turnierId Optional tournament ID to limit search
     * @param activeOnly Whether to return only active venues
     * @return List of venues with the specified ground type
     */
    suspend fun findByGroundType(boden: String, turnierId: Uuid? = null, activeOnly: Boolean = true): List<Platz>

    /**
     * Finds venues by dimensions.
     *
     * @param dimension The venue dimensions (e.g., "20x60m", "20x40m")
     * @param turnierId Optional tournament ID to limit search
     * @param activeOnly Whether to return only active venues
     * @return List of venues with the specified dimensions
     */
    suspend fun findByDimensions(dimension: String, turnierId: Uuid? = null, activeOnly: Boolean = true): List<Platz>

    /**
     * Finds all active venues.
     *
     * @param orderBySortierung Whether to order by sortierReihenfolge field
     * @return List of active venues
     */
    suspend fun findAllActive(orderBySortierung: Boolean = true): List<Platz>

    /**
     * Finds venues suitable for a specific discipline based on type and dimensions.
     *
     * @param requiredType The required venue type
     * @param requiredDimensions Optional required dimensions
     * @param turnierId Optional tournament ID to limit search
     * @return List of suitable venues
     */
    suspend fun findSuitableForDiscipline(
        requiredType: PlatzTypE,
        requiredDimensions: String? = null,
        turnierId: Uuid? = null
    ): List<Platz>

    /**
     * Saves a venue (create or update).
     *
     * @param platz The venue to save
     * @return The saved venue with updated timestamps
     */
    suspend fun save(platz: Platz): Platz

    /**
     * Deletes a venue by ID.
     *
     * @param id The unique identifier of the venue to delete
     * @return true if the venue was deleted, false if not found
     */
    suspend fun delete(id: Uuid): Boolean

    /**
     * Checks if a venue with the given name exists for a tournament.
     *
     * @param name The venue name to check
     * @param turnierId The tournament ID
     * @return true if a venue with this name exists, false otherwise
     */
    suspend fun existsByNameAndTournament(name: String, turnierId: Uuid): Boolean

    /**
     * Counts the total number of active venues for a tournament.
     *
     * @param turnierId The tournament ID
     * @return The total count of active venues
     */
    suspend fun countActiveByTournament(turnierId: Uuid): Long

    /**
     * Counts venues by type for a tournament.
     *
     * @param typ The venue type
     * @param turnierId The tournament ID
     * @param activeOnly Whether to count only active venues
     * @return The count of venues of the specified type
     */
    suspend fun countByTypeAndTournament(typ: PlatzTypE, turnierId: Uuid, activeOnly: Boolean = true): Long

    /**
     * Finds available venues for a specific time slot (if scheduling is implemented).
     * This method can be extended when venue scheduling functionality is added.
     *
     * @param turnierId The tournament ID
     * @param startTime The start time (placeholder for future scheduling feature)
     * @param endTime The end time (placeholder for future scheduling feature)
     * @return List of available venues (currently returns all active venues)
     */
    suspend fun findAvailableForTimeSlot(turnierId: Uuid, startTime: String? = null, endTime: String? = null): List<Platz>
}

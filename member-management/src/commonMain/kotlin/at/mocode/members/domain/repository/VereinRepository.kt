package at.mocode.members.domain.repository

import at.mocode.members.domain.model.DomVerein
import com.benasher44.uuid.Uuid

/**
 * Repository interface for Verein (Club/Association) domain operations.
 *
 * This interface defines the contract for club data access operations
 * without depending on specific implementation details (database, etc.).
 * Following the hexagonal architecture pattern, this interface belongs
 * to the domain layer and will be implemented in the infrastructure layer.
 */
interface VereinRepository {

    /**
     * Finds a club by its unique ID.
     *
     * @param id The unique identifier of the club
     * @return The club if found, null otherwise
     */
    suspend fun findById(id: Uuid): DomVerein?

    /**
     * Finds a club by its OEPS Vereinsnummer.
     *
     * @param oepsVereinsNr The OEPS Vereinsnummer (4-digit identifier)
     * @return The club if found, null otherwise
     */
    suspend fun findByOepsVereinsNr(oepsVereinsNr: String): DomVerein?

    /**
     * Finds clubs by name (partial match).
     *
     * @param searchTerm The search term to match against club names
     * @param limit Maximum number of results to return
     * @return List of matching clubs
     */
    suspend fun findByName(searchTerm: String, limit: Int = 50): List<DomVerein>

    /**
     * Finds all clubs in a specific Bundesland (state).
     *
     * @param bundeslandId The unique identifier of the Bundesland
     * @return List of clubs in the specified Bundesland
     */
    suspend fun findByBundeslandId(bundeslandId: Uuid): List<DomVerein>

    /**
     * Finds all clubs in a specific country.
     *
     * @param landId The unique identifier of the country
     * @return List of clubs in the specified country
     */
    suspend fun findByLandId(landId: Uuid): List<DomVerein>

    /**
     * Finds all active clubs.
     *
     * @param limit Maximum number of results to return
     * @param offset Number of records to skip for pagination
     * @return List of active clubs
     */
    suspend fun findAllActive(limit: Int = 50, offset: Int = 0): List<DomVerein>

    /**
     * Finds clubs by location (city/postal code).
     *
     * @param searchTerm The search term to match against city or postal code
     * @param limit Maximum number of results to return
     * @return List of matching clubs
     */
    suspend fun findByLocation(searchTerm: String, limit: Int = 50): List<DomVerein>

    /**
     * Saves a club (create or update).
     *
     * @param verein The club to save
     * @return The saved club with updated timestamps
     */
    suspend fun save(verein: DomVerein): DomVerein

    /**
     * Deletes a club by ID.
     *
     * @param id The unique identifier of the club to delete
     * @return true if the club was deleted, false if not found
     */
    suspend fun delete(id: Uuid): Boolean

    /**
     * Checks if a club with the given OEPS Vereinsnummer exists.
     *
     * @param oepsVereinsNr The OEPS Vereinsnummer to check
     * @return true if a club with this number exists, false otherwise
     */
    suspend fun existsByOepsVereinsNr(oepsVereinsNr: String): Boolean

    /**
     * Counts the total number of active clubs.
     *
     * @return The total count of active clubs
     */
    suspend fun countActive(): Long

    /**
     * Counts the number of active clubs in a specific Bundesland.
     *
     * @param bundeslandId The unique identifier of the Bundesland
     * @return The count of active clubs in the specified Bundesland
     */
    suspend fun countActiveByBundeslandId(bundeslandId: Uuid): Long
}

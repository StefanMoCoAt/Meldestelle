@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)
package at.mocode.masterdata.domain.repository

import at.mocode.masterdata.domain.model.LandDefinition
import kotlin.uuid.Uuid

/**
 * Repository interface for LandDefinition (Country) domain operations.
 *
 * This interface defines the contract for country data access operations
 * without depending on specific implementation details (database, etc.).
 * Following the hexagonal architecture pattern, this interface belongs
 * to the domain layer and will be implemented in the infrastructure layer.
 */
interface LandRepository {

    /**
     * Finds a country by its unique ID.
     *
     * @param id The unique identifier of the country
     * @return The country if found, null otherwise
     */
    suspend fun findById(id: Uuid): LandDefinition?

    /**
     * Finds a country by its ISO Alpha-2 code.
     *
     * @param isoAlpha2Code The 2-letter ISO code (e.g., "AT", "DE")
     * @return The country if found, null otherwise
     */
    suspend fun findByIsoAlpha2Code(isoAlpha2Code: String): LandDefinition?

    /**
     * Finds a country by its ISO Alpha-3 code.
     *
     * @param isoAlpha3Code The 3-letter ISO code (e.g., "AUT", "DEU")
     * @return The country if found, null otherwise
     */
    suspend fun findByIsoAlpha3Code(isoAlpha3Code: String): LandDefinition?

    /**
     * Finds countries by name (partial match on German or English name).
     *
     * @param searchTerm The search term to match against country names
     * @param limit Maximum number of results to return
     * @return List of matching countries
     */
    suspend fun findByName(searchTerm: String, limit: Int = 50): List<LandDefinition>

    /**
     * Finds all active countries.
     *
     * @param orderBySortierung Whether to order by sortierReihenfolge field
     * @return List of active countries
     */
    suspend fun findAllActive(orderBySortierung: Boolean = true): List<LandDefinition>

    /**
     * Finds all EU member countries.
     *
     * @return List of EU member countries
     */
    suspend fun findEuMembers(): List<LandDefinition>

    /**
     * Finds all EWR (European Economic Area) member countries.
     *
     * @return List of EWR member countries
     */
    suspend fun findEwrMembers(): List<LandDefinition>

    /**
     * Saves a country (create or update).
     *
     * @param land The country to save
     * @return The saved country with updated timestamps
     */
    suspend fun save(land: LandDefinition): LandDefinition

    /**
     * Deletes a country by ID.
     *
     * @param id The unique identifier of the country to delete
     * @return true if the country was deleted, false if not found
     */
    suspend fun delete(id: Uuid): Boolean

    /**
     * Checks if a country with the given ISO Alpha-2 code exists.
     *
     * @param isoAlpha2Code The ISO Alpha-2 code to check
     * @return true if a country with this code exists, false otherwise
     */
    suspend fun existsByIsoAlpha2Code(isoAlpha2Code: String): Boolean

    /**
     * Checks if a country with the given ISO Alpha-3 code exists.
     *
     * @param isoAlpha3Code The ISO Alpha-3 code to check
     * @return true if a country with this code exists, false otherwise
     */
    suspend fun existsByIsoAlpha3Code(isoAlpha3Code: String): Boolean

    /**
     * Counts the total number of active countries.
     *
     * @return The total count of active countries
     */
    suspend fun countActive(): Long
}

@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)
package at.mocode.masterdata.domain.repository

import at.mocode.masterdata.domain.model.BundeslandDefinition
import kotlin.uuid.Uuid

/**
 * Repository interface for BundeslandDefinition (Federal State) domain operations.
 *
 * This interface defines the contract for federal state data access operations
 * without depending on specific implementation details (database, etc.).
 * Following the hexagonal architecture pattern, this interface belongs
 * to the domain layer and will be implemented in the infrastructure layer.
 */
interface BundeslandRepository {

    /**
     * Finds a federal state by its unique ID.
     *
     * @param id The unique identifier of the federal state
     * @return The federal state if found, null otherwise
     */
    suspend fun findById(id: Uuid): BundeslandDefinition?

    /**
     * Finds a federal state by its OEPS code.
     *
     * @param oepsCode The OEPS code (e.g., "01", "02")
     * @param landId The country ID to search within
     * @return The federal state if found, null otherwise
     */
    suspend fun findByOepsCode(oepsCode: String, landId: Uuid): BundeslandDefinition?

    /**
     * Finds a federal state by its ISO 3166-2 code.
     *
     * @param iso3166_2_Code The ISO 3166-2 code (e.g., "AT-1", "DE-BY")
     * @return The federal state if found, null otherwise
     */
    suspend fun findByIso3166_2_Code(iso3166_2_Code: String): BundeslandDefinition?

    /**
     * Finds all federal states for a specific country.
     *
     * @param landId The country ID
     * @param activeOnly Whether to return only active federal states
     * @param orderBySortierung Whether to order by sortierReihenfolge field
     * @return List of federal states for the country
     */
    suspend fun findByCountry(landId: Uuid, activeOnly: Boolean = true, orderBySortierung: Boolean = true): List<BundeslandDefinition>

    /**
     * Finds federal states by name (partial match).
     *
     * @param searchTerm The search term to match against federal state names
     * @param landId Optional country ID to limit search
     * @param limit Maximum number of results to return
     * @return List of matching federal states
     */
    suspend fun findByName(searchTerm: String, landId: Uuid? = null, limit: Int = 50): List<BundeslandDefinition>

    /**
     * Finds all active federal states.
     *
     * @param orderBySortierung Whether to order by sortierReihenfolge field
     * @return List of active federal states
     */
    suspend fun findAllActive(orderBySortierung: Boolean = true): List<BundeslandDefinition>

    /**
     * Saves a federal state (create or update).
     *
     * @param bundesland The federal state to save
     * @return The saved federal state with updated timestamps
     */
    suspend fun save(bundesland: BundeslandDefinition): BundeslandDefinition

    /**
     * Deletes a federal state by ID.
     *
     * @param id The unique identifier of the federal state to delete
     * @return true if the federal state was deleted, false if not found
     */
    suspend fun delete(id: Uuid): Boolean

    /**
     * Checks if a federal state with the given OEPS code exists for a country.
     *
     * @param oepsCode The OEPS code to check
     * @param landId The country ID
     * @return true if a federal state with this code exists, false otherwise
     */
    suspend fun existsByOepsCode(oepsCode: String, landId: Uuid): Boolean

    /**
     * Checks if a federal state with the given ISO 3166-2 code exists.
     *
     * @param iso3166_2_Code The ISO 3166-2 code to check
     * @return true if a federal state with this code exists, false otherwise
     */
    suspend fun existsByIso3166_2_Code(iso3166_2_Code: String): Boolean

    /**
     * Counts the total number of active federal states for a country.
     *
     * @param landId The country ID
     * @return The total count of active federal states
     */
    suspend fun countActiveByCountry(landId: Uuid): Long
}

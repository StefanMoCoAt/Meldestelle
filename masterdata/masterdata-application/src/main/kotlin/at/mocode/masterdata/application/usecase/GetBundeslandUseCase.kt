package at.mocode.masterdata.application.usecase

import at.mocode.masterdata.domain.model.BundeslandDefinition
import at.mocode.masterdata.domain.repository.BundeslandRepository
import com.benasher44.uuid.Uuid

/**
 * Use case for retrieving federal state information.
 *
 * This use case encapsulates the business logic for fetching federal state data
 * and provides a clean interface for the application layer.
 */
class GetBundeslandUseCase(
    private val bundeslandRepository: BundeslandRepository
) {

    /**
     * Retrieves a federal state by its unique ID.
     *
     * @param bundeslandId The unique identifier of the federal state
     * @return The federal state if found, null otherwise
     */
    suspend fun getById(bundeslandId: Uuid): BundeslandDefinition? {
        return bundeslandRepository.findById(bundeslandId)
    }

    /**
     * Retrieves a federal state by its OEPS code for a specific country.
     *
     * @param oepsCode The OEPS code (e.g., "01", "02")
     * @param landId The country ID
     * @return The federal state if found, null otherwise
     */
    suspend fun getByOepsCode(oepsCode: String, landId: Uuid): BundeslandDefinition? {
        require(oepsCode.isNotBlank()) { "OEPS code cannot be blank" }
        return bundeslandRepository.findByOepsCode(oepsCode.trim(), landId)
    }

    /**
     * Retrieves a federal state by its ISO 3166-2 code.
     *
     * @param iso3166_2_Code The ISO 3166-2 code (e.g., "AT-1", "DE-BY")
     * @return The federal state if found, null otherwise
     */
    suspend fun getByIso3166_2_Code(iso3166_2_Code: String): BundeslandDefinition? {
        require(iso3166_2_Code.isNotBlank()) { "ISO 3166-2 code cannot be blank" }
        return bundeslandRepository.findByIso3166_2_Code(iso3166_2_Code.trim().uppercase())
    }

    /**
     * Retrieves all federal states for a specific country.
     *
     * @param landId The country ID
     * @param activeOnly Whether to return only active federal states (default: true)
     * @param orderBySortierung Whether to order by sortierReihenfolge field (default: true)
     * @return List of federal states for the country
     */
    suspend fun getByCountry(landId: Uuid, activeOnly: Boolean = true, orderBySortierung: Boolean = true): List<BundeslandDefinition> {
        return bundeslandRepository.findByCountry(landId, activeOnly, orderBySortierung)
    }

    /**
     * Searches for federal states by name (partial match).
     *
     * @param searchTerm The search term to match against federal state names
     * @param landId Optional country ID to limit search
     * @param limit Maximum number of results to return (default: 50)
     * @return List of matching federal states
     */
    suspend fun searchByName(searchTerm: String, landId: Uuid? = null, limit: Int = 50): List<BundeslandDefinition> {
        require(searchTerm.isNotBlank()) { "Search term cannot be blank" }
        require(limit > 0) { "Limit must be positive" }
        return bundeslandRepository.findByName(searchTerm.trim(), landId, limit)
    }

    /**
     * Retrieves all active federal states.
     *
     * @param orderBySortierung Whether to order by sortierReihenfolge field (default: true)
     * @return List of active federal states
     */
    suspend fun getAllActive(orderBySortierung: Boolean = true): List<BundeslandDefinition> {
        return bundeslandRepository.findAllActive(orderBySortierung)
    }

    /**
     * Checks if a federal state with the given OEPS code exists for a country.
     *
     * @param oepsCode The OEPS code to check
     * @param landId The country ID
     * @return true if a federal state with this code exists, false otherwise
     */
    suspend fun existsByOepsCode(oepsCode: String, landId: Uuid): Boolean {
        require(oepsCode.isNotBlank()) { "OEPS code cannot be blank" }
        return bundeslandRepository.existsByOepsCode(oepsCode.trim(), landId)
    }

    /**
     * Checks if a federal state with the given ISO 3166-2 code exists.
     *
     * @param iso3166_2_Code The ISO 3166-2 code to check
     * @return true if a federal state with this code exists, false otherwise
     */
    suspend fun existsByIso3166_2_Code(iso3166_2_Code: String): Boolean {
        require(iso3166_2_Code.isNotBlank()) { "ISO 3166-2 code cannot be blank" }
        return bundeslandRepository.existsByIso3166_2_Code(iso3166_2_Code.trim().uppercase())
    }

    /**
     * Counts the total number of active federal states for a country.
     *
     * @param landId The country ID
     * @return The total count of active federal states
     */
    suspend fun countActiveByCountry(landId: Uuid): Long {
        return bundeslandRepository.countActiveByCountry(landId)
    }
}

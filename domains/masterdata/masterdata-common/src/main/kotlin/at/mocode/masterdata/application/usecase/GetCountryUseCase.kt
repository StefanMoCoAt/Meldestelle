@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)
package at.mocode.masterdata.application.usecase

import at.mocode.masterdata.domain.model.LandDefinition
import at.mocode.masterdata.domain.repository.LandRepository
import kotlin.uuid.Uuid

/**
 * Use case for retrieving country information.
 *
 * This use case encapsulates the business logic for fetching country data
 * and provides a clean interface for the application layer.
 */
class GetCountryUseCase(
    private val landRepository: LandRepository
) {

    /**
     * Retrieves a country by its unique ID.
     *
     * @param countryId The unique identifier of the country
     * @return The country if found, null otherwise
     */
    suspend fun getById(countryId: Uuid): LandDefinition? {
        return landRepository.findById(countryId)
    }

    /**
     * Retrieves a country by its ISO Alpha-2 code.
     *
     * @param isoCode The 2-letter ISO code (e.g., "AT", "DE")
     * @return The country if found, null otherwise
     */
    suspend fun getByIsoAlpha2Code(isoCode: String): LandDefinition? {
        require(isoCode.length == 2) { "ISO Alpha-2 code must be exactly 2 characters" }
        return landRepository.findByIsoAlpha2Code(isoCode.uppercase())
    }

    /**
     * Retrieves a country by its ISO Alpha-3 code.
     *
     * @param isoCode The 3-letter ISO code (e.g., "AUT", "DEU")
     * @return The country if found, null otherwise
     */
    suspend fun getByIsoAlpha3Code(isoCode: String): LandDefinition? {
        require(isoCode.length == 3) { "ISO Alpha-3 code must be exactly 3 characters" }
        return landRepository.findByIsoAlpha3Code(isoCode.uppercase())
    }

    /**
     * Searches for countries by name (partial match).
     *
     * @param searchTerm The search term to match against country names
     * @param limit Maximum number of results to return (default: 50)
     * @return List of matching countries
     */
    suspend fun searchByName(searchTerm: String, limit: Int = 50): List<LandDefinition> {
        require(searchTerm.isNotBlank()) { "Search term cannot be blank" }
        require(limit > 0) { "Limit must be positive" }
        return landRepository.findByName(searchTerm.trim(), limit)
    }

    /**
     * Retrieves all active countries.
     *
     * @param orderBySortierung Whether to order by sortierReihenfolge field (default: true)
     * @return List of active countries
     */
    suspend fun getAllActive(orderBySortierung: Boolean = true): List<LandDefinition> {
        return landRepository.findAllActive(orderBySortierung)
    }

    /**
     * Retrieves all EU member countries.
     *
     * @return List of EU member countries
     */
    suspend fun getEuMembers(): List<LandDefinition> {
        return landRepository.findEuMembers()
    }

    /**
     * Retrieves all EWR (European Economic Area) member countries.
     *
     * @return List of EWR member countries
     */
    suspend fun getEwrMembers(): List<LandDefinition> {
        return landRepository.findEwrMembers()
    }

    /**
     * Checks if a country with the given ISO Alpha-2 code exists.
     *
     * @param isoCode The ISO Alpha-2 code to check
     * @return true if a country with this code exists, false otherwise
     */
    suspend fun existsByIsoAlpha2Code(isoCode: String): Boolean {
        require(isoCode.length == 2) { "ISO Alpha-2 code must be exactly 2 characters" }
        return landRepository.existsByIsoAlpha2Code(isoCode.uppercase())
    }

    /**
     * Checks if a country with the given ISO Alpha-3 code exists.
     *
     * @param isoCode The ISO Alpha-3 code to check
     * @return true if a country with this code exists, false otherwise
     */
    suspend fun existsByIsoAlpha3Code(isoCode: String): Boolean {
        require(isoCode.length == 3) { "ISO Alpha-3 code must be exactly 3 characters" }
        return landRepository.existsByIsoAlpha3Code(isoCode.uppercase())
    }

    /**
     * Counts the total number of active countries.
     *
     * @return The total count of active countries
     */
    suspend fun countActive(): Long {
        return landRepository.countActive()
    }
}

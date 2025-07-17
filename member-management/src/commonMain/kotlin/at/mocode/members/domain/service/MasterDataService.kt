package at.mocode.members.domain.service

import com.benasher44.uuid.Uuid

/**
 * Service interface for accessing master data from other bounded contexts.
 *
 * This interface abstracts the communication with the master-data context,
 * following the Self-Contained Systems architecture principles by avoiding
 * direct repository dependencies between bounded contexts.
 */
interface MasterDataService {

    /**
     * Data class representing country information.
     */
    data class CountryInfo(
        val id: Uuid,
        val name: String,
        val code: String
    )

    /**
     * Data class representing state/bundesland information.
     */
    data class StateInfo(
        val id: Uuid,
        val name: String,
        val code: String,
        val countryId: Uuid
    )

    /**
     * Validates if a country exists by its ID.
     *
     * @param countryId The unique identifier of the country
     * @return true if the country exists, false otherwise
     */
    suspend fun countryExists(countryId: Uuid): Boolean

    /**
     * Validates if a state/bundesland exists by its ID.
     *
     * @param stateId The unique identifier of the state
     * @return true if the state exists, false otherwise
     */
    suspend fun stateExists(stateId: Uuid): Boolean

    /**
     * Gets country information by ID.
     *
     * @param countryId The unique identifier of the country
     * @return CountryInfo if found, null otherwise
     */
    suspend fun getCountryById(countryId: Uuid): CountryInfo?

    /**
     * Gets state information by ID.
     *
     * @param stateId The unique identifier of the state
     * @return StateInfo if found, null otherwise
     */
    suspend fun getStateById(stateId: Uuid): StateInfo?

    /**
     * Gets all available countries.
     *
     * @return List of all countries
     */
    suspend fun getAllCountries(): List<CountryInfo>

    /**
     * Gets all states for a specific country.
     *
     * @param countryId The unique identifier of the country
     * @return List of states in the specified country
     */
    suspend fun getStatesByCountry(countryId: Uuid): List<StateInfo>
}

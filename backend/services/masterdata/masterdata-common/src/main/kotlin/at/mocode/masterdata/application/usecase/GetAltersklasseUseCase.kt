@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)
package at.mocode.masterdata.application.usecase

import at.mocode.core.domain.model.SparteE
import at.mocode.masterdata.domain.model.AltersklasseDefinition
import at.mocode.masterdata.domain.repository.AltersklasseRepository
import kotlin.uuid.Uuid

/**
 * Use case for retrieving age class information.
 *
 * This use case encapsulates the business logic for fetching age class data
 * and provides a clean interface for the application layer.
 */
class GetAltersklasseUseCase(
    private val altersklasseRepository: AltersklasseRepository
) {

    /**
     * Retrieves an age class by its unique ID.
     *
     * @param altersklasseId The unique identifier of the age class
     * @return The age class if found, null otherwise
     */
    suspend fun getById(altersklasseId: Uuid): AltersklasseDefinition? {
        return altersklasseRepository.findById(altersklasseId)
    }

    /**
     * Retrieves an age class by its code.
     *
     * @param altersklasseCode The age class code (e.g., "JGD_U16", "JUN_U18")
     * @return The age class if found, null otherwise
     */
    suspend fun getByCode(altersklasseCode: String): AltersklasseDefinition? {
        require(altersklasseCode.isNotBlank()) { "Age class code cannot be blank" }
        return altersklasseRepository.findByCode(altersklasseCode.trim().uppercase())
    }

    /**
     * Searches for age classes by name (partial match).
     *
     * @param searchTerm The search term to match against age class names
     * @param limit Maximum number of results to return (default: 50)
     * @return List of matching age classes
     */
    suspend fun searchByName(searchTerm: String, limit: Int = 50): List<AltersklasseDefinition> {
        require(searchTerm.isNotBlank()) { "Search term cannot be blank" }
        require(limit > 0) { "Limit must be positive" }
        return altersklasseRepository.findByName(searchTerm.trim(), limit)
    }

    /**
     * Retrieves all active age classes.
     *
     * @param sparteFilter Optional filter by sport type
     * @param geschlechtFilter Optional filter by gender ('M', 'W')
     * @return List of active age classes
     */
    suspend fun getAllActive(sparteFilter: SparteE? = null, geschlechtFilter: Char? = null): List<AltersklasseDefinition> {
        geschlechtFilter?.let { gender ->
            require(gender == 'M' || gender == 'W') { "Gender filter must be 'M' or 'W'" }
        }
        return altersklasseRepository.findAllActive(sparteFilter, geschlechtFilter)
    }

    /**
     * Finds age classes applicable for a specific age.
     *
     * @param age The age to check
     * @param sparteFilter Optional filter by sport type
     * @param geschlechtFilter Optional filter by gender ('M', 'W')
     * @return List of applicable age classes
     */
    suspend fun getApplicableForAge(age: Int, sparteFilter: SparteE? = null, geschlechtFilter: Char? = null): List<AltersklasseDefinition> {
        require(age >= 0) { "Age must be non-negative" }
        geschlechtFilter?.let { gender ->
            require(gender == 'M' || gender == 'W') { "Gender filter must be 'M' or 'W'" }
        }
        return altersklasseRepository.findApplicableForAge(age, sparteFilter, geschlechtFilter)
    }

    /**
     * Retrieves age classes by sport type.
     *
     * @param sparte The sport type
     * @param activeOnly Whether to return only active age classes (default: true)
     * @return List of age classes for the sport type
     */
    suspend fun getBySparte(sparte: SparteE, activeOnly: Boolean = true): List<AltersklasseDefinition> {
        return altersklasseRepository.findBySparte(sparte, activeOnly)
    }

    /**
     * Retrieves age classes by gender filter.
     *
     * @param geschlecht The gender ('M', 'W')
     * @param activeOnly Whether to return only active age classes (default: true)
     * @return List of age classes for the gender
     */
    suspend fun getByGeschlecht(geschlecht: Char, activeOnly: Boolean = true): List<AltersklasseDefinition> {
        require(geschlecht == 'M' || geschlecht == 'W') { "Gender must be 'M' or 'W'" }
        return altersklasseRepository.findByGeschlecht(geschlecht, activeOnly)
    }

    /**
     * Retrieves age classes by age range.
     *
     * @param minAge Minimum age (inclusive)
     * @param maxAge Maximum age (inclusive)
     * @param activeOnly Whether to return only active age classes (default: true)
     * @return List of age classes within the age range
     */
    suspend fun getByAgeRange(minAge: Int?, maxAge: Int?, activeOnly: Boolean = true): List<AltersklasseDefinition> {
        minAge?.let { min ->
            require(min >= 0) { "Minimum age must be non-negative" }
        }
        maxAge?.let { max ->
            require(max >= 0) { "Maximum age must be non-negative" }
            minAge?.let { min ->
                require(max >= min) { "Maximum age must be greater than or equal to minimum age" }
            }
        }
        return altersklasseRepository.findByAgeRange(minAge, maxAge, activeOnly)
    }

    /**
     * Retrieves age classes by OETO rule reference.
     *
     * @param oetoRegelReferenzId The OETO rule reference ID
     * @return List of age classes linked to the rule
     */
    suspend fun getByOetoRegelReferenz(oetoRegelReferenzId: Uuid): List<AltersklasseDefinition> {
        return altersklasseRepository.findByOetoRegelReferenz(oetoRegelReferenzId)
    }

    /**
     * Checks if an age class with the given code exists.
     *
     * @param altersklasseCode The age class code to check
     * @return true if an age class with this code exists, false otherwise
     */
    suspend fun existsByCode(altersklasseCode: String): Boolean {
        require(altersklasseCode.isNotBlank()) { "Age class code cannot be blank" }
        return altersklasseRepository.existsByCode(altersklasseCode.trim().uppercase())
    }

    /**
     * Counts the total number of active age classes.
     *
     * @param sparteFilter Optional filter by sport type
     * @return The total count of active age classes
     */
    suspend fun countActive(sparteFilter: SparteE? = null): Long {
        return altersklasseRepository.countActive(sparteFilter)
    }

    /**
     * Validates if a person with given age and gender can participate in an age class.
     *
     * @param altersklasseId The age class ID
     * @param age The person's age
     * @param geschlecht The person's gender ('M', 'W')
     * @return true if the person can participate, false otherwise
     */
    suspend fun isEligible(altersklasseId: Uuid, age: Int, geschlecht: Char): Boolean {
        require(age >= 0) { "Age must be non-negative" }
        require(geschlecht == 'M' || geschlecht == 'W') { "Gender must be 'M' or 'W'" }
        return altersklasseRepository.isEligible(altersklasseId, age, geschlecht)
    }

    /**
     * Retrieves age classes suitable for a participant based on age, gender, and sport.
     * This is a convenience method that combines multiple filters.
     *
     * @param age The participant's age
     * @param geschlecht The participant's gender ('M', 'W')
     * @param sparte The sport type
     * @return List of suitable age classes
     */
    suspend fun getSuitableForParticipant(age: Int, geschlecht: Char, sparte: SparteE): List<AltersklasseDefinition> {
        require(age >= 0) { "Age must be non-negative" }
        require(geschlecht == 'M' || geschlecht == 'W') { "Gender must be 'M' or 'W'" }
        return altersklasseRepository.findApplicableForAge(age, sparte, geschlecht)
    }
}

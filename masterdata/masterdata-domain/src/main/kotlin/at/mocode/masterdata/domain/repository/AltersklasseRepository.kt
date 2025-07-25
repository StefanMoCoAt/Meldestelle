package at.mocode.masterdata.domain.repository

import at.mocode.core.domain.model.SparteE
import at.mocode.masterdata.domain.model.AltersklasseDefinition
import com.benasher44.uuid.Uuid

/**
 * Repository interface for AltersklasseDefinition (Age Class) domain operations.
 *
 * This interface defines the contract for age class data access operations
 * without depending on specific implementation details (database, etc.).
 * Following the hexagonal architecture pattern, this interface belongs
 * to the domain layer and will be implemented in the infrastructure layer.
 */
interface AltersklasseRepository {

    /**
     * Finds an age class by its unique ID.
     *
     * @param id The unique identifier of the age class
     * @return The age class if found, null otherwise
     */
    suspend fun findById(id: Uuid): AltersklasseDefinition?

    /**
     * Finds an age class by its code.
     *
     * @param altersklasseCode The age class code (e.g., "JGD_U16", "JUN_U18")
     * @return The age class if found, null otherwise
     */
    suspend fun findByCode(altersklasseCode: String): AltersklasseDefinition?

    /**
     * Finds age classes by name (partial match).
     *
     * @param searchTerm The search term to match against age class names
     * @param limit Maximum number of results to return
     * @return List of matching age classes
     */
    suspend fun findByName(searchTerm: String, limit: Int = 50): List<AltersklasseDefinition>

    /**
     * Finds all active age classes.
     *
     * @param sparteFilter Optional filter by sport type
     * @param geschlechtFilter Optional filter by gender ('M', 'W')
     * @return List of active age classes
     */
    suspend fun findAllActive(sparteFilter: SparteE? = null, geschlechtFilter: Char? = null): List<AltersklasseDefinition>

    /**
     * Finds age classes applicable for a specific age.
     *
     * @param age The age to check
     * @param sparteFilter Optional filter by sport type
     * @param geschlechtFilter Optional filter by gender ('M', 'W')
     * @return List of applicable age classes
     */
    suspend fun findApplicableForAge(age: Int, sparteFilter: SparteE? = null, geschlechtFilter: Char? = null): List<AltersklasseDefinition>

    /**
     * Finds age classes by sport type.
     *
     * @param sparte The sport type
     * @param activeOnly Whether to return only active age classes
     * @return List of age classes for the sport type
     */
    suspend fun findBySparte(sparte: SparteE, activeOnly: Boolean = true): List<AltersklasseDefinition>

    /**
     * Finds age classes by gender filter.
     *
     * @param geschlecht The gender ('M', 'W')
     * @param activeOnly Whether to return only active age classes
     * @return List of age classes for the gender
     */
    suspend fun findByGeschlecht(geschlecht: Char, activeOnly: Boolean = true): List<AltersklasseDefinition>

    /**
     * Finds age classes by age range.
     *
     * @param minAge Minimum age (inclusive)
     * @param maxAge Maximum age (inclusive)
     * @param activeOnly Whether to return only active age classes
     * @return List of age classes within the age range
     */
    suspend fun findByAgeRange(minAge: Int?, maxAge: Int?, activeOnly: Boolean = true): List<AltersklasseDefinition>

    /**
     * Finds age classes by OETO rule reference.
     *
     * @param oetoRegelReferenzId The OETO rule reference ID
     * @return List of age classes linked to the rule
     */
    suspend fun findByOetoRegelReferenz(oetoRegelReferenzId: Uuid): List<AltersklasseDefinition>

    /**
     * Saves an age class (create or update).
     *
     * @param altersklasse The age class to save
     * @return The saved age class with updated timestamps
     */
    suspend fun save(altersklasse: AltersklasseDefinition): AltersklasseDefinition

    /**
     * Deletes an age class by ID.
     *
     * @param id The unique identifier of the age class to delete
     * @return true if the age class was deleted, false if not found
     */
    suspend fun delete(id: Uuid): Boolean

    /**
     * Checks if an age class with the given code exists.
     *
     * @param altersklasseCode The age class code to check
     * @return true if an age class with this code exists, false otherwise
     */
    suspend fun existsByCode(altersklasseCode: String): Boolean

    /**
     * Counts the total number of active age classes.
     *
     * @param sparteFilter Optional filter by sport type
     * @return The total count of active age classes
     */
    suspend fun countActive(sparteFilter: SparteE? = null): Long

    /**
     * Validates if a person with given age and gender can participate in an age class.
     *
     * @param altersklasseId The age class ID
     * @param age The person's age
     * @param geschlecht The person's gender ('M', 'W')
     * @return true if the person can participate, false otherwise
     */
    suspend fun isEligible(altersklasseId: Uuid, age: Int, geschlecht: Char): Boolean
}

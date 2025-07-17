package at.mocode.members.domain.repository

import at.mocode.members.domain.model.DomPerson
import com.benasher44.uuid.Uuid

/**
 * Repository interface for Person domain operations.
 *
 * This interface defines the contract for person data access operations
 * without depending on specific implementation details (database, etc.).
 * Following the hexagonal architecture pattern, this interface belongs
 * to the domain layer and will be implemented in the infrastructure layer.
 */
interface PersonRepository {

    /**
     * Finds a person by their unique ID.
     *
     * @param id The unique identifier of the person
     * @return The person if found, null otherwise
     */
    suspend fun findById(id: Uuid): DomPerson?

    /**
     * Finds a person by their OEPS Satznummer.
     *
     * @param oepsSatzNr The OEPS Satznummer (6-digit identifier)
     * @return The person if found, null otherwise
     */
    suspend fun findByOepsSatzNr(oepsSatzNr: String): DomPerson?

    /**
     * Finds all persons belonging to a specific club.
     *
     * @param vereinId The unique identifier of the club
     * @return List of persons belonging to the club
     */
    suspend fun findByStammVereinId(vereinId: Uuid): List<DomPerson>

    /**
     * Finds persons by name (partial match on first name or last name).
     *
     * @param searchTerm The search term to match against names
     * @param limit Maximum number of results to return
     * @return List of matching persons
     */
    suspend fun findByName(searchTerm: String, limit: Int = 50): List<DomPerson>

    /**
     * Finds all active persons.
     *
     * @param limit Maximum number of results to return
     * @param offset Number of records to skip for pagination
     * @return List of active persons
     */
    suspend fun findAllActive(limit: Int = 50, offset: Int = 0): List<DomPerson>

    /**
     * Saves a person (create or update).
     *
     * @param person The person to save
     * @return The saved person with updated timestamps
     */
    suspend fun save(person: DomPerson): DomPerson

    /**
     * Deletes a person by ID.
     *
     * @param id The unique identifier of the person to delete
     * @return true if the person was deleted, false if not found
     */
    suspend fun delete(id: Uuid): Boolean

    /**
     * Checks if a person with the given OEPS Satznummer exists.
     *
     * @param oepsSatzNr The OEPS Satznummer to check
     * @return true if a person with this number exists, false otherwise
     */
    suspend fun existsByOepsSatzNr(oepsSatzNr: String): Boolean

    /**
     * Counts the total number of active persons.
     *
     * @return The total count of active persons
     */
    suspend fun countActive(): Long
}

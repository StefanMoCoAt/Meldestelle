package at.mocode.client.common.repository

/**
 * Client-side repository interface for Person entities.
 * This is a simplified version of the domain repository interface.
 */
interface PersonRepository {
    /**
     * Finds a person by their ID.
     *
     * @param id The unique identifier of the person
     * @return The person if found, null otherwise
     */
    suspend fun findById(id: String): Person?

    /**
     * Finds all active persons with pagination.
     *
     * @param limit Maximum number of results to return
     * @param offset Number of results to skip
     * @return List of active persons
     */
    suspend fun findAllActive(limit: Int = 100, offset: Int = 0): List<Person>

    /**
     * Finds persons by name (partial match).
     *
     * @param searchTerm The search term to match against person names
     * @param limit Maximum number of results to return
     * @return List of matching persons
     */
    suspend fun findByName(searchTerm: String, limit: Int = 50): List<Person>

    /**
     * Saves a person (create or update).
     *
     * @param person The person to save
     * @return The saved person with updated information
     */
    suspend fun save(person: Person): Person

    /**
     * Deletes a person by ID.
     *
     * @param id The unique identifier of the person to delete
     * @return true if the person was deleted, false if not found
     */
    suspend fun delete(id: String): Boolean

    /**
     * Counts the total number of active persons.
     *
     * @return The total count of active persons
     */
    suspend fun countActive(): Long
}

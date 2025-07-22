package at.mocode.client.common.repository

import kotlinx.datetime.LocalDate

/**
 * Client-side repository interface for Event entities.
 * This is a simplified version of the domain repository interface.
 */
interface EventRepository {
    /**
     * Finds an event by its ID.
     *
     * @param id The unique identifier of the event
     * @return The event if found, null otherwise
     */
    suspend fun findById(id: String): Event?

    /**
     * Finds all active events with pagination.
     *
     * @param limit Maximum number of results to return
     * @param offset Number of results to skip
     * @return List of active events
     */
    suspend fun findAllActive(limit: Int = 100, offset: Int = 0): List<Event>

    /**
     * Finds events by name (partial match).
     *
     * @param searchTerm The search term to match against event names
     * @param limit Maximum number of results to return
     * @return List of matching events
     */
    suspend fun findByName(searchTerm: String, limit: Int = 50): List<Event>

    /**
     * Finds events by location (partial match).
     *
     * @param location The location to match against event locations
     * @param limit Maximum number of results to return
     * @return List of matching events
     */
    suspend fun findByLocation(location: String, limit: Int = 50): List<Event>

    /**
     * Finds events by date range.
     *
     * @param startDate The start date of the range
     * @param endDate The end date of the range
     * @param limit Maximum number of results to return
     * @return List of events within the date range
     */
    suspend fun findByDateRange(startDate: LocalDate, endDate: LocalDate, limit: Int = 100): List<Event>

    /**
     * Finds upcoming events.
     *
     * @param limit Maximum number of results to return
     * @return List of upcoming events
     */
    suspend fun findUpcoming(limit: Int = 50): List<Event>

    /**
     * Saves an event (create or update).
     *
     * @param event The event to save
     * @return The saved event with updated information
     */
    suspend fun save(event: Event): Event

    /**
     * Deletes an event by ID.
     *
     * @param id The unique identifier of the event to delete
     * @return true if the event was deleted, false if not found
     */
    suspend fun delete(id: String): Boolean

    /**
     * Counts the total number of active events.
     *
     * @return The total count of active events
     */
    suspend fun countActive(): Long
}

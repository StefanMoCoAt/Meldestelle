@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)
package at.mocode.events.domain.repository

import at.mocode.events.domain.model.Veranstaltung
import kotlin.uuid.Uuid
import kotlinx.datetime.LocalDate

/**
 * Repository interface for Veranstaltung (Event) entities.
 *
 * This interface defines the contract for data access operations
 * related to events in the event management bounded context.
 */
interface VeranstaltungRepository {

    /**
     * Finds an event by its unique identifier.
     *
     * @param id The unique identifier of the event
     * @return The event if found, null otherwise
     */
    suspend fun findById(id: Uuid): Veranstaltung?

    /**
     * Finds events by name (partial match).
     *
     * @param searchTerm The search term to match against event names
     * @param limit Maximum number of results to return
     * @return List of matching events
     */
    suspend fun findByName(searchTerm: String, limit: Int = 50): List<Veranstaltung>

    /**
     * Finds events organized by a specific club/association.
     *
     * @param vereinId The ID of the organizing club
     * @param activeOnly Whether to return only active events
     * @return List of events organized by the specified club
     */
    suspend fun findByVeranstalterVereinId(vereinId: Uuid, activeOnly: Boolean = true): List<Veranstaltung>

    /**
     * Finds events within a date range.
     *
     * @param startDate The earliest start date to include
     * @param endDate The latest end date to include
     * @param activeOnly Whether to return only active events
     * @return List of events within the specified date range
     */
    suspend fun findByDateRange(startDate: LocalDate, endDate: LocalDate, activeOnly: Boolean = true): List<Veranstaltung>

    /**
     * Finds events starting on a specific date.
     *
     * @param date The date to search for
     * @param activeOnly Whether to return only active events
     * @return List of events starting on the specified date
     */
    suspend fun findByStartDate(date: LocalDate, activeOnly: Boolean = true): List<Veranstaltung>

    /**
     * Finds all active events.
     *
     * @param limit Maximum number of results to return
     * @param offset Number of results to skip
     * @return List of active events
     */
    suspend fun findAllActive(limit: Int = 100, offset: Int = 0): List<Veranstaltung>

    /**
     * Finds public events (events that are open to public registration).
     *
     * @param activeOnly Whether to return only active events
     * @return List of public events
     */
    suspend fun findPublicEvents(activeOnly: Boolean = true): List<Veranstaltung>

    /**
     * Saves an event (insert or update).
     *
     * @param veranstaltung The event to save
     * @return The saved event
     */
    suspend fun save(veranstaltung: Veranstaltung): Veranstaltung

    /**
     * Deletes an event by its ID.
     *
     * @param id The unique identifier of the event to delete
     * @return True if the event was deleted, false if not found
     */
    suspend fun delete(id: Uuid): Boolean

    /**
     * Counts the number of active events.
     *
     * @return The number of active events
     */
    suspend fun countActive(): Long

    /**
     * Counts events organized by a specific club.
     *
     * @param vereinId The ID of the organizing club
     * @param activeOnly Whether to count only active events
     * @return The number of events organized by the specified club
     */
    suspend fun countByVeranstalterVereinId(vereinId: Uuid, activeOnly: Boolean = true): Long
}

package at.mocode.client.common.repository

import at.mocode.client.common.api.ApiClient
import at.mocode.client.common.api.ApiException
import kotlinx.datetime.LocalDate

/**
 * Client-side implementation of the EventRepository interface.
 * Uses the ApiClient to make HTTP requests to the backend API.
 */
class ClientEventRepository : EventRepository {

    private val baseEndpoint = "/api/events"

    override suspend fun findById(id: String): Event? {
        return try {
            ApiClient.get<Event>("$baseEndpoint/$id")
        } catch (e: Exception) {
            println("[ERROR] Failed to fetch event with ID $id: ${e.message}")
            null
        }
    }

    override suspend fun findAllActive(limit: Int, offset: Int): List<Event> {
        return try {
            ApiClient.get<List<Event>>("$baseEndpoint?limit=$limit&offset=$offset") ?: emptyList()
        } catch (e: Exception) {
            println("[ERROR] Failed to fetch active events: ${e.message}")
            emptyList()
        }
    }

    override suspend fun findByName(searchTerm: String, limit: Int): List<Event> {
        return try {
            ApiClient.get<List<Event>>("$baseEndpoint?search=$searchTerm&limit=$limit") ?: emptyList()
        } catch (e: Exception) {
            println("[ERROR] Failed to search events by name: ${e.message}")
            emptyList()
        }
    }

    override suspend fun findByLocation(location: String, limit: Int): List<Event> {
        return try {
            ApiClient.get<List<Event>>("$baseEndpoint?location=$location&limit=$limit") ?: emptyList()
        } catch (e: Exception) {
            println("[ERROR] Failed to search events by location: ${e.message}")
            emptyList()
        }
    }

    override suspend fun findByDateRange(startDate: LocalDate, endDate: LocalDate, limit: Int): List<Event> {
        return try {
            ApiClient.get<List<Event>>("$baseEndpoint?startDate=$startDate&endDate=$endDate&limit=$limit") ?: emptyList()
        } catch (e: Exception) {
            println("[ERROR] Failed to search events by date range: ${e.message}")
            emptyList()
        }
    }

    override suspend fun findUpcoming(limit: Int): List<Event> {
        return try {
            ApiClient.get<List<Event>>("$baseEndpoint/upcoming?limit=$limit") ?: emptyList()
        } catch (e: Exception) {
            println("[ERROR] Failed to fetch upcoming events: ${e.message}")
            emptyList()
        }
    }

    override suspend fun save(event: Event): Event {
        return try {
            if (event.id.isBlank()) {
                // Create new event
                ApiClient.post<Event>(baseEndpoint, event)
            } else {
                // Update existing event
                ApiClient.put<Event>("$baseEndpoint/${event.id}", event)
            }
        } catch (e: ApiException) {
            println("[ERROR] Failed to save event: ${e.message}")
            throw e
        } catch (e: Exception) {
            println("[ERROR] Unexpected error while saving event: ${e.message}")
            throw ApiException(
                message = "Failed to save event: ${e.message}",
                code = "SAVE_ERROR",
                details = null
            )
        }
    }

    override suspend fun delete(id: String): Boolean {
        return try {
            ApiClient.delete<Boolean>("$baseEndpoint/$id")
            true
        } catch (e: Exception) {
            println("[ERROR] Failed to delete event with ID $id: ${e.message}")
            false
        }
    }

    override suspend fun countActive(): Long {
        return try {
            ApiClient.get<Long>("$baseEndpoint/count") ?: 0L
        } catch (e: Exception) {
            println("[ERROR] Failed to count active events: ${e.message}")
            0L
        }
    }
}

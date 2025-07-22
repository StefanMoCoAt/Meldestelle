package at.mocode.client.common.repository

import at.mocode.client.common.api.ApiClient
import at.mocode.client.common.api.ApiException

/**
 * Client-side implementation of the PersonRepository interface.
 * Uses the ApiClient to make HTTP requests to the backend API.
 */
class ClientPersonRepository : PersonRepository {

    private val baseEndpoint = "/api/persons"

    override suspend fun findById(id: String): Person? {
        return try {
            ApiClient.get<Person>("$baseEndpoint/$id")
        } catch (e: Exception) {
            println("[ERROR] Failed to fetch person with ID $id: ${e.message}")
            null
        }
    }

    override suspend fun findAllActive(limit: Int, offset: Int): List<Person> {
        return try {
            ApiClient.get<List<Person>>("$baseEndpoint?limit=$limit&offset=$offset") ?: emptyList()
        } catch (e: Exception) {
            println("[ERROR] Failed to fetch active persons: ${e.message}")
            emptyList()
        }
    }

    override suspend fun findByName(searchTerm: String, limit: Int): List<Person> {
        return try {
            ApiClient.get<List<Person>>("$baseEndpoint?search=$searchTerm&limit=$limit") ?: emptyList()
        } catch (e: Exception) {
            println("[ERROR] Failed to search persons by name: ${e.message}")
            emptyList()
        }
    }

    override suspend fun save(person: Person): Person {
        return try {
            if (person.id.isBlank()) {
                // Create new person
                ApiClient.post<Person>(baseEndpoint, person)
            } else {
                // Update existing person
                ApiClient.put<Person>("$baseEndpoint/${person.id}", person)
            }
        } catch (e: ApiException) {
            println("[ERROR] Failed to save person: ${e.message}")
            throw e
        } catch (e: Exception) {
            println("[ERROR] Unexpected error while saving person: ${e.message}")
            throw ApiException(
                message = "Failed to save person: ${e.message}",
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
            println("[ERROR] Failed to delete person with ID $id: ${e.message}")
            false
        }
    }

    override suspend fun countActive(): Long {
        return try {
            ApiClient.get<Long>("$baseEndpoint/count") ?: 0L
        } catch (e: Exception) {
            println("[ERROR] Failed to count active persons: ${e.message}")
            0L
        }
    }
}

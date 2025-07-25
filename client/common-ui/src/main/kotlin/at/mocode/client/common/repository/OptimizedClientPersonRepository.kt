package at.mocode.client.common.repository

import at.mocode.client.common.api.ApiClient

/**
 * Optimized client-side implementation of the PersonRepository interface.
 * Uses BaseClientRepository to eliminate code duplication and provide consistent error handling.
 */
class OptimizedClientPersonRepository : BaseClientRepository<Person>("/api/persons"), PersonRepository {

    override suspend fun findById(id: String): Person? {
        return try {
            ApiClient.get<Person>("$baseEndpoint/$id")
        } catch (e: Exception) {
            logError("Failed to fetch person with ID $id", e)
            null
        }
    }

    override suspend fun findAllActive(limit: Int, offset: Int): List<Person> {
        return try {
            ApiClient.get<List<Person>>("$baseEndpoint?limit=$limit&offset=$offset") ?: emptyList()
        } catch (e: Exception) {
            logError("Failed to fetch active persons", e)
            emptyList()
        }
    }

    override suspend fun findByName(searchTerm: String, limit: Int): List<Person> {
        return try {
            ApiClient.get<List<Person>>("$baseEndpoint?search=$searchTerm&limit=$limit") ?: emptyList()
        } catch (e: Exception) {
            logError("Failed to search persons by name", e)
            emptyList()
        }
    }

    override suspend fun save(person: Person): Person {
        return try {
            val result = if (person.id.isBlank()) {
                // Create new person
                ApiClient.post<Person>(baseEndpoint, person)
            } else {
                // Update existing person
                ApiClient.put<Person>("$baseEndpoint/${person.id}", person)
            }

            // Invalidate related cache entries after successful save
            invalidateCache()

            result
        } catch (e: Exception) {
            logError("Failed to save person", e)
            throw e
        }
    }

    override suspend fun delete(id: String): Boolean {
        return try {
            ApiClient.delete<Boolean>("$baseEndpoint/$id")

            // Invalidate related cache entries after successful delete
            invalidateCache()

            true
        } catch (e: Exception) {
            logError("Failed to delete person with ID $id", e)
            false
        }
    }

    override suspend fun countActive(): Long {
        return countActiveEntities()
    }
}

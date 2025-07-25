package at.mocode.client.common.repository

import at.mocode.client.common.api.ApiClient
import at.mocode.client.common.api.ApiException

/**
 * Base repository class that provides common CRUD operations for client-side repositories.
 * Eliminates code duplication and provides consistent error handling across all repositories.
 */
abstract class BaseClientRepository<T : Any>(
    protected val baseEndpoint: String
) {

    /**
     * Finds an entity by its ID.
     */
    protected suspend fun <T> findEntityById(id: String, entityClass: Class<T>): T? {
        return try {
            // Note: This is a simplified version - in practice you'd use reflection or other means
            // to handle the generic type properly
            @Suppress("UNCHECKED_CAST")
            ApiClient.get<Any>("$baseEndpoint/$id") as? T
        } catch (e: Exception) {
            logError("Failed to fetch entity with ID $id", e)
            null
        }
    }

    /**
     * Finds all active entities with pagination.
     */
    protected suspend fun <T> findAllActiveEntities(limit: Int, offset: Int, entityClass: Class<T>): List<T> {
        return try {
            @Suppress("UNCHECKED_CAST")
            (ApiClient.get<Any>("$baseEndpoint?limit=$limit&offset=$offset") as? List<T>) ?: emptyList()
        } catch (e: Exception) {
            logError("Failed to fetch active entities", e)
            emptyList()
        }
    }

    /**
     * Searches entities by name/search term.
     */
    protected suspend fun <T> searchEntities(searchTerm: String, limit: Int, entityClass: Class<T>): List<T> {
        return try {
            @Suppress("UNCHECKED_CAST")
            (ApiClient.get<Any>("$baseEndpoint?search=$searchTerm&limit=$limit") as? List<T>) ?: emptyList()
        } catch (e: Exception) {
            logError("Failed to search entities by term: $searchTerm", e)
            emptyList()
        }
    }

    /**
     * Searches entities by a specific field.
     */
    protected suspend fun <T> searchEntitiesByField(
        fieldName: String,
        fieldValue: String,
        limit: Int,
        entityClass: Class<T>
    ): List<T> {
        return try {
            @Suppress("UNCHECKED_CAST")
            (ApiClient.get<Any>("$baseEndpoint?$fieldName=$fieldValue&limit=$limit") as? List<T>) ?: emptyList()
        } catch (e: Exception) {
            logError("Failed to search entities by $fieldName: $fieldValue", e)
            emptyList()
        }
    }

    /**
     * Searches entities by date range.
     */
    protected suspend fun <T> searchEntitiesByDateRange(
        startDate: String,
        endDate: String,
        limit: Int,
        entityClass: Class<T>
    ): List<T> {
        return try {
            @Suppress("UNCHECKED_CAST")
            (ApiClient.get<Any>("$baseEndpoint?startDate=$startDate&endDate=$endDate&limit=$limit") as? List<T>) ?: emptyList()
        } catch (e: Exception) {
            logError("Failed to search entities by date range: $startDate to $endDate", e)
            emptyList()
        }
    }

    /**
     * Saves an entity (create or update based on ID).
     */
    protected suspend fun <T> saveEntity(entity: T, getId: (T) -> String, entityClass: Class<T>): T {
        return try {
            val id = getId(entity)
            if (id.isBlank()) {
                // Create new entity
                @Suppress("UNCHECKED_CAST")
                ApiClient.post<Any>(baseEndpoint, entity as Any) as T
            } else {
                // Update existing entity
                @Suppress("UNCHECKED_CAST")
                ApiClient.put<Any>("$baseEndpoint/$id", entity as Any) as T
            }
        } catch (e: ApiException) {
            logError("Failed to save entity", e)
            throw e
        } catch (e: Exception) {
            logError("Unexpected error while saving entity", e)
            throw ApiException(
                message = "Failed to save entity: ${e.message}",
                code = "SAVE_ERROR",
                details = null
            )
        }
    }

    /**
     * Deletes an entity by ID.
     */
    protected suspend fun deleteEntity(id: String): Boolean {
        return try {
            ApiClient.delete<Boolean>("$baseEndpoint/$id")
            true
        } catch (e: Exception) {
            logError("Failed to delete entity with ID $id", e)
            false
        }
    }

    /**
     * Counts active entities.
     */
    protected suspend fun countActiveEntities(): Long {
        return try {
            ApiClient.get<Long>("$baseEndpoint/count") ?: 0L
        } catch (e: Exception) {
            logError("Failed to count active entities", e)
            0L
        }
    }

    /**
     * Gets entities from a specific sub-endpoint.
     */
    protected suspend fun <T> getFromSubEndpoint(subEndpoint: String, limit: Int, entityClass: Class<T>): List<T> {
        return try {
            @Suppress("UNCHECKED_CAST")
            (ApiClient.get<Any>("$baseEndpoint/$subEndpoint?limit=$limit") as? List<T>) ?: emptyList()
        } catch (e: Exception) {
            logError("Failed to fetch from sub-endpoint: $subEndpoint", e)
            emptyList()
        }
    }

    /**
     * Logs errors in a consistent format.
     * In a real application, this should use a proper logging framework.
     */
    internal fun logError(message: String, exception: Exception) {
        println("[ERROR] ${this::class.simpleName}: $message - ${exception.message}")
        // TODO: Replace with proper logging framework (e.g., SLF4J)
    }

    /**
     * Invalidates cache entries related to this repository's endpoint.
     */
    protected fun invalidateCache() {
        // Extract base path for cache invalidation
        val basePath = baseEndpoint.split("/").take(3).joinToString("/")
        // For now, clear all cache - in a real implementation, we'd use pattern matching
        ApiClient.clearCache()
    }
}

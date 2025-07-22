package at.mocode.client.common.api

import at.mocode.core.domain.model.ApiResponse
import at.mocode.core.domain.model.ErrorDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap

/**
 * Shared API client for making HTTP requests to the backend API.
 * Provides methods for common HTTP operations and handles response deserialization.
 * Includes a simple caching mechanism for GET requests.
 */
object ApiClient {
    // Public properties to avoid inline function issues
    val BASE_URL = "http://localhost:8080"
    val json = Json { ignoreUnknownKeys = true; isLenient = true }

    val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(json)
        }

        // Add error handling, timeouts, etc.
        engine {
            requestTimeout = 30_000 // 30 seconds
        }
    }

    // Cache implementation
    val cache = ConcurrentHashMap<String, Pair<Any, Long>>()
    val CACHE_TTL = 30_000L // 30 seconds

    /**
     * Generic GET method with ApiResponse handling and caching
     *
     * @param endpoint The API endpoint to call (without base URL)
     * @param cacheable Whether to cache the response
     * @return The deserialized data of type T
     * @throws ApiException if the request fails or returns an error
     */
    suspend inline fun <reified T> get(endpoint: String, cacheable: Boolean = true): T? {
        try {
            // Check cache if cacheable
            if (cacheable) {
                val cacheKey = endpoint
                val cachedValue = cache[cacheKey]
                if (cachedValue != null && System.currentTimeMillis() - cachedValue.second < CACHE_TTL) {
                    @Suppress("UNCHECKED_CAST")
                    return cachedValue.first as T
                }
            }

            // Make HTTP request
            val response = httpClient.get("$BASE_URL$endpoint")
            val responseText = response.bodyAsText()
            val apiResponse = json.decodeFromString<ApiResponse<T>>(responseText)

            // Handle success/error
            if (apiResponse.success) {
                val data = apiResponse.data

                // Update cache if cacheable
                if (cacheable && data != null) {
                    val cacheKey = endpoint
                    cache[cacheKey] = Pair(data, System.currentTimeMillis())
                }

                return data
            } else {
                throw ApiException(
                    message = apiResponse.error?.message ?: "Unknown API error",
                    code = apiResponse.error?.code ?: "ERROR",
                    details = apiResponse.error?.details
                )
            }
        } catch (e: Exception) {
            if (e is ApiException) throw e
            throw ApiException(
                message = "Error executing GET request: ${e.message}",
                code = "ERROR",
                details = null
            )
        }
    }

    /**
     * Generic POST method with ApiResponse handling
     *
     * @param endpoint The API endpoint to call (without base URL)
     * @param body The request body to send
     * @return The deserialized data of type T
     * @throws ApiException if the request fails or returns an error
     */
    suspend inline fun <reified T> post(endpoint: String, body: Any): T {
        try {
            // Make HTTP request
            val response = httpClient.post("$BASE_URL$endpoint") {
                contentType(ContentType.Application.Json)
                setBody(body)
            }

            val responseText = response.bodyAsText()
            val apiResponse = json.decodeFromString<ApiResponse<T>>(responseText)

            // Handle success/error
            if (apiResponse.success) {
                return apiResponse.data
                    ?: throw IllegalStateException("API response success but data is null")
            } else {
                throw ApiException(
                    message = apiResponse.error?.message ?: "Unknown API error",
                    code = apiResponse.error?.code ?: "ERROR",
                    details = apiResponse.error?.details
                )
            }
        } catch (e: Exception) {
            if (e is ApiException) throw e
            throw ApiException(
                message = "Error executing POST request: ${e.message}",
                code = "ERROR",
                details = null
            )
        }
    }

    /**
     * Generic PUT method with ApiResponse handling
     *
     * @param endpoint The API endpoint to call (without base URL)
     * @param body The request body to send
     * @return The deserialized data of type T
     * @throws ApiException if the request fails or returns an error
     */
    suspend inline fun <reified T> put(endpoint: String, body: Any): T {
        try {
            // Make HTTP request
            val response = httpClient.put("$BASE_URL$endpoint") {
                contentType(ContentType.Application.Json)
                setBody(body)
            }

            val responseText = response.bodyAsText()
            val apiResponse = json.decodeFromString<ApiResponse<T>>(responseText)

            // Handle success/error
            if (apiResponse.success) {
                return apiResponse.data
                    ?: throw IllegalStateException("API response success but data is null")
            } else {
                throw ApiException(
                    message = apiResponse.error?.message ?: "Unknown API error",
                    code = apiResponse.error?.code ?: "ERROR",
                    details = apiResponse.error?.details
                )
            }
        } catch (e: Exception) {
            if (e is ApiException) throw e
            throw ApiException(
                message = "Error executing PUT request: ${e.message}",
                code = "ERROR",
                details = null
            )
        }
    }

    /**
     * Generic DELETE method with ApiResponse handling
     *
     * @param endpoint The API endpoint to call (without base URL)
     * @return The deserialized data of type T
     * @throws ApiException if the request fails or returns an error
     */
    suspend inline fun <reified T> delete(endpoint: String): T {
        try {
            // Make HTTP request
            val response = httpClient.delete("$BASE_URL$endpoint")

            val responseText = response.bodyAsText()
            val apiResponse = json.decodeFromString<ApiResponse<T>>(responseText)

            // Handle success/error
            if (apiResponse.success) {
                return apiResponse.data
                    ?: throw IllegalStateException("API response success but data is null")
            } else {
                throw ApiException(
                    message = apiResponse.error?.message ?: "Unknown API error",
                    code = apiResponse.error?.code ?: "ERROR",
                    details = apiResponse.error?.details
                )
            }
        } catch (e: Exception) {
            if (e is ApiException) throw e
            throw ApiException(
                message = "Error executing DELETE request: ${e.message}",
                code = "ERROR",
                details = null
            )
        }
    }

    /**
     * Clears the cache
     */
    fun clearCache() {
        cache.clear()
    }

    /**
     * Removes a specific item from the cache
     */
    fun invalidateCache(endpoint: String) {
        cache.remove(endpoint)
    }
}

/**
 * Exception thrown when an API request fails
 */
class ApiException(
    message: String,
    val code: String,
    val details: Map<String, String>?
) : Exception(message)

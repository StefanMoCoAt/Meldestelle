package at.mocode.gateway.discovery

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.URI
import java.util.concurrent.ConcurrentHashMap

/**
 * Service discovery component for the API Gateway.
 * Uses Consul to discover services and route requests to them.
 */
class ServiceDiscovery(
    private val consulHost: String = "consul",
    private val consulPort: Int = 8500
) {
    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    // Cache of service instances
    private val serviceCache = ConcurrentHashMap<String, List<ServiceInstance>>()
    private val cacheMutex = Mutex()

    // Default TTL for cache entries in milliseconds (30 seconds)
    private val cacheTtl = 30_000L
    private val cacheTimestamps = ConcurrentHashMap<String, Long>()

    /**
     * Get a service instance for the given service name.
     * Uses a simple round-robin load balancing strategy.
     *
     * @param serviceName The name of the service to get an instance for
     * @return A service instance, or null if no instances are available
     */
    suspend fun getServiceInstance(serviceName: String): ServiceInstance? {
        val instances = getServiceInstances(serviceName)
        if (instances.isEmpty()) {
            return null
        }

        // Simple round-robin load balancing
        val index = (System.currentTimeMillis() % instances.size).toInt()
        return instances[index]
    }

    /**
     * Get all instances of a service.
     *
     * @param serviceName The name of the service to get instances for
     * @return A list of service instances
     */
    suspend fun getServiceInstances(serviceName: String): List<ServiceInstance> {
        // Check cache first
        val cachedInstances = serviceCache[serviceName]
        val timestamp = cacheTimestamps[serviceName] ?: 0

        if (cachedInstances != null && System.currentTimeMillis() - timestamp < cacheTtl) {
            return cachedInstances
        }

        // Cache miss or expired, fetch from Consul
        return cacheMutex.withLock {
            // Double-check in case another thread updated the cache while we were waiting
            val currentTimestamp = cacheTimestamps[serviceName] ?: 0
            if (serviceCache[serviceName] != null && System.currentTimeMillis() - currentTimestamp < cacheTtl) {
                return@withLock serviceCache[serviceName]!!
            }

            try {
                val instances = fetchServiceInstances(serviceName)
                serviceCache[serviceName] = instances
                cacheTimestamps[serviceName] = System.currentTimeMillis()
                instances
            } catch (e: Exception) {
                println("Failed to fetch service instances for $serviceName: ${e.message}")
                e.printStackTrace()

                // Return cached instances if available, even if expired
                cachedInstances ?: emptyList()
            }
        }
    }

    /**
     * Fetch service instances from Consul.
     *
     * @param serviceName The name of the service to fetch instances for
     * @return A list of service instances
     */
    private suspend fun fetchServiceInstances(serviceName: String): List<ServiceInstance> {
        val response = httpClient.get("http://$consulHost:$consulPort/v1/catalog/service/$serviceName")

        if (response.status != HttpStatusCode.OK) {
            throw Exception("Failed to fetch service instances: ${response.status}")
        }

        val responseBody = response.bodyAsText()
        val consulServices = Json.decodeFromString<List<ConsulService>>(responseBody)

        return consulServices.map { service ->
            ServiceInstance(
                id = service.ServiceID,
                name = service.ServiceName,
                host = service.ServiceAddress.ifEmpty { service.Address },
                port = service.ServicePort,
                tags = service.ServiceTags,
                meta = service.ServiceMeta
            )
        }
    }

    /**
     * Build a URL for a service instance.
     *
     * @param instance The service instance
     * @param path The path to append to the URL
     * @return The complete URL
     */
    fun buildServiceUrl(instance: ServiceInstance, path: String): String {
        val baseUrl = "https://${instance.host}:${instance.port}"
        return URI(baseUrl).resolve(path).toString()
    }

    /**
     * Check if a service is healthy.
     *
     * @param serviceName The name of the service to check
     * @return True if the service is healthy, false otherwise
     */
    suspend fun isServiceHealthy(serviceName: String): Boolean {
        try {
            val response = httpClient.get("https://$consulHost:$consulPort/v1/health/service/$serviceName?passing=true")
            val responseBody = response.bodyAsText()
            val healthyServices = Json.decodeFromString<List<Any>>(responseBody)
            return healthyServices.isNotEmpty()
        } catch (e: Exception) {
            println("Failed to check service health for $serviceName: ${e.message}")
            return false
        }
    }
}

/**
 * Represents a service instance.
 */
data class ServiceInstance(
    val id: String,
    val name: String,
    val host: String,
    val port: Int,
    val tags: List<String> = emptyList(),
    val meta: Map<String, String> = emptyMap()
)

/**
 * Consul service response model.
 */
@Serializable
data class ConsulService(
    val ServiceID: String,
    val ServiceName: String,
    val ServiceAddress: String,
    val ServicePort: Int,
    val ServiceTags: List<String> = emptyList(),
    val ServiceMeta: Map<String, String> = emptyMap(),
    val Address: String
)

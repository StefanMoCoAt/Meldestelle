package at.mocode.shared.discovery

import at.mocode.shared.config.AppConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.InetAddress
import java.util.*
import kotlin.time.Duration.Companion.seconds
import com.orbitz.consul.Consul
import com.orbitz.consul.model.agent.ImmutableRegistration
import com.orbitz.consul.model.agent.Registration

/**
 * Service registration configuration.
 *
 * @property serviceName The name of the service to register
 * @property serviceId A unique ID for this service instance (defaults to serviceName + random UUID)
 * @property servicePort The port the service is running on
 * @property healthCheckPath The path for the health check endpoint (defaults to "/health")
 * @property healthCheckInterval The interval between health checks in seconds (defaults to 10 seconds)
 * @property tags Optional tags to associate with the service
 * @property meta Optional metadata to associate with the service
 */
data class ServiceRegistrationConfig(
    val serviceName: String,
    val serviceId: String = "$serviceName-${UUID.randomUUID()}",
    val servicePort: Int,
    val healthCheckPath: String = "/health",
    val healthCheckInterval: Int = 10,
    val tags: List<String> = emptyList(),
    val meta: Map<String, String> = emptyMap()
)

/**
 * Service registration component for registering services with Consul.
 */
class ServiceRegistration(
    private val config: ServiceRegistrationConfig,
    private val consulHost: String = "consul",
    private val consulPort: Int = 8500
) {
    private val consul: Consul by lazy {
        try {
            Consul.builder()
                .withUrl("http://$consulHost:$consulPort")
                .build()
        } catch (e: Exception) {
            println("Failed to connect to Consul: ${e.message}")
            throw e
        }
    }

    private val serviceId = config.serviceId
    private var registered = false

    /**
     * Register the service with Consul.
     */
    fun register() {
        try {
            val hostAddress = InetAddress.getLocalHost().hostAddress

            // Create health check
            val healthCheck = Registration.RegCheck.http(
                "http://$hostAddress:${config.servicePort}${config.healthCheckPath}",
                config.healthCheckInterval.toLong()
            )

            // Create service registration
            val registration = ImmutableRegistration.builder()
                .id(serviceId)
                .name(config.serviceName)
                .address(hostAddress)
                .port(config.servicePort)
                .tags(config.tags)
                .meta(config.meta)
                .check(healthCheck)
                .build()

            // Register service with Consul
            consul.agentClient().register(registration)
            registered = true
            println("Service $serviceId registered with Consul at $consulHost:$consulPort")

            // Start heartbeat to keep service registration active
            startHeartbeat()
        } catch (e: Exception) {
            println("Failed to register service with Consul: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Deregister the service from Consul.
     */
    fun deregister() {
        try {
            if (registered) {
                consul.agentClient().deregister(serviceId)
                registered = false
                println("Service $serviceId deregistered from Consul")
            }
        } catch (e: Exception) {
            println("Failed to deregister service from Consul: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Start a heartbeat to keep the service registration active.
     */
    private fun startHeartbeat() {
        CoroutineScope(Dispatchers.IO).launch {
            while (registered) {
                try {
                    // Send heartbeat to Consul
                    consul.agentClient().pass(serviceId)
                    delay(config.healthCheckInterval.seconds)
                } catch (e: Exception) {
                    println("Failed to send heartbeat to Consul: ${e.message}")
                    delay(5.seconds)
                }
            }
        }
    }
}

/**
 * Factory for creating ServiceRegistration instances.
 */
object ServiceRegistrationFactory {
    /**
     * Create a ServiceRegistration instance for a service.
     *
     * @param serviceName The name of the service to register
     * @param servicePort The port the service is running on
     * @param healthCheckPath The path for the health check endpoint (defaults to "/health")
     * @param tags Optional tags to associate with the service
     * @param meta Optional metadata to associate with the service
     * @return A ServiceRegistration instance
     */
    fun createServiceRegistration(
        serviceName: String,
        servicePort: Int,
        healthCheckPath: String = "/health",
        tags: List<String> = emptyList(),
        meta: Map<String, String> = emptyMap()
    ): ServiceRegistration {
        val config = ServiceRegistrationConfig(
            serviceName = serviceName,
            servicePort = servicePort,
            healthCheckPath = healthCheckPath,
            tags = tags,
            meta = meta
        )

        // Get Consul host and port from configuration if available
        val consulHost = AppConfig.serviceDiscovery.consulHost
        val consulPort = AppConfig.serviceDiscovery.consulPort

        return ServiceRegistration(config, consulHost, consulPort)
    }
}

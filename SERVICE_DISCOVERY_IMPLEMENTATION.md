# Service Discovery Implementation Guide

This document outlines the implementation of service discovery in the Meldestelle project using Consul.

## Overview

Service discovery allows services to dynamically discover and communicate with each other without hardcoded endpoints. This is essential for a microservices architecture to be scalable and resilient.

The implementation consists of three main components:

1. **Consul Service Registry**: A central registry where services register themselves and discover other services.
2. **Service Registration**: Each service registers itself with Consul on startup.
3. **Service Discovery**: The API Gateway uses Consul to discover services and route requests to them.

## 1. Consul Service Registry

Consul has been added to the docker-compose.yml file with the following configuration:

```yaml
consul:
  image: consul:1.15
  container_name: meldestelle-consul
  restart: unless-stopped
  ports:
    - "8500:8500"  # HTTP UI and API
    - "8600:8600/udp"  # DNS interface
  volumes:
    - consul_data:/consul/data
  environment:
    - CONSUL_BIND_INTERFACE=eth0
    - CONSUL_CLIENT_INTERFACE=eth0
  command: "agent -server -ui -bootstrap-expect=1 -client=0.0.0.0"
  networks:
    - meldestelle-net
  healthcheck:
    test: ["CMD", "consul", "members"]
    interval: 10s
    timeout: 5s
    retries: 3
    start_period: 10s
```

The Consul UI is accessible at http://localhost:8500.

## 2. Service Registration

Each service should register itself with Consul on startup. This can be implemented using the following approach:

### Dependencies

Add the following dependencies to each service's build.gradle.kts file:

```kotlin
// Service Discovery dependencies
implementation("com.orbitz.consul:consul-client:1.5.3")
implementation("io.ktor:ktor-client-core:${libs.versions.ktor.get()}")
implementation("io.ktor:ktor-client-cio:${libs.versions.ktor.get()}")
```

### Service Registration Component

Create a service registration component in the shared-kernel module:

```kotlin
class ServiceRegistration(
    private val serviceName: String,
    private val servicePort: Int,
    private val healthCheckPath: String = "/health",
    private val tags: List<String> = emptyList(),
    private val meta: Map<String, String> = emptyMap()
) {
    private val serviceId = "$serviceName-${UUID.randomUUID()}"
    private val consulHost = AppConfig.serviceDiscovery.consulHost
    private val consulPort = AppConfig.serviceDiscovery.consulPort
    private val consul = Consul.builder()
        .withUrl("http://$consulHost:$consulPort")
        .build()
    private var registered = false

    fun register() {
        try {
            val hostAddress = InetAddress.getLocalHost().hostAddress

            // Create health check
            val healthCheck = Registration.RegCheck.http(
                "http://$hostAddress:$servicePort$healthCheckPath",
                AppConfig.serviceDiscovery.healthCheckInterval.toLong()
            )

            // Create service registration
            val registration = ImmutableRegistration.builder()
                .id(serviceId)
                .name(serviceName)
                .address(hostAddress)
                .port(servicePort)
                .tags(tags)
                .meta(meta)
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

    private fun startHeartbeat() {
        CoroutineScope(Dispatchers.IO).launch {
            while (registered) {
                try {
                    consul.agentClient().pass(serviceId)
                    delay(AppConfig.serviceDiscovery.healthCheckInterval.seconds)
                } catch (e: Exception) {
                    println("Failed to send heartbeat to Consul: ${e.message}")
                    delay(5.seconds)
                }
            }
        }
    }
}
```

### Health Check Endpoint

Each service should implement a health check endpoint at `/health` that returns a 200 OK response when the service is healthy:

```kotlin
routing {
    get("/health") {
        call.respond(HttpStatusCode.OK, mapOf("status" to "UP"))
    }
}
```

### Service Registration in Application Startup

Register the service with Consul during application startup:

```kotlin
fun main() {
    // Initialize configuration
    val config = AppConfig

    // Initialize database
    DatabaseFactory.init(config.database)

    // Register service with Consul
    val serviceRegistration = ServiceRegistration(
        serviceName = "my-service",
        servicePort = config.server.port,
        healthCheckPath = "/health",
        tags = listOf("api", "v1"),
        meta = mapOf("version" to config.appInfo.version)
    )
    serviceRegistration.register()

    // Start server
    embeddedServer(Netty, port = config.server.port, host = config.server.host) {
        module()
    }.start(wait = true)

    // Add shutdown hook to deregister service
    Runtime.getRuntime().addShutdownHook(Thread {
        serviceRegistration.deregister()
    })
}
```

## 3. Service Discovery in API Gateway

The API Gateway should use Consul to discover services and route requests to them.

### Dependencies

Add the following dependencies to the API Gateway's build.gradle.kts file:

```kotlin
// Service Discovery dependencies
implementation("com.orbitz.consul:consul-client:1.5.3")
implementation("io.ktor:ktor-client-core:${libs.versions.ktor.get()}")
implementation("io.ktor:ktor-client-cio:${libs.versions.ktor.get()}")
implementation("io.ktor:ktor-client-content-negotiation:${libs.versions.ktor.get()}")
implementation("io.ktor:ktor-serialization-kotlinx-json:${libs.versions.ktor.get()}")
```

### Service Discovery Component

Create a service discovery component in the API Gateway:

```kotlin
class ServiceDiscovery(
    private val consulHost: String = "consul",
    private val consulPort: Int = 8500
) {
    private val consul = Consul.builder()
        .withUrl("http://$consulHost:$consulPort")
        .build()

    // Cache of service instances
    private val serviceCache = ConcurrentHashMap<String, List<ServiceInstance>>()

    // Default TTL for cache entries in milliseconds (30 seconds)
    private val cacheTtl = 30_000L
    private val cacheTimestamps = ConcurrentHashMap<String, Long>()

    /**
     * Get a service instance for the given service name.
     * Uses a simple round-robin load balancing strategy.
     */
    fun getServiceInstance(serviceName: String): ServiceInstance? {
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
     */
    fun getServiceInstances(serviceName: String): List<ServiceInstance> {
        // Check cache first
        val cachedInstances = serviceCache[serviceName]
        val timestamp = cacheTimestamps[serviceName] ?: 0

        if (cachedInstances != null && System.currentTimeMillis() - timestamp < cacheTtl) {
            return cachedInstances
        }

        // Cache miss or expired, fetch from Consul
        try {
            val healthyServices = consul.healthClient()
                .getHealthyServiceInstances(serviceName)
                .response

            val instances = healthyServices.map { serviceHealth ->
                ServiceInstance(
                    id = serviceHealth.service.id,
                    name = serviceHealth.service.service,
                    host = serviceHealth.service.address,
                    port = serviceHealth.service.port,
                    tags = serviceHealth.service.tags,
                    meta = serviceHealth.service.meta
                )
            }

            serviceCache[serviceName] = instances
            cacheTimestamps[serviceName] = System.currentTimeMillis()
            return instances
        } catch (e: Exception) {
            println("Failed to fetch service instances for $serviceName: ${e.message}")
            e.printStackTrace()

            // Return cached instances if available, even if expired
            return cachedInstances ?: emptyList()
        }
    }

    /**
     * Build a URL for a service instance.
     */
    fun buildServiceUrl(instance: ServiceInstance, path: String): String {
        val baseUrl = "http://${instance.host}:${instance.port}"
        return URI(baseUrl).resolve(path).toString()
    }

    /**
     * Check if a service is healthy.
     */
    fun isServiceHealthy(serviceName: String): Boolean {
        try {
            val healthyServices = consul.healthClient()
                .getHealthyServiceInstances(serviceName)
                .response
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
```

### Dynamic Routing in API Gateway

Update the API Gateway's routing configuration to use the service discovery component:

```kotlin
// Initialize service discovery
val serviceDiscovery = ServiceDiscovery(
    consulHost = AppConfig.serviceDiscovery.consulHost,
    consulPort = AppConfig.serviceDiscovery.consulPort
)

routing {
    // Route requests to master-data service
    route("/api/masterdata") {
        handle {
            val serviceName = "master-data"
            val serviceInstance = serviceDiscovery.getServiceInstance(serviceName)

            if (serviceInstance == null) {
                call.respond(HttpStatusCode.ServiceUnavailable, "Service $serviceName is not available")
                return@handle
            }

            val path = call.request.path().removePrefix("/api/masterdata")
            val url = serviceDiscovery.buildServiceUrl(serviceInstance, path)

            // Forward request to service
            val client = HttpClient(CIO)
            val response = client.request(url) {
                method = call.request.httpMethod
                headers {
                    call.request.headers.forEach { key, values ->
                        values.forEach { value ->
                            append(key, value)
                        }
                    }
                }
                call.request.receiveChannel().readRemaining().use {
                    setBody(it.readBytes())
                }
            }

            // Forward response back to client
            call.respond(response.status, response.readBytes())
            client.close()
        }
    }

    // Similar routes for other services...
}
```

## Configuration

Update the AppConfig class to include service discovery configuration:

```kotlin
class ServiceDiscoveryConfig {
    var enabled: Boolean = true
    var consulHost: String = System.getenv("CONSUL_HOST") ?: "consul"
    var consulPort: Int = System.getenv("CONSUL_PORT")?.toIntOrNull() ?: 8500
    var healthCheckInterval: Int = 10 // seconds

    fun configure(props: Properties) {
        enabled = props.getProperty("service-discovery.enabled")?.toBoolean() ?: enabled
        consulHost = props.getProperty("service-discovery.consul.host") ?: consulHost
        consulPort = props.getProperty("service-discovery.consul.port")?.toIntOrNull() ?: consulPort
        healthCheckInterval = props.getProperty("service-discovery.health-check.interval")?.toIntOrNull() ?: healthCheckInterval
    }
}
```

## Conclusion

This implementation provides a robust service discovery mechanism using Consul. Services register themselves with Consul on startup and the API Gateway uses Consul to discover services and route requests to them.

The implementation includes:
- Service registration with health checks
- Service discovery with caching
- Dynamic routing in the API Gateway
- Fallback mechanisms for service unavailability

This approach allows the system to be more resilient and scalable, as services can be added, removed, or scaled without manual configuration changes.

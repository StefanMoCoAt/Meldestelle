package at.mocode.core.utils.discovery

import at.mocode.core.utils.config.AppConfig
import com.orbitz.consul.Consul
import com.orbitz.consul.model.agent.ImmutableRegistration
import com.orbitz.consul.model.agent.Registration
import org.slf4j.LoggerFactory
import java.util.*

class ServiceRegistration internal constructor(
    private val consul: Consul,
    private val registration: ImmutableRegistration
) {
    private companion object {
        private val logger = LoggerFactory.getLogger(ServiceRegistration::class.java)
    }

    private var isRegistered = false

    fun register() {
        if (isRegistered) return
        try {
            consul.agentClient().register(registration)
            isRegistered = true
            logger.info(
                "Service '{}' with ID '{}' successfully registered with Consul.",
                registration.name(),
                registration.id()
            )
        } catch (e: Exception) {
            logger.error("Failed to register service '{}' with Consul.", registration.name(), e)
            throw IllegalStateException("Could not register service with Consul", e)
        }
    }

    fun deregister() {
        if (!isRegistered) return
        try {
            consul.agentClient().deregister(registration.id())
            isRegistered = false
            logger.info(
                "Service '{}' with ID '{}' successfully deregistered from Consul.",
                registration.name(),
                registration.id()
            )
        } catch (e: Exception) {
            logger.error("Failed to deregister service '{}' from Consul.", registration.id(), e)
        }
    }
}

class ServiceRegistrar(private val appConfig: AppConfig) {
    private companion object {
        private val logger = LoggerFactory.getLogger(ServiceRegistrar::class.java)
    }

    private val consul: Consul by lazy {
        val consulConfig = appConfig.serviceDiscovery
        logger.info("Connecting to Consul at {}:{}", consulConfig.consulHost, consulConfig.consulPort)
        Consul.builder()
            .withUrl("http://${consulConfig.consulHost}:${consulConfig.consulPort}")
            .build()
    }

    fun registerCurrentService(): ServiceRegistration {
        val serviceName = appConfig.appInfo.name
        val servicePort = appConfig.server.port
        val serviceId = "$serviceName-${UUID.randomUUID()}"
        val hostAddress = appConfig.server.advertisedHost

        val healthCheck = Registration.RegCheck.http(
            "http://$hostAddress:$servicePort/health",
            10L,
            5L
        )

        // ========= FINALE KORREKTUR =========
        // Wir erstellen die Liste und die Map VORHER mit expliziten Typen,
        // um dem Compiler bei der Typinferenz zu helfen.
        val serviceTags: List<String> = listOf("env:${appConfig.environment.name.lowercase()}")
        val serviceMeta: Map<String, String> = mapOf("version" to appConfig.appInfo.version)

        val registration = ImmutableRegistration.builder()
            .id(serviceId)
            .name(serviceName)
            .address(hostAddress)
            .port(servicePort)
            .check(healthCheck)
            .tags(serviceTags) // Verwenden der explizit typisierten Variablen
            .meta(serviceMeta)  // Verwenden der explizit typisierten Variablen
            .build()

        val serviceRegistration = ServiceRegistration(consul, registration)
        serviceRegistration.register()

        Runtime.getRuntime().addShutdownHook(Thread {
            logger.info("Shutdown hook triggered: Deregistering service '{}'...", serviceId)
            serviceRegistration.deregister()
        })

        return serviceRegistration
    }
}

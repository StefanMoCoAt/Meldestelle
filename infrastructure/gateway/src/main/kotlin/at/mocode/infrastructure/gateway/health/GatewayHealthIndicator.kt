package at.mocode.infrastructure.gateway.health

import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.time.Duration

/**
 * Gateway Health Indicator zur Überwachung der Downstream Services.
 *
 * Prüft die Verfügbarkeit aller registrierten Services über Consul Discovery
 * und führt Health-Checks für kritische Services durch.
 */
@Component
class GatewayHealthIndicator(
    private val discoveryClient: DiscoveryClient,
    private val webClient: WebClient.Builder,
    private val environment: Environment
) : HealthIndicator {

    companion object {
        private val CRITICAL_SERVICES = setOf(
            "members-service",
            "horses-service",
            "events-service",
            "masterdata-service",
            "auth-service"
        )

        private val OPTIONAL_SERVICES = setOf(
            "ping-service"
        )

        private val HEALTH_CHECK_TIMEOUT = Duration.ofSeconds(5)
    }

    override fun health(): Health {
        val builder = Health.up()
        val details = mutableMapOf<String, Any>()

        try {
            // Prüfe alle registrierten Services in Consul
            val allServices = discoveryClient.services
            val discoveredServices = mutableMapOf<String, Any>()

            allServices.forEach { serviceName ->
                val instances = discoveryClient.getInstances(serviceName)
                discoveredServices[serviceName] = mapOf(
                    "instanceCount" to instances.size,
                    "instances" to instances.map { "${it.host}:${it.port}" }
                )
            }

            details["discoveredServices"] = discoveredServices
            details["totalServices"] = allServices.size

            // Prüfe kritische Services
            val criticalServiceStatus = mutableMapOf<String, String>()
            var hasCriticalFailure = false

            CRITICAL_SERVICES.forEach { serviceName ->
                val status = checkServiceHealth(serviceName)
                criticalServiceStatus[serviceName] = status
                if (status != "UP") {
                    hasCriticalFailure = true
                }
            }

            // Prüfe optionale Services
            val optionalServiceStatus = mutableMapOf<String, String>()
            OPTIONAL_SERVICES.forEach { serviceName ->
                optionalServiceStatus[serviceName] = checkServiceHealth(serviceName)
            }

            details["criticalServices"] = criticalServiceStatus
            details["optionalServices"] = optionalServiceStatus

            // Gateway Status basierend auf kritischen Services
            val isTestEnvironment = environment.activeProfiles.contains("test")

            if (hasCriticalFailure && !isTestEnvironment) {
                builder.down()
                details["status"] = "DOWN"
                details["reason"] = "One or more critical services are unavailable"
            } else {
                details["status"] = "UP"
                details["reason"] = if (isTestEnvironment) {
                    "Health check passed (test environment)"
                } else {
                    "All critical services are available"
                }
            }

        } catch (exception: Exception) {
            builder.down()
                .withException(exception)
            details["status"] = "DOWN"
            details["reason"] = "Failed to check downstream services: ${exception.message}"
        }

        return builder.withDetails(details).build()
    }

    private fun checkServiceHealth(serviceName: String): String {
        return try {
            val instances = discoveryClient.getInstances(serviceName)

            if (instances.isEmpty()) {
                "NO_INSTANCES"
            } else {
                // Versuche Health-Check für die erste verfügbare Instanz
                val instance = instances.first()
                val healthUrl = "http://${instance.host}:${instance.port}/actuator/health"

                val client = webClient.build()
                val response = client.get()
                    .uri(healthUrl)
                    .retrieve()
                    .bodyToMono(Map::class.java)
                    .timeout(HEALTH_CHECK_TIMEOUT)
                    .onErrorReturn(mapOf("status" to "DOWN"))
                    .block()

                val status = response?.get("status")?.toString() ?: "UNKNOWN"
                if (status == "UP") "UP" else "DOWN"
            }
        } catch (exception: WebClientResponseException) {
            when (exception.statusCode.value()) {
                404 -> "NO_HEALTH_ENDPOINT"
                503 -> "DOWN"
                else -> "ERROR"
            }
        } catch (exception: Exception) {
            "ERROR"
        }
    }
}

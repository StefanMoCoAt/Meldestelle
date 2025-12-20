package at.mocode.infrastructure.gateway.health

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.health.contributor.Health
import org.springframework.boot.health.contributor.ReactiveHealthIndicator
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import java.net.URI
import java.time.Duration

/**
 * Konfigurations-Properties für die Gesundheitsprüfung der Services.
 */
@Configuration
class HealthConfig {
  @Bean
  fun webClientBuilder(): WebClient.Builder = WebClient.builder()
}

@ConfigurationProperties(prefix = "gateway.health")
data class GatewayHealthProperties(
  val criticalServices: Set<String> = setOf("ping-service", "auth-service"),
  val optionalServices: Set<String> = setOf(
    "entries-service",
    "members-service",
    "horses-service",
    "events-service",
    "masterdata-service"
  ),
  val checkTimeout: Duration = Duration.ofSeconds(5)
)

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
  private val environment: Environment,
  private val properties: GatewayHealthProperties
) : ReactiveHealthIndicator {

  override fun health(): Mono<Health> {
    val details = mutableMapOf<String, Any>()

    // Sammle alle Service-Namen
    val allServices = try {
      discoveryClient.services
    } catch (e: Exception) {
      emptyList<String>()
    }

    // Erstelle Details für entdeckte Services
    val discoveredServices = allServices.associateWith { serviceName ->
      val instances = discoveryClient.getInstances(serviceName)
      mapOf(
        "instanceCount" to instances.size,
        "instances" to instances.map { "${it.host}:${it.port}" }
      )
    }

    details["discoveredServices"] = discoveredServices
    details["totalServices"] = allServices.size

    // Prüfe kritische Services parallel
    val criticalChecks = properties.criticalServices.map { serviceName ->
      checkServiceHealth(serviceName).map { serviceName to it }
    }

    // Prüfe optionale Services parallel
    val optionalChecks = properties.optionalServices.map { serviceName ->
      checkServiceHealth(serviceName).map { serviceName to it }
    }

    return Mono.zip(
      if (criticalChecks.isNotEmpty()) Mono.zip(criticalChecks) { it.associate { check -> check as Pair<*, *> } } else Mono.just(emptyMap()),
      if (optionalChecks.isNotEmpty()) Mono.zip(optionalChecks) { it.associate { check -> check as Pair<*, *> } } else Mono.just(emptyMap())
    ).map { tuple ->
      val criticalServiceStatus = tuple.t1
      val optionalServiceStatus = tuple.t2

      details["criticalServices"] = criticalServiceStatus
      details["optionalServices"] = optionalServiceStatus

      val hasCriticalFailure = criticalServiceStatus.values.any { it != "UP" }
      val isTestEnvironment = environment.activeProfiles.contains("test")
      val isDevEnvironment = environment.activeProfiles.contains("dev")

      val builder = Health.up()
      if (hasCriticalFailure && !isTestEnvironment && !isDevEnvironment) {
        builder.down()
        details["status"] = "DOWN"
        details["reason"] = "Ein oder mehrere kritische Services sind nicht verfügbar"
      } else {
        details["status"] = "UP"
        details["reason"] = when {
          isTestEnvironment -> "Gesundheitsprüfung erfolgreich (Testumgebung)"
          isDevEnvironment -> "Gesundheitsprüfung erfolgreich (Entwicklungsumgebung - nicht alle Services erforderlich)"
          else -> "Alle kritischen Services sind verfügbar"
        }
      }
      builder.withDetails(details).build()
    }.onErrorResume { exception ->
      Mono.just(
        Health.down()
          .withException(exception)
          .withDetails(details)
          .withDetail("status", "DOWN")
          .withDetail("reason", "Fehler beim Prüfen der nachgelagerten Services: ${exception.message}")
          .build()
      )
    }
  }

  private fun checkServiceHealth(serviceName: String): Mono<String> {
    val instances = try {
      discoveryClient.getInstances(serviceName)
    } catch (e: Exception) {
      emptyList()
    }

    if (instances.isEmpty()) {
      return Mono.just("NO_INSTANCES")
    }

    val instance = instances.first()
    val healthUrl = URI.create("http://${instance.host}:${instance.port}/actuator/health")

    return webClient.build().get()
      .uri(healthUrl)
      .retrieve()
      .bodyToMono(Map::class.java)
      .timeout(properties.checkTimeout)
      .map { response ->
        val status = response["status"]?.toString() ?: "UNKNOWN"
        if (status == "UP") "UP" else "DOWN"
      }
      .onErrorResume { exception ->
        val status = when (exception) {
          is WebClientResponseException -> when (exception.statusCode.value()) {
            404 -> "NO_HEALTH_ENDPOINT"
            503 -> "DOWN"
            else -> "ERROR"
          }
          else -> "ERROR"
        }
        Mono.just(status)
      }
  }
}

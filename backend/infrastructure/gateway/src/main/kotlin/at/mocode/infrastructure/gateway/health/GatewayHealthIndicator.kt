package at.mocode.infrastructure.gateway.health

import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.ReactiveHealthIndicator
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
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
  webClientBuilder: WebClient.Builder,
  private val environment: Environment
) : ReactiveHealthIndicator {

  private val webClient = webClientBuilder.build()

  companion object {
    private val CRITICAL_SERVICES = setOf(
      "ping-service"
    )

    private val OPTIONAL_SERVICES = setOf(
      "members-service",
      "horses-service",
      "events-service",
      "masterdata-service",
      "auth-service"
    )

    private val HEALTH_CHECK_TIMEOUT = Duration.ofSeconds(5)
  }

  // KORREKTUR für Spring Boot 4: Die `health()`-Methode und ihr Rückgabetyp `Mono<Health>`
  // erlauben keine Null-Werte mehr. Die Fragezeichen (?) wurden entfernt.
  override fun health(): Mono<Health> {
    val builder = Health.up()
    val details = mutableMapOf<String, Any>()

    return Mono.fromCallable { discoveryClient.services }
      .flatMapMany { services ->
        details["totalServices"] = services.size
        Flux.fromIterable(services)
      }
      .flatMap({ serviceName ->
        val instances = discoveryClient.getInstances(serviceName)
        val instanceDetails = mapOf(
          "instanceCount" to instances.size,
          "instances" to instances.map { "${it.host}:${it.port}" }
        )
        // Für Health-Check nur auf definierte Services gehen
        val checkMono = when {
          CRITICAL_SERVICES.contains(serviceName) || OPTIONAL_SERVICES.contains(serviceName) ->
            checkServiceHealthReactive(serviceName)
          else -> Mono.just("SKIPPED")
        }
        checkMono
          .map { status -> Triple(serviceName, status, instanceDetails) }
      }, 8) // begrenze Parallelität
      .collectList()
      .map { results ->
        val discoveredServices = mutableMapOf<String, Any>()
        val criticalServiceStatus = mutableMapOf<String, String>()
        val optionalServiceStatus = mutableMapOf<String, String>()

        results.forEach { (serviceName, status, instanceDetails) ->
          discoveredServices[serviceName] = instanceDetails
          if (CRITICAL_SERVICES.contains(serviceName)) {
            criticalServiceStatus[serviceName] = status
          } else if (OPTIONAL_SERVICES.contains(serviceName)) {
            optionalServiceStatus[serviceName] = status
          }
        }

        details["discoveredServices"] = discoveredServices
        details["criticalServices"] = criticalServiceStatus
        details["optionalServices"] = optionalServiceStatus

        val isTestEnvironment = environment.activeProfiles.contains("test")
        val isDevEnvironment = environment.activeProfiles.contains("dev")
        val hasCriticalFailure = criticalServiceStatus.values.any { it != "UP" }

        if (hasCriticalFailure && !isTestEnvironment && !isDevEnvironment) {
          builder.down()
          details["status"] = "DOWN"
          details["reason"] = "Ein oder mehrere kritische Services sind nicht verfügbar"
        } else {
          details["status"] = "UP"
          details["reason"] = when {
            isTestEnvironment -> "Gesundheitsprüfung erfolgreich (Testumgebung)"
            isDevEnvironment -> "Gesundheitsprüfung erfolgreich (Entwicklungsumgebung - nicht alle Services erforderlich)"
            else -> "Alle kritischen Services sind verfügbar oder optional"
          }
        }

        builder.withDetails(details).build()
      }
      .onErrorResume { ex ->
        Mono.just(
          Health.down(ex)
            .withDetail("status", "DOWN")
            .withDetail("reason", "Fehler beim Prüfen der nachgelagerten Services: ${ex.message}")
            .build()
        )
      }
  }

  private fun checkServiceHealthReactive(serviceName: String): Mono<String> {
    return Mono.fromCallable { discoveryClient.getInstances(serviceName) }
      .flatMap { instances ->
        if (instances.isEmpty()) {
          Mono.just("NO_INSTANCES")
        } else {
          val instance = instances.first()
          val healthUrl = "http://${instance.host}:${instance.port}/actuator/health"
          webClient.get()
            .uri(healthUrl)
            .retrieve()
            .bodyToMono(Map::class.java)
            .timeout(HEALTH_CHECK_TIMEOUT)
            .map { it["status"]?.toString() ?: "UNKNOWN" }
            .map { status -> if (status == "UP") "UP" else "DOWN" }
            .onErrorResume { ex ->
              when (ex) {
                is WebClientResponseException -> when (ex.statusCode.value()) {
                  404 -> Mono.just("NO_HEALTH_ENDPOINT")
                  503 -> Mono.just("DOWN")
                  else -> Mono.just("ERROR")
                }
                else -> Mono.just("ERROR")
              }
            }
        }
      }
  }
}

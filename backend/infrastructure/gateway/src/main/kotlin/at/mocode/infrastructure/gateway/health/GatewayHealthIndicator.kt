package at.mocode.infrastructure.gateway.health

import org.springframework.boot.health.contributor.Health
import org.springframework.boot.health.contributor.ReactiveHealthIndicator
import org.springframework.cloud.client.ServiceInstance
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.Duration
import java.util.concurrent.TimeoutException

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
    private const val PARALLELISM = 8
  }

  override fun health(): Mono<Health> {
    val builder = Health.up()
    val details = mutableMapOf<String, Any>()

    return Mono.fromCallable { discoveryClient.services }
      .subscribeOn(Schedulers.boundedElastic())
      .flatMapMany { services: List<String> ->
        details["totalServices"] = services.size
        Flux.fromIterable(services)
      }
      .flatMap({ serviceName: String ->
        Mono.fromCallable { discoveryClient.getInstances(serviceName) }
          .subscribeOn(Schedulers.boundedElastic())
          .flatMap { instances: List<ServiceInstance> ->
            val instanceDetails = mapOf(
              "instanceCount" to instances.size,
              "instances" to instances.map { "${it.host}:${it.port}" }
            )

            val checkMono: Mono<String> = when {
              CRITICAL_SERVICES.contains(serviceName) || OPTIONAL_SERVICES.contains(serviceName) ->
                checkServiceHealthReactive(serviceName, instances)
              else -> Mono.just("SKIPPED")
            }
            checkMono.map { status -> Triple(serviceName, status, instanceDetails) }
          }
      }, PARALLELISM)
      .collectList()
      .flatMap { results: List<Triple<String, String, Map<String, Any>>> ->
        val discoveredServices = mutableMapOf<String, Any>()
        val criticalServiceStatus = mutableMapOf<String, String>()
        val optionalServiceStatus = mutableMapOf<String, String>()

        results.forEach { (serviceName, status, instanceDetails) ->
          discoveredServices[serviceName] = instanceDetails
          when {
            CRITICAL_SERVICES.contains(serviceName) -> criticalServiceStatus[serviceName] = status
            OPTIONAL_SERVICES.contains(serviceName) -> optionalServiceStatus[serviceName] = status
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
            isDevEnvironment -> "Gesundheitsprüfung erfolgreich (Entwicklungsumgebung)"
            else -> "Alle kritischen Services sind verfügbar"
          }
        }

        Mono.just(builder.withDetails(details).build())
      }
      .onErrorResume { ex ->
        Mono.just(
          Health.down(ex)
            .withDetail("status", "DOWN")
            .withDetail("reason", "Fehler bei der Service-Prüfung: ${ex.message}")
            .build()
        )
      }
  }

  private fun checkServiceHealthReactive(serviceName: String, instances: List<ServiceInstance>): Mono<String> {
    if (instances.isEmpty()) {
      return Mono.just("NO_INSTANCES")
    }

    // Wir prüfen exemplarisch die erste Instanz
    val instance = instances.first()
    val healthUrl = "http://${instance.host}:${instance.port}/actuator/health"

    return webClient.get()
      .uri(healthUrl)
      .retrieve()
      .bodyToMono(Map::class.java)
      .timeout(HEALTH_CHECK_TIMEOUT)
      .map { it["status"]?.toString() ?: "UNKNOWN" }
      .map { status -> if (status.equals("UP", ignoreCase = true)) "UP" else "DOWN" }
      .onErrorResume { ex ->
        when (ex) {
          is WebClientResponseException -> when (ex.statusCode.value()) {
            404 -> Mono.just("NO_HEALTH_ENDPOINT")
            503 -> Mono.just("DOWN")
            else -> Mono.just("ERROR")
          }
          is TimeoutException -> Mono.just("TIMEOUT")
          else -> Mono.just("ERROR")
        }
      }
  }
}

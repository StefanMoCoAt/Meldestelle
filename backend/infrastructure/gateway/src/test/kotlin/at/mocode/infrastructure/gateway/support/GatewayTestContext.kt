package at.mocode.infrastructure.gateway.support

import at.mocode.infrastructure.gateway.MinimalTestApp
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.security.oauth2.resource.reactive.ReactiveOAuth2ResourceServerAutoConfiguration
import org.springframework.boot.http.client.autoconfigure.HttpClientAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Profile
import org.springframework.test.context.ActiveProfiles
import org.springframework.context.annotation.Import

/**
 * Zentrale Meta-Annotation für Gateway-Tests.
 *
 * - Lädt einen minimalen Spring-Boot-Kontext über `MinimalTestApp`.
 * - Erzwingt das `test`-Profil.
 * - Schließt laute/unnötige Auto-Konfigurationen für schnelle, stabile Context-Loads aus.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@SpringBootTest(
  classes = [MinimalTestApp::class],
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  properties = [
    // Cloud/Discovery im Test deaktivieren
    "spring.cloud.discovery.enabled=false",
    "spring.cloud.consul.enabled=false",
    "spring.cloud.consul.config.enabled=false",
    "spring.cloud.consul.discovery.register=false",
    "spring.cloud.loadbalancer.enabled=false",
    // Circuit Breaker Health aus
    "resilience4j.circuitbreaker.configs.default.registerHealthIndicator=false",
    "management.health.circuitbreakers.enabled=false",
    // Gateway Discovery Locator aus
    "spring.cloud.gateway.discovery.locator.enabled=false",
    // Reaktiven Web‑Stack initialisieren (für WebTestClient)
    "spring.main.web-application-type=reactive",
    // Zufälliger Port verhindert Port-Konflikte
    "server.port=0"
  ]
)
@ActiveProfiles("test")
@ImportAutoConfiguration(
  exclude = [
    // Nur die wirklich lauten/unnötigen Auto‑Configs im Default‑Testprofil deaktivieren
    // Spring Cloud Refresh (verursachte CNF in früheren Läufen)
    org.springframework.cloud.autoconfigure.RefreshAutoConfiguration::class,
    // Security Resource Server (Keycloak) für die meisten Tests nicht nötig
    ReactiveOAuth2ResourceServerAutoConfiguration::class
  ]
)
@Profile("test")
annotation class GatewayTestContext

package at.mocode.infrastructure.gateway

import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration
import org.springframework.boot.http.client.autoconfigure.HttpClientAutoConfiguration
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration
import org.springframework.boot.autoconfigure.security.oauth2.resource.reactive.ReactiveOAuth2ResourceServerAutoConfiguration
import org.springframework.context.annotation.ComponentScan

/**
 * Test-spezifische, minimale GatewayApplication. Diese Klasse überschattet die Produktions-
 * `GatewayApplication` während der Tests und deaktiviert problematische Auto-Konfigurationen,
 * lädt aber weiterhin unsere Komponenten aus dem Gateway-Paket.
 */
@SpringBootConfiguration
@ComponentScan(basePackages = ["at.mocode.infrastructure.gateway"])
@ImportAutoConfiguration(
  exclude = [
    // Spring Cloud Refresh/Context (CNF in Tests vermeiden)
    RefreshAutoConfiguration::class,
    // HTTP/WebClient in Basis-Context-Load-Tests nicht erforderlich
    HttpClientAutoConfiguration::class,
    WebClientAutoConfiguration::class,
    // Security AutoConfigs minimieren
    ReactiveOAuth2ResourceServerAutoConfiguration::class,
    SecurityAutoConfiguration::class,
    ReactiveSecurityAutoConfiguration::class
  ]
)
class GatewayApplication

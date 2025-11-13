package at.mocode.infrastructure.gateway

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Hauptanwendung des API-Gateways.
 *
 * Zweck:
 * - Startpunkt für die Spring-Boot-Anwendung (WebFlux + Spring Cloud Gateway).
 * - Bindet auto-konfigurierte Komponenten (Routing, Security, Discovery).
 *
 * Hinweise:
 * - Aktive Spring-Profile werden über die Umgebungsvariable
 *   SPRING_PROFILES_ACTIVE gesetzt (Fallback: dev, siehe application.yml).
 * - Typische Profile: "keycloak" (Security) und optional "docker" (Container-Defaults).
 */
@SpringBootApplication
class GatewayApplication

/**
 * Startet die Spring Boot Anwendung.
 * @param args Kommandozeilenargumente, z. B. --server.port=8081
 */
fun main(args: Array<String>) {
    runApplication<GatewayApplication>(*args)
}

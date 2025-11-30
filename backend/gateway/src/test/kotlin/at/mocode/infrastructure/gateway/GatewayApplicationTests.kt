package at.mocode.infrastructure.gateway

import at.mocode.infrastructure.gateway.config.TestSecurityConfig
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

/**
 * Basis-Test zur Überprüfung, dass der Gateway-Anwendungskontext erfolgreich lädt.
 * Verwendet Test-Profil um Produktions-Filter und externe Abhängigkeiten zu deaktivieren.
 */
@SpringBootTest(
    classes = [GatewayApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        // Alle externen Abhängigkeiten für Context-Loading-Test deaktivieren
        "spring.cloud.discovery.enabled=false",
        "spring.cloud.consul.enabled=false",
        "spring.cloud.consul.config.enabled=false",
        "spring.cloud.consul.discovery.register=false",
        "spring.cloud.loadbalancer.enabled=false",
        // Circuit Breaker für Tests deaktivieren
        "resilience4j.circuitbreaker.configs.default.registerHealthIndicator=false",
        "management.health.circuitbreakers.enabled=false",
        // Custom Security und Filter deaktivieren
        "gateway.security.jwt.enabled=false",
        // Reaktiven Web-Anwendungstyp verwenden
        "spring.main.web-application-type=reactive",
        // Gateway Discovery deaktivieren
        "spring.cloud.gateway.server.webflux.discovery.locator.enabled=false",
        // Actuator Security deaktivieren
        "management.security.enabled=false",
        // Zufälligen Port setzen
        "server.port=0"
    ]
)
@ActiveProfiles("test")
@Import(TestSecurityConfig::class)
class GatewayApplicationTests {

    @Test
    fun contextLoads() {
        // Dieser Test ist erfolgreich, wenn der Spring-Anwendungskontext erfolgreich lädt
        // ohne Konfigurationsfehler oder fehlende Bean-Abhängigkeiten
    }
}

package at.mocode.infrastructure.auth

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Hauptklasse für den Auth-Server.
 *
 * Dieser Service fungiert als zentraler Authentifizierungs- und Autorisierungsserver,
 * der mit Keycloak kommuniziert und JWT-Token-Management bereitstellt.
 *
 * Funktionalitäten:
 * - JWT Token Generation und Validierung
 * - Integration mit Keycloak
 * - Benutzer- und Berechtigungsverwaltung
 * - REST API für Authentifizierung
 */
@SpringBootApplication
class AuthServerApplication

/**
 * Haupteinstiegspunkt für den Auth-Server Service
 */
fun main(args: Array<String>) {
    runApplication<AuthServerApplication>(*args)
}

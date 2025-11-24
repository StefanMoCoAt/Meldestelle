package at.mocode.clients.shared

/**
 * Zentrale App-Konfiguration für alle Client-Module.
 * Hinweis: Diese Werte sind zentrale Defaults für DEV. Für PROD sollten sie
 * via Build-Injektion (Gradle/ENV) überschrieben werden. Ein einfaches
 * BuildConfig-Setup kann später ergänzt werden.
 */
object AppConfig {
    // Gateway Basis-URL (API Gateway)
    const val GATEWAY_URL: String = "http://localhost:8081"

    // Keycloak Konfiguration
    const val KEYCLOAK_URL: String = "http://localhost:8180"
    const val KEYCLOAK_REALM: String = "meldestelle"
    const val KEYCLOAK_CLIENT_ID: String = "meldestelle-frontend"
}

// Dieses Modul stellt gemeinsame technische Hilfsfunktionen bereit,
// wie z.B. Konfigurations-Management, Datenbank-Verbindungen und Service Discovery.
plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    // Abhängigkeit zum platform-Modul für zentrale Versionsverwaltung
    api(projects.platform.platformDependencies)
    // Abhängigkeit zum core-domain-Modul, um dessen Typen zu verwenden
    api(projects.core.coreDomain)

    // Asynchronität
    api(libs.kotlinx.coroutines.core)

    // Datenbank-Management
    // OPTIMIERUNG: Verwendung von Bundles für Exposed und Flyway
    api(libs.bundles.exposed)
    api(libs.bundles.flyway)
    api(libs.hikari.cp)

    // Service Discovery
    api(libs.consul.client)

    // Logging
    api(libs.kotlin.logging.jvm)

    // Utilities
    api(libs.bignum) // Für BigDecimal Serialisierung

    // Testing
    testImplementation(projects.platform.platformTesting)
    testImplementation(libs.bundles.testing.jvm)
}

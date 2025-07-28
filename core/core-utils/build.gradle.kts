plugins {
    kotlin("jvm")
}

dependencies {
    api(projects.platform.platformDependencies)

    // Explizite `api`-Abhängigkeit zum core-domain Modul.
    api(projects.core.coreDomain)

    // --- Coroutines & Asynchronität ---
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // --- Datenbank-Management ---
    api("org.jetbrains.exposed:exposed-core")
    api("org.jetbrains.exposed:exposed-dao")
    api("org.jetbrains.exposed:exposed-jdbc")
    api("org.jetbrains.exposed:exposed-kotlin-datetime")
    api("com.zaxxer:HikariCP")
    // Flyway Core-Bibliothek
    api("org.flywaydb:flyway-core:9.22.3")
    // KORREKTUR: Spezifischer Treiber für PostgreSQL mit Versionsnummer
    api("org.flywaydb:flyway-database-postgresql:9.22.3")

    // --- Service Discovery ---
    api("com.orbitz.consul:consul-client:1.5.3")

    // --- Testing ---
    testImplementation(projects.platform.platformTesting)
}

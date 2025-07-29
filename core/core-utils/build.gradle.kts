plugins {
    kotlin("jvm")
}

dependencies {
    // Verwendet die zentrale Platform-BOM für konsistente Versionen
    api(projects.platform.platformDependencies)

    // Explizite `api`-Abhängigkeit zum core-domain Modul.
    api(projects.core.coreDomain)

    // --- Coroutines & Asynchronität ---
    api(libs.kotlinx.coroutines.core)

    // --- Datenbank-Management ---
    api(libs.exposed.core)
    api(libs.exposed.dao)
    api(libs.exposed.jdbc)
    api(libs.exposed.kotlin.datetime)
    api(libs.hikari.cp)
    api(libs.flyway.core)
    api(libs.flyway.postgresql)

    // --- Service Discovery ---
    api(libs.consul.client)

    // --- Testing ---
    testImplementation(projects.platform.platformTesting)
}

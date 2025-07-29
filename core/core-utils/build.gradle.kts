plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    api(projects.platform.platformDependencies)
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

    // --- Utilities ---
    // KORREKTUR: Fehlende Abhängigkeit für den BigDecimalSerializer hinzugefügt.
    api(libs.bignum)

    // --- Testing ---
    testImplementation(projects.platform.platformTesting)
}

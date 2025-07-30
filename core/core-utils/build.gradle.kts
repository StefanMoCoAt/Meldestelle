plugins {
    // Wendet das Kotlin JVM Plugin über den zentralen Alias an.
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    // Stellt sicher, dass dieses Modul die zentralen Versionen aus unserem BOM respektiert.
    api(projects.platform.platformDependencies)

    // Stellt die reinen Domänen-Klassen und -Interfaces aus dem `core-domain`-Modul bereit.
    // `api` ist hier zwingend, da `core-utils` eine Implementierung von `core-domain` ist.
    api(projects.core.coreDomain)

    // --- Coroutines & Asynchronität ---
    api(libs.kotlinx.coroutines.core)

    // --- Datenbank-Management ---
    api(libs.exposed.core)
    api(libs.exposed.dao)
    api(libs.exposed.jdbc)
    api(libs.exposed.kotlin.datetime) // exposed-kotlin-datetime -> exposed.kotlin.datetime
    // KORREKTUR: Der Alias `hikari-cp` wird zu `hikariCp` umgewandelt.
    api(libs.hikari.cp)
    api(libs.flyway.core)
    api(libs.flyway.postgresql)

    // --- Service Discovery ---
    api(libs.consul.client)

    // --- Utilities ---
    // Stellt die BigDecimal-Implementierung für den Serializer bereit.
    api(libs.bignum)

    // --- Testing ---
    testImplementation(projects.platform.platformTesting)
}

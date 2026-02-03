// Dieses Modul definiert die "Bill of Materials" (BOM) für das gesamte Projekt.
// Es nutzt das `java-platform`-Plugin, um eine zentrale Liste von Abhängigkeitsversionen
// zu erstellen, die von allen anderen Modulen importiert wird.
// Dies ist die ultimative "Single Source of Truth" für Versionen.
plugins {
    `java-platform`
    `maven-publish` // Nützlich, falls die BOM extern veröffentlicht werden soll
}

javaPlatform {
    // Erlaubt die Deklaration von Abhängigkeiten in einer Plattform.
    allowDependencies()
}

dependencies {
    // Importiert andere wichtige BOMs. Die Versionen werden durch diese
    // importierten Plattformen transitiv verwaltet.
    api(platform(libs.spring.boot.dependencies))
    api(platform(libs.spring.cloud.dependencies))
    api(platform(libs.kotlin.bom))
    api(platform(libs.kotlinx.coroutines.bom))

    // `constraints` erzwingt spezifische Versionen für einzelne Bibliotheken.
    // Alle Versionen werden sicher aus `libs.versions.toml` bezogen.
    constraints {
        // --- Utilities & Other ---
        api(libs.caffeine)
        api(libs.reactor.kafka)
        api(libs.redisson)
        api(libs.bignum)
        api(libs.spring.cloud.starter.consul.discovery)
        api(libs.kotlin.logging.jvm)
        api(libs.jakarta.annotation.api)
        api(libs.auth0.java.jwt)

        // Logging: WICHTIG! Core und Classic müssen synchronisiert sein.
        api(libs.logback.classic)
        api(libs.logback.core)

        // --- Spring & SpringDoc ---
        api(libs.springdoc.openapi.starter.common)
        api(libs.springdoc.openapi.starter.webflux.ui)

        // --- Database & Persistence ---
        api(libs.exposed.core)
        api(libs.exposed.dao)
        api(libs.exposed.jdbc)
        api(libs.exposed.kotlin.datetime)

        api(libs.flyway.core)
        api(libs.flyway.postgresql)

        api(libs.postgresql.driver)
        api(libs.hikari.cp)
        api(libs.h2.driver)
        api(libs.lettuce.core)

        // --- Kotlinx Libraries ---
        api(libs.kotlinx.serialization.json)
        api(libs.kotlinx.datetime)

        // --- Jackson Modules ---
        api(libs.jackson.module.kotlin)
        api(libs.jackson.datatype.jsr310)

        // --- Ktor OpenAPI ---
        api("io.ktor:ktor-server-routing-openapi:${libs.versions.ktor.get()}")

        // --- Testcontainers ---
        api(libs.testcontainers.core)
        api(libs.testcontainers.junit.jupiter)
        api(libs.testcontainers.postgresql)
        api(libs.testcontainers.keycloak)
    }
}

// Konfiguration für das Veröffentlichen der BOM (optional, aber gute Praxis).
publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["javaPlatform"])
        }
    }
}

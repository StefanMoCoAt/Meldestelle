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
    api(platform(libs.spring.cloud.dependencies)) // NEU: Spring Cloud BOM hinzugefügt
    api(platform(libs.kotlin.bom))
    api(platform(libs.kotlinx.coroutines.bom))
    // `constraints` erzwingt spezifische Versionen für einzelne Bibliotheken.
    // Alle Versionen werden sicher aus `libs.versions.toml` bezogen.
    constraints {
        // --- Spring Boot Core Constraints wurden entfernt. ---
        // Die Versionen werden jetzt vollständig durch das importierte Spring Boot BOM verwaltet.
        // Das ist der saubere und empfohlene Weg.

        // --- Utilities & Other ---
        api(libs.caffeine)
        api(libs.reactor.kafka)
        api(libs.redisson)
        // Removed the legacy UUID library constraint (com.benasher44:uuid) since project uses Kotlin stdlib UUID
        api(libs.bignum)
        // api(libs.consul.client) wird getauscht mir spring-cloud-starter-consul-discovery
        api(libs.spring.cloud.starter.consul.discovery)
        api(libs.kotlin.logging.jvm)
        api(libs.jakarta.annotation.api)
        api(libs.auth0.java.jwt)
        api(libs.logback.classic)
        // --- Spring & SpringDoc ---
        api(libs.springdoc.openapi.starter.common)
        // KORREKTUR: `webmvc`-Starter durch `webflux`-Starter ersetzt, um Konflikt zu beheben.
        api(libs.springdoc.openapi.starter.webflux.ui)
        // --- Database & Persistence ---
        // CHIRURGISCHER EINGRIFF: `exposed`-Bundle entfernt, um Kotlin-Versionskonflikt zu beheben.
        // api(libs.bundles.exposed)
        api(libs.bundles.flyway)
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
        // --- Testcontainers ---
        api(libs.bundles.testcontainers)
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

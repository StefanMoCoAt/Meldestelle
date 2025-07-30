plugins {
    `java-platform`
    `maven-publish`
}

javaPlatform {
    allowDependencies()
}

dependencies {
    // KORREKTUR: Alle BOMs werden jetzt über Aliase aus der libs.versions.toml bezogen.
    api(platform(libs.spring.boot.dependencies))
    api(platform(libs.kotlin.bom))
    api(platform(libs.kotlinx.coroutines.bom))

    constraints {
        // KORREKTUR: Alle Abhängigkeiten verwenden jetzt Aliase.
        // Keine einzige hartcodierte Version mehr in dieser Datei!

        // --- Utilities & Other ---
        api(libs.caffeine)
        api(libs.reactor.kafka)
        api(libs.redisson)
        api(libs.uuid)
        api(libs.bignum)
        api(libs.consul.client)
        api(libs.kotlin.logging.jvm)
        api(libs.jakarta.annotation.api)

        // --- Spring & SpringDoc ---
        api(libs.springdoc.openapi.starter.common)
        api(libs.springdoc.openapi.starter.webmvc.ui)

        // --- Database & Persistence ---
        api(libs.exposed.core)
        api(libs.exposed.dao)
        api(libs.exposed.jdbc)
        api(libs.exposed.kotlin.datetime)
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
        api(libs.testcontainers.core)
        api(libs.testcontainers.junit.jupiter)
        api(libs.testcontainers.postgresql)
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["javaPlatform"])
        }
    }
}

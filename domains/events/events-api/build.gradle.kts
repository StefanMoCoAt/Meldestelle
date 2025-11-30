plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktor)
    application
    // KORREKTUR 1: Dieses Plugin hinzufügen, um die Spring-BOM zu aktivieren.
    alias(libs.plugins.spring.dependencyManagement)
}

application {
    mainClass.set("at.mocode.events.api.ApplicationKt")
}

dependencies {
    // KORREKTUR 2: Die Spring-Boot-BOM hier explizit als Plattform deklarieren.
    api(platform(libs.spring.boot.dependencies))
    // Bestehende Abhängigkeiten
    implementation(projects.platform.platformDependencies)
    implementation(projects.events.eventsDomain)
    implementation(projects.events.eventsApplication)
    implementation(projects.core.coreDomain)
    implementation(projects.core.coreUtils)
    // Spring dependencies (jetzt mit korrekter Version aus der BOM)
    implementation(libs.spring.web)
    implementation(libs.springdoc.openapi.starter.common)
    // Ktor Server
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.contentNegotiation)
    implementation(libs.ktor.server.serialization.kotlinx.json)
    implementation(libs.ktor.server.statusPages)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.authJwt)

    testImplementation(projects.platform.platformTesting)
    testImplementation(libs.ktor.server.tests)
}

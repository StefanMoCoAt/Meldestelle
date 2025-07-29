plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktor)
    application
}

application {
    mainClass.set("at.mocode.infrastructure.gateway.ApplicationKt")
}

dependencies {
    // --- Interne Module ---
    // Der Gateway benötigt nur die Kern-Definitionen und Utilities.
    implementation(projects.platform.platformDependencies)
    implementation(projects.core.coreDomain)
    implementation(projects.core.coreUtils)
    // Der Gateway nutzt den Auth-Client, um Tokens zu validieren.
    implementation(projects.infrastructure.auth.authClient)
    implementation(projects.infrastructure.monitoring.monitoringClient)

    // !!! WICHTIG: KEINE direkten Abhängigkeiten zu den Domänen- oder
    // Infrastruktur-Modulen der Backend-Services mehr!

    // --- Ktor Server ---
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.contentNegotiation)
    implementation(libs.ktor.server.serialization.kotlinx.json)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.callLogging)
    implementation(libs.ktor.server.defaultHeaders)
    implementation(libs.ktor.server.statusPages)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.authJwt)
    implementation(libs.ktor.server.rateLimit)
    implementation(libs.ktor.server.metrics.micrometer)

    // --- OpenAPI & Swagger for Ktor ---
    implementation(libs.ktor.server.openapi)
    implementation(libs.ktor.server.swagger)

    // --- Ktor Client (damit der Gateway Anfragen an die Backend-Services weiterleiten kann) ---
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio) // CIO ist eine gute, asynchrone Engine
    implementation(libs.ktor.client.contentNegotiation)
    implementation(libs.ktor.client.serialization.kotlinx.json)

    // --- Monitoring ---
    implementation(libs.micrometer.prometheus)

    // --- Testing ---
    testImplementation(projects.platform.platformTesting)
    testImplementation(libs.ktor.server.tests)
}

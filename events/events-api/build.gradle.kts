plugins {
    // KORREKTUR: Alle Plugins werden jetzt konsistent über den Version Catalog geladen.
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktor)
    application
}

application {
    mainClass.set("at.mocode.events.api.ApplicationKt")
}

dependencies {
    // Deine Abhängigkeiten sind hier bereits korrekt und benötigen keine Änderung.
    implementation(projects.platform.platformDependencies)

    implementation(projects.events.eventsDomain)
    implementation(projects.events.eventsApplication)
    implementation(projects.core.coreDomain)
    implementation(projects.core.coreUtils)

    // Spring dependencies
    implementation("org.springframework:spring-web")
    implementation("org.springdoc:springdoc-openapi-starter-common")

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

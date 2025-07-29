plugins {
    // KORREKTUR: Alle Plugins werden jetzt konsistent über den Version Catalog geladen.
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktor)
    application
}

application {
    mainClass.set("at.mocode.masterdata.api.ApplicationKt")
}

dependencies {
    // Interne Module
    implementation(projects.platform.platformDependencies)
    implementation(projects.masterdata.masterdataDomain)
    implementation(projects.masterdata.masterdataApplication)
    implementation(projects.core.coreDomain)
    implementation(projects.core.coreUtils)

    // KORREKTUR: Alle externen Abhängigkeiten werden jetzt über den Version Catalog bezogen.

    // Spring dependencies
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

    // Testing
    testImplementation(projects.platform.platformTesting)
    testImplementation(libs.ktor.server.tests)
}

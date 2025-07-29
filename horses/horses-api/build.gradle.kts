plugins {
    // KORREKTUR: Alle Plugins werden jetzt konsistent über den Version Catalog geladen.
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.serialization)

    // Das Ktor-Plugin wird hier nicht benötigt, da Ktor als Bibliothek in Spring Boot läuft.
    // Das 'application'-Plugin wird vom Spring Boot Plugin bereitgestellt.
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependencyManagement)
}

// Der springBoot-Block konfiguriert die Anwendung, wenn sie als JAR-Datei ausgeführt wird.
springBoot {
    mainClass.set("at.mocode.horses.api.ApplicationKt")
}

dependencies {
    // Interne Module
    implementation(projects.platform.platformDependencies)
    implementation(projects.horses.horsesDomain)
    implementation(projects.horses.horsesApplication)
    implementation(projects.core.coreDomain)
    implementation(projects.core.coreUtils)

    // KORREKTUR: Alle externen Abhängigkeiten werden jetzt über den Version Catalog bezogen.

    // Spring dependencies
    implementation(libs.spring.web)

    // Ktor Server (als embedded Server in Spring)
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
    testImplementation(libs.spring.boot.starter.test)
}

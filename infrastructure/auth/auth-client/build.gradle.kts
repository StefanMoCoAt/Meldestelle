// Dieses Modul enthält die clientseitige Logik für die Authentifizierung.
// Es stellt Konfigurationen und Beans bereit, um mit einem OAuth2/OIDC-Provider
// wie Keycloak zu interagieren und JWTs zu validieren.
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependencyManagement)
}

// Deaktiviert die Erstellung eines ausführbaren Jars für dieses Bibliotheks-Modul.
tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = false
}

// Stellt sicher, dass stattdessen ein reguläres Jar gebaut wird.
tasks.getByName<org.gradle.api.tasks.bundling.Jar>("jar") {
    enabled = true
}

dependencies {
    // Stellt sicher, dass alle Versionen aus der zentralen BOM kommen.
    implementation(platform(projects.platform.platformBom))

    // Stellt gemeinsame Abhängigkeiten wie Coroutines und Logging bereit.
    implementation(projects.platform.platformDependencies)

    // Stellt Domänenobjekte und technische Utilities bereit.
    implementation(projects.core.coreUtils)

    // Spring Security für OAuth2-Client-Funktionalität und JWT-Verarbeitung.
    implementation(libs.spring.boot.starter.oauth2.client)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.security.oauth2.jose)

    // Bibliothek zur einfachen Handhabung von JWTs.
    implementation(libs.auth0.java.jwt)

    // Stellt alle Test-Abhängigkeiten gebündelt bereit.
    testImplementation(projects.platform.platformTesting)
}

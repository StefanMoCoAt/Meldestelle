// Dieses Modul ist ein eigenständiger Spring Boot Service, der als
// zentraler Authentifizierung- und Autorisierungs-Server agiert.
// Er kommuniziert mit Keycloak und stellt Endpunkte für die Benutzerverwaltung bereit.
plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSpring)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependencyManagement)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

// Konfiguriert die Hauptklasse für das ausführbare JAR.
springBoot {
    mainClass.set("at.mocode.infrastructure.auth.AuthServerApplicationKt")
}

dependencies {
    // Stellt sicher, dass alle Versionen aus der zentralen BOM kommen.
    implementation(platform(projects.platform.platformBom))
    // Stellt gemeinsame Abhängigkeiten bereit.
    implementation(projects.platform.platformDependencies)
    // Nutzt die Client-Logik für die Kommunikation mit Keycloak.
    implementation(projects.infrastructure.auth.authClient)
    // Spring Boot Starter für einen Web-Service.
    // OPTIMIERUNG: Verwendung des `spring-boot-essentials`-Bundles.
    implementation(libs.bundles.spring.boot.essentials)
    // Spring Security für die Absicherung des Servers.
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.oauth2.resource.server)
    // Keycloak Admin Client zur Verwaltung von Benutzern und Realms.
    implementation(libs.keycloak.admin.client)
    // API-Dokumentation mit OpenAPI/Swagger.
    implementation(libs.springdoc.openapi.starter.webmvc.ui)
    // Monitoring und Metriken für Production-Readiness.
    implementation(libs.bundles.monitoring.client)
    // JSON-Serialization für API-Responses.
    implementation(libs.kotlinx.serialization.json)
    // Stellt alle Test-Abhängigkeiten gebündelt bereit.
    testImplementation(projects.platform.platformTesting)
    // Testcontainers für Integration Tests
    testImplementation(libs.bundles.testcontainers)
    // SLF4J provider for tests
    testImplementation(libs.logback.classic)
    testImplementation(libs.logback.core)
}

tasks.test {
    useJUnitPlatform()
}

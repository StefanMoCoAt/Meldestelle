// Dieses Modul ist ein eigenständiger Spring Boot Service, der als
// zentraler Authentifizierungs- und Autorisierungs-Server agiert.
// Er kommuniziert mit Keycloak und stellt Endpunkte für die Benutzerverwaltung bereit.
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependencyManagement)
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


    // Stellt alle Test-Abhängigkeiten gebündelt bereit.
    testImplementation(projects.platform.platformTesting)
}

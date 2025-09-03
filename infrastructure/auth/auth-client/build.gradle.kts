// Dieses Modul enthält die clientseitige Logik für die Authentifizierung.
// Es stellt Konfigurationen und Beans bereit, um mit einem OAuth2/OIDC-Provider
// wie Keycloak zu interagieren und JWTs zu validieren.
plugins {
    `java-library`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.spring.dependencyManagement)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.test {
    useJUnitPlatform()
}

java {
    withJavadocJar()
    withSourcesJar()
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

    // JSON-Serialization für konsistente API-Datenverarbeitung.
    implementation(libs.kotlinx.serialization.json)

    // Stellt alle Test-Abhängigkeiten gebündelt bereit.
    testImplementation(projects.platform.platformTesting)
}

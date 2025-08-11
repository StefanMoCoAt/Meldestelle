// Dieses Modul ist das API-Gateway und der einzige öffentliche Einstiegspunkt
// für alle externen Anfragen an das Meldestelle-System.
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependencyManagement)
}

// Konfiguriert die Hauptklasse für das ausführbare JAR.
springBoot {
    mainClass.set("at.mocode.infrastructure.gateway.GatewayApplicationKt")
}

dependencies {
    // Stellt sicher, dass alle Versionen aus der zentralen BOM kommen.
    implementation(platform(projects.platform.platformBom))
    // Stellt Utilities bereit
    implementation(projects.core.coreUtils)
    // Stellt gemeinsame Abhängigkeiten bereit.
    implementation(projects.platform.platformDependencies)

    // Stellt die Spring Cloud Gateway und Consul Discovery Abhängigkeiten bereit
    implementation(libs.bundles.spring.cloud.gateway)
    // Sichert den reaktiven Webserver (Netty) explizit ab, um Test-/Kontext-Probleme zu vermeiden
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // Bindet die wiederverwendbare Logik zur JWT-Validierung ein.
    implementation(projects.infrastructure.auth.authClient)

    // Bindet die wiederverwendbare Logik für Metriken und Tracing ein.
    implementation(projects.infrastructure.monitoring.monitoringClient)

    // Stellt alle Test-Abhängigkeiten gebündelt bereit.
    testImplementation(projects.platform.platformTesting)
    testImplementation(libs.bundles.testing.jvm)
    // Security im Testkontext, um eine permissive Security-Konfiguration bereitstellen zu können
    testImplementation(libs.spring.boot.starter.security)

}

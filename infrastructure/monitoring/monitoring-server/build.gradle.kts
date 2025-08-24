// Dieses Modul ist ein eigenständiger Spring Boot Service, der den
// Zipkin-Server mit seiner UI hostet, um Tracing-Daten zu visualisieren.
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependencyManagement)
}

// Konfiguriert die Hauptklasse für das ausführbare JAR.
springBoot {
    mainClass.set("at.mocode.infrastructure.monitoring.MonitoringServerApplicationKt")
}

dependencies {
    // Stellt sicher, dass alle Versionen aus der zentralen BOM kommen.
    implementation(platform(projects.platform.platformBom))
    // Stellt gemeinsame Abhängigkeiten bereit.
    implementation(projects.platform.platformDependencies)

    // Spring Boot Starter für einen einfachen Web-Service.
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.actuator)

    // Abhängigkeiten für den Zipkin-Server (UI ist via zipkin-lens bereits enthalten).
    implementation(libs.zipkin.server)
    // Prometheus client für Zipkin Metriken
    implementation(libs.micrometer.prometheus)

    // Stellt alle Test-Abhängigkeiten gebündelt bereit.
    testImplementation(projects.platform.platformTesting)
    testImplementation(libs.logback.classic)  // SLF4J provider for tests
}

tasks.test {
    useJUnitPlatform()
}

// Dieses Modul ist eine wiederverwendbare Bibliothek, die von jedem Microservice
// eingebunden wird, um Metriken und Tracing-Daten zu exportieren.
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.dependencyManagement)
}



dependencies {
    // Stellt sicher, dass alle Versionen aus der zentralen BOM kommen.
    implementation(platform(projects.platform.platformBom))
    // Stellt gemeinsame Abhängigkeiten bereit.
    implementation(projects.platform.platformDependencies)

    // OPTIMIERUNG: Verwendung des `monitoring-client`-Bundles.
    // Es enthält Spring Boot Actuator, Micrometer Prometheus und Zipkin Tracing.
    implementation(libs.bundles.monitoring.client)

    // Stellt alle Test-Abhängigkeiten gebündelt bereit.
    testImplementation(projects.platform.platformTesting)
}

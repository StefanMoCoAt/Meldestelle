// Dieses Modul stellt High-Level-Clients (Producer/Consumer) für die
// Interaktion mit Apache Kafka bereit. Es baut auf der `messaging-config` auf.
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependencyManagement)
}

dependencies {
    // Stellt sicher, dass alle Versionen aus der zentralen BOM kommen.
    implementation(platform(projects.platform.platformBom))
    // Stellt gemeinsame Abhängigkeiten bereit.
    implementation(projects.platform.platformDependencies)
    // Baut auf der zentralen Kafka-Konfiguration auf und erbt deren Abhängigkeiten.
    implementation(projects.infrastructure.messaging.messagingConfig)

    // Fügt die reaktive Kafka-Implementierung hinzu (Project Reactor).
    implementation(libs.reactor.kafka)

    // Stellt alle Test-Abhängigkeiten gebündelt bereit.
    testImplementation(projects.platform.platformTesting)
}

// Dieses Modul stellt High-Level-Clients (Producer/Consumer) für die
// Interaktion mit Apache Kafka bereit. Es baut auf der `messaging-config` auf.
plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSpring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependencyManagement)
}

// Deaktiviert die Erstellung eines ausführbaren Jars für dieses Bibliotheks-Modul.
tasks.getByName("bootJar") {
    enabled = false
}

// Deaktiviert bootRun und bootTestRun für dieses Bibliotheks-Modul, da es keine ausführbare Anwendung ist.
tasks.getByName("bootRun") {
    enabled = false
}

tasks.getByName("bootTestRun") {
    enabled = false
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

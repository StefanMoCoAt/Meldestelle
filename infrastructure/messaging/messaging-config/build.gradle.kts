// Dieses Modul stellt die zentrale, wiederverwendbare Konfiguration
// für die Verbindung mit Apache Kafka bereit (z.B. Bootstrap-Server, Serializer).
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependencyManagement)
}

// Deaktiviert die Erstellung eines ausführbaren Jars für dieses Bibliotheks-Modul.
tasks.getByName("bootJar") {
    enabled = false
}

dependencies {
    // Stellt sicher, dass alle Versionen aus der zentralen BOM kommen.
    api(platform(projects.platform.platformBom))
    // Stellt gemeinsame Abhängigkeiten bereit.
    api(projects.platform.platformDependencies)

    // OPTIMIERUNG: Verwendung des `kafka-config`-Bundles.
    // `api` wird verwendet, damit der `messaging-client` diese Konfigurationen
    // und Abhängigkeiten (wie Jackson) direkt nutzen kann.
    api(libs.bundles.kafka.config)

    // Stellt alle Test-Abhängigkeiten gebündelt bereit.
    testImplementation(projects.platform.platformTesting)
}

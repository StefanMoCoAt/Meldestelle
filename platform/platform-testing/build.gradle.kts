// Dieses Modul bündelt alle für JVM-Tests notwendigen Abhängigkeiten.
// Jedes Modul, das Tests enthält, sollte dieses Modul mit `testImplementation` einbinden.
plugins {
    alias(libs.plugins.kotlinJvm)
}

dependencies {
    // Importiert die zentrale BOM für konsistente Versionen.
    api(platform(projects.platform.platformBom))

    // Diese Bundles sind in `libs.versions.toml` definiert.
    api(libs.bundles.testing.jvm)
    api(libs.bundles.testcontainers)

    // Macht Kafka- und Reactor-Test-Bibliotheken verfügbar
    api(libs.testcontainers.kafka)
    api(libs.reactor.test)

    // Stellt Spring Boot Test-Abhängigkeiten und die H2-Datenbank für Tests bereit.
    api(libs.spring.boot.starter.test)
    api(libs.h2.driver)
}

tasks.withType<Test> {
    useJUnitPlatform()
    // Removed parallel execution and byte-buddy agent configuration to prevent conflicts
    // with global test configuration in root build.gradle.kts
}

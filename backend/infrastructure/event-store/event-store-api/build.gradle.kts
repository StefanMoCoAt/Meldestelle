// Dieses Modul definiert die provider-agnostische API für den Event Store.
// Es enthält die Interfaces (z.B. `EventStore`, `EventSerializer`) und die
// Domänen-Events aus `core-domain`, die gespeichert und publiziert werden.
plugins {
    alias(libs.plugins.kotlinJvm)
    // Für bessere IDE-Unterstützung und Dokumentation
    `java-library`
}

kotlin {
    compilerOptions {
        // Optimierungen für API-Module
        freeCompilerArgs.addAll(
            "-opt-in=kotlin.time.ExperimentalTime",
            "-Xjvm-default=all"
        )
    }
}

java {
    // Aktiviert die Erstellung von Source- und Javadoc-JARs für bessere API-Dokumentation
    withSourcesJar()
    withJavadocJar()
}

dependencies {
    // === Core Dependencies ===
    // Stellt sicher, dass alle Versionen aus der zentralen BOM kommen
    implementation(platform(projects.platform.platformBom))
    // Abhängigkeit zu den Core-Modulen, um auf Domänenobjekte (Events)
    // und technische Hilfsklassen zugreifen zu können
    api(projects.core.coreDomain)
    implementation(projects.core.coreUtils)
    // === Test Dependencies ===
    // Stellt alle Test-Abhängigkeiten gebündelt bereit
    testImplementation(projects.platform.platformTesting)
    testImplementation(libs.bundles.testing.jvm)
    // Für erweiterte Test-Unterstützung bei API-Tests
    testImplementation(libs.kotlinx.coroutines.test)
}

// === Task Configuration ===
// Optimiert die Test-Ausführung
tasks.test {
    useJUnitPlatform()
    // Parallelisierung für bessere Performance
    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
}

// Konfiguration für bessere JAR-Erstellung bei API-Modulen
tasks.jar {
    manifest {
        attributes(
            "Implementation-Title" to "Event Store API",
            "Implementation-Version" to project.version,
            "Automatic-Module-Name" to "at.mocode.infrastructure.eventstore.api"
        )
    }
}

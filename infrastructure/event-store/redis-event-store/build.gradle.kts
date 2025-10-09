// Dieses Modul stellt eine konkrete Implementierung der `event-store-api`
// unter Verwendung von Redis Streams als Event-Store-Backend bereit.
plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSpring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependencyManagement)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-opt-in=kotlin.time.ExperimentalTime",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=kotlin.uuid.ExperimentalUuidApi"
        )
    }
}

dependencies {
    // === Core Dependencies ===
    // Stellt sicher, dass alle Versionen aus der zentralen BOM kommen
    implementation(platform(projects.platform.platformBom))
    // Implementiert die provider-agnostische Event-Store-API
    api(projects.infrastructure.eventStore.eventStoreApi)
    // Benötigt Zugriff auf Core-Module für Domänen-Events und Utilities
    implementation(projects.core.coreDomain)
    implementation(projects.core.coreUtils)
    // === Redis & Spring Dependencies ===
    // OPTIMIERUNG: Wiederverwendung des `redis-cache`-Bundles, da es die
    // gleichen Technologien (Spring Data Redis, Lettuce, Jackson) verwendet
    implementation(libs.bundles.redis.cache)
    // Stellt Jakarta Annotations bereit (z. B. @PostConstruct), die von Spring verwendet werden
    implementation(libs.jakarta.annotation.api)
    // Für Kotlin-spezifische Coroutines-Integration mit Spring
    implementation(libs.kotlinx.coroutines.reactor)
    // === Test Dependencies ===
    // Fügt JUnit, Mockk, AssertJ etc. für die Tests hinzu
    testImplementation(projects.platform.platformTesting)
    testImplementation(libs.bundles.testing.jvm)
    testImplementation(libs.bundles.testcontainers)
    // Zusätzliche Test-Dependencies für erweiterte Event-Store-Tests
    testImplementation(libs.kotlinx.serialization.json)
    testImplementation(libs.reactor.test)
}

// === Task Configuration ===
// Deaktiviert die Erstellung eines ausführbaren Jars für dieses Bibliotheks-Modul
tasks.bootJar {
    enabled = false
}

// Stellt sicher, dass stattdessen ein reguläres Jar gebaut wird
tasks.jar {
    enabled = true
    archiveClassifier.set("")
}

// Optimiert die Test-Ausführung
tasks.test {
    useJUnitPlatform()
    // Verbesserte Test-Performance für Testcontainer
    systemProperty("testcontainers.reuse.enable", "true")
    // Parallelisierung für bessere Performance
    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
}

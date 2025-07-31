// Dieses Modul stellt eine konkrete Implementierung der `event-store-api`
// unter Verwendung von Redis Streams als Event-Store-Backend bereit.
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependencyManagement)
}

dependencies {
    // Stellt sicher, dass alle Versionen aus der zentralen BOM kommen.
    implementation(platform(projects.platform.platformBom))

    // Implementiert die provider-agnostische Event-Store-API.
    implementation(projects.infrastructure.eventStore.eventStoreApi)
    // Benötigt Zugriff auf Core-Module für Domänen-Events und Utilities.
    implementation(projects.core.coreDomain)
    implementation(projects.core.coreUtils)

    // OPTIMIERUNG: Wiederverwendung des `redis-cache`-Bundles, da es die
    // gleichen Technologien (Spring Data Redis, Lettuce, Jackson) verwendet.
    implementation(libs.bundles.redis.cache)

    // Stellt Jakarta Annotations bereit (z.B. @PostConstruct), die von Spring verwendet werden.
    implementation(libs.jakarta.annotation.api)

    // Stellt alle Test-Abhängigkeiten gebündelt bereit.
    testImplementation(projects.platform.platformTesting)
    // Stellt Testcontainers für Integrationstests mit einer echten Redis-Instanz bereit.
    testImplementation(libs.bundles.testcontainers)
}

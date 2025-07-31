// Dieses Modul stellt eine konkrete Implementierung der `cache-api`
// unter Verwendung von Redis als Caching-Backend bereit.
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

    // Implementiert die provider-agnostische Caching-API.
    implementation(projects.infrastructure.cache.cacheApi)

    // OPTIMIERUNG: Verwendung des `redis-cache`-Bundles aus libs.versions.toml.
    // Dieses Bundle enthält Spring Data Redis, Lettuce und Jackson-Module.
    implementation(libs.bundles.redis.cache)

    // Stellt alle Test-Abhängigkeiten gebündelt bereit.
    testImplementation(projects.platform.platformTesting)
    testImplementation(libs.bundles.testing.jvm)
    testImplementation(libs.kotlin.test)
}

// Dieses Modul definiert die provider-agnostische Caching-API.
// Es enthält nur Interfaces (z.B. `CacheService`) und Datenmodelle,
// aber keine konkrete Implementierung.
plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    // Stellt sicher, dass alle Versionen aus der zentralen BOM kommen.
    implementation(platform(projects.platform.platformBom))
    // Stellt gemeinsame Abhängigkeiten wie Logging bereit.
    implementation(projects.platform.platformDependencies)

    // Stellt Test-Abhängigkeiten bereit.
    testImplementation(projects.platform.platformTesting)
}

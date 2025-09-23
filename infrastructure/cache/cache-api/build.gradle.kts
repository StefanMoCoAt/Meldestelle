// Dieses Modul definiert die provider-agnostische Caching-API.
// Es enthält nur Interfaces (z.B. `CacheService`) und Datenmodelle,
// aber keine konkrete Implementierung.
plugins {
    alias(libs.plugins.kotlinJvm)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

// Erlaubt die Verwendung der kotlin.time API im gesamten Modul
kotlin {
    compilerOptions {
        freeCompilerArgs.add("-opt-in=kotlin.time.ExperimentalTime")
    }
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    // Stellt sicher, dass alle Versionen aus der zentralen BOM kommen.
    api(platform(projects.platform.platformBom))
    // Stellt gemeinsame Abhängigkeiten wie Logging bereit und exportiert sie für Konsumenten der API.
    api(projects.platform.platformDependencies)

    // Stellt Test-Abhängigkeiten bereit.
    testImplementation(projects.platform.platformTesting)
}

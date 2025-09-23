// Dieses Modul stellt eine konkrete Implementierung der `cache-api`
// unter Verwendung von Redis als Caching-Backend bereit.
plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSpring)
    // Als Bibliothek benötigt dieses Modul das Spring Boot Plugin nicht.
    alias(libs.plugins.spring.dependencyManagement)
}

// Stellt sicher, dass ein normales JAR gebaut wird (Bibliotheks-Modul).
java {
    withJavadocJar()
    withSourcesJar()
}

tasks.test {
    useJUnitPlatform()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
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
    testImplementation(libs.kotlin.logging.jvm)
    testImplementation(libs.logback.classic)
    testImplementation(libs.logback.core)
}

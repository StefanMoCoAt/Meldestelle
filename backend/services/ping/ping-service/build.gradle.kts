plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSpring)
    alias(libs.plugins.kotlinJpa)
    alias(libs.plugins.spring.boot)
}

kotlin {
    compilerOptions {
        // Aktiviert die experimentelle UUID API von Kotlin 2.3.0
        freeCompilerArgs.add("-opt-in=kotlin.uuid.ExperimentalUuidApi")
    }
}

dependencies {
    // === Project Dependencies ===
    implementation(projects.backend.services.ping.pingApi)
    implementation(projects.platform.platformDependencies)

    // === Spring Boot & Cloud ===
    implementation(libs.bundles.spring.boot.service.complete)
    // WICHTIG: Da wir JPA (blockierend) nutzen, brauchen wir Spring MVC (nicht WebFlux)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.bundles.spring.cloud.gateway) // Für Discovery Client

    // === Database & Persistence ===
    implementation(libs.bundles.database.complete)

    // === Resilience ===
    implementation(libs.bundles.resilience)

    // === Testing ===
    testImplementation(libs.bundles.testing.jvm)
}

tasks.test {
    useJUnitPlatform()
}

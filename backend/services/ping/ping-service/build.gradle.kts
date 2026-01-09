plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSpring)
    alias(libs.plugins.kotlinJpa)
    alias(libs.plugins.spring.boot)
}

kotlin {
    compilerOptions {
        // Aktiviert die experimentelle UUID-API von Kotlin 2.3.0
        freeCompilerArgs.add("-opt-in=kotlin.uuid.ExperimentalUuidApi")
    }
}

dependencies {
    // === Project Dependencies ===
    implementation(projects.backend.services.ping.pingApi)
    implementation(projects.platform.platformDependencies)
    // NEU: Zugriff auf die verschobenen DatabaseUtils
    implementation(projects.backend.infrastructure.persistence)

    // === Spring Boot & Cloud ===
    implementation(libs.bundles.spring.boot.service.complete)
    // WICHTIG: Da wir JPA (blockierend) nutzen, brauchen wir Spring MVC (nicht WebFlux)
    implementation(libs.spring.boot.starter.web)

    // Service Discovery
    implementation(libs.spring.cloud.starter.consul.discovery)

    // === Database & Persistence ===
    implementation(libs.bundles.database.complete)

    // === Resilience ===
    implementation(libs.resilience4j.spring.boot3)
    implementation(libs.resilience4j.reactor)
    implementation(libs.spring.boot.starter.aop)

    // === Testing ===
    testImplementation(libs.bundles.testing.jvm)
}

tasks.test {
    useJUnitPlatform()
}

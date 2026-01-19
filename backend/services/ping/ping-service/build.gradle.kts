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
    implementation(projects.contracts.pingApi)

    // Our central BOM for consistent versions
    implementation(platform(projects.platform.platformBom))
    implementation(projects.platform.platformDependencies)

    // Infrastructure Modules
    implementation(projects.backend.infrastructure.persistence)
    implementation(projects.backend.infrastructure.security)
    implementation(projects.backend.infrastructure.monitoring.monitoringClient) // NEU: Monitoring & Tracing

    // === Spring Boot & Cloud ===
    // Standard dependencies for a secure microservice
    implementation(libs.bundles.spring.boot.secure.service)
    // Common service extras
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.json)

    // === Database & Persistence ===
    implementation(libs.bundles.database.complete)

    // === Resilience ===
    implementation(libs.resilience4j.spring.boot3)
    implementation(libs.resilience4j.reactor)
    implementation(libs.spring.boot.starter.aop)

    // === Testing ===
    testImplementation(libs.bundles.testing.jvm)
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.security.test)
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        showStandardStreams = true
    }
}

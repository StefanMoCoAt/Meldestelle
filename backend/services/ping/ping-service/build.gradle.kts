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

    // === Spring Boot & Cloud ===
    implementation(libs.bundles.spring.boot.service.complete)
    // WICHTIG: Da wir JPA (blockierend) nutzen, brauchen wir Spring MVC (nicht WebFlux)
    implementation(libs.spring.boot.starter.web)

    // KORREKTUR: Bundle aufgelöst, da Accessor fehlschlägt
    // libs.bundles.spring.cloud.gateway -> spring-cloud-gateway
    implementation(libs.spring.cloud.starter.gateway.server.webflux)
    implementation(libs.spring.cloud.starter.consul.discovery)

    // === Database & Persistence ===
    implementation(libs.bundles.database.complete)

    // === Resilience ===
    // KORREKTUR: Bundle aufgelöst
    implementation(libs.resilience4j.spring.boot3)
    implementation(libs.resilience4j.reactor)
    implementation(libs.spring.boot.starter.aop)

    // === Testing ===
    testImplementation(libs.bundles.testing.jvm)
}

tasks.test {
    useJUnitPlatform()
}

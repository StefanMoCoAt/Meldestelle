// Optimized Spring Boot ping service for testing microservice architecture
// This service demonstrates circuit breaker patterns, service discovery, and monitoring
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependencyManagement)
}

// Build optimization settings
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

// Configure the main class for the executable JAR
springBoot {
    mainClass.set("at.mocode.temp.pingservice.PingServiceApplicationKt")
    buildInfo()
}

dependencies {
    // === Platform Dependencies ===
    // Ensure all versions come from the central BOM
    implementation(platform(projects.platform.platformBom))
    // Provide common Kotlin dependencies (coroutines, serialization, logging)
    implementation(projects.platform.platformDependencies)

    // === Core Spring Boot Dependencies ===
    // Web starter for REST endpoints
    implementation(libs.spring.boot.starter.web)

    // Validation for request/response validation
    implementation(libs.spring.boot.starter.validation)

    // Actuator for health checks and metrics
    implementation(libs.spring.boot.starter.actuator)

    // === Service Discovery ===
    // Spring Cloud Consul for service registration and discovery
    implementation(libs.spring.cloud.starter.consul.discovery)

    // === Resilience & Fault Tolerance ===
    // Resilience4j Circuit Breaker for fault tolerance
    implementation(libs.resilience4j.spring.boot3)
    implementation(libs.resilience4j.reactor)
    implementation(libs.spring.boot.starter.aop)

    // === Monitoring & Metrics ===
    // Micrometer for metrics collection and Prometheus integration
    implementation(libs.micrometer.prometheus)

    // === Documentation ===
    // OpenAPI 3 documentation generation
    implementation(libs.springdoc.openapi.starter.webmvc.ui)

    // === Testing Dependencies ===
    // Platform testing utilities
    testImplementation(projects.platform.platformTesting)
    // JVM testing bundle (JUnit, AssertJ, Mockk)
    testImplementation(libs.bundles.testing.jvm)
    // Spring Boot testing starter
    testImplementation(libs.spring.boot.starter.test)
}

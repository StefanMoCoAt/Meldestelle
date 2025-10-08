// Optimized Spring Boot ping service for testing microservice architecture
// This service demonstrates circuit breaker patterns, service discovery, and monitoring
plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSpring)
    alias(libs.plugins.kotlinJpa)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependencyManagement)
}

// Configure the main class for the executable JAR
springBoot {
    mainClass.set("at.mocode.ping.service.PingServiceApplicationKt")
}

dependencies {
    // Platform BOM für zentrale Versionsverwaltung
    implementation(platform(projects.platform.platformBom))

    // Platform und Core Dependencies
    implementation(projects.platform.platformDependencies)
    implementation(projects.services.ping.pingApi)
    implementation(projects.infrastructure.monitoring.monitoringClient)

    // Spring Boot Service Complete Bundle
    // Provides: web, validation, actuator, security, oauth2-client, oauth2-resource-server,
    //           data-jpa, data-redis, micrometer-prometheus, tracing, zipkin
    implementation(libs.bundles.spring.boot.service.complete)

    // Jackson Kotlin Support Bundle
    implementation(libs.bundles.jackson.kotlin)

    // Kotlin Reflection (now from version catalog)
    implementation(libs.kotlin.reflect)

    // Service Discovery
    implementation(libs.spring.cloud.starter.consul.discovery)

    // Caching (Caffeine for Spring Cloud LoadBalancer)
    implementation(libs.caffeine)
    implementation(libs.spring.web) // Provides spring-context-support

    // Resilience4j Bundle (Circuit Breaker, Reactor, AOP)
    implementation(libs.bundles.resilience)

    // OpenAPI Documentation
    implementation(libs.springdoc.openapi.starter.webmvc.ui)

    // Test Dependencies
    testImplementation(projects.platform.platformTesting)
    testImplementation(libs.bundles.testing.jvm)
    testImplementation(libs.spring.boot.starter.test)
}

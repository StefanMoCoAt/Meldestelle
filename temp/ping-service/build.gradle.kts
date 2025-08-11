// Simple Spring Boot ping service for testing microservice architecture
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependencyManagement)
}

// Configure the main class for the executable JAR
springBoot {
    mainClass.set("at.mocode.temp.pingservice.PingServiceApplicationKt")
}

dependencies {
    // Ensure all versions come from the central BOM
    implementation(platform(projects.platform.platformBom))
    // Provide common dependencies
    implementation(projects.platform.platformDependencies)

    // Spring Boot Web starter for REST endpoints
    implementation(libs.spring.boot.starter.web)

    // Spring Boot Actuator for health checks
    implementation(libs.spring.boot.starter.actuator)

    // Spring Cloud Consul for service discovery
    implementation(libs.spring.cloud.starter.consul.discovery)

    // Testing dependencies
    testImplementation(projects.platform.platformTesting)
    testImplementation(libs.bundles.testing.jvm)
    testImplementation(libs.spring.boot.starter.test)
}

// Dieses Modul ist das API-Gateway und der einzige öffentliche Einstiegspunkt
// für alle externen Anfragen an das Meldestelle-System.
plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSpring)
    alias(libs.plugins.kotlinJpa)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependencyManagement)
}

// Konfiguriert die Hauptklasse für das ausführbare JAR
springBoot {
    mainClass.set("at.mocode.infrastructure.gateway.GatewayApplicationKt")
}

dependencies {
    // Platform BOM für zentrale Versionsverwaltung
    implementation(platform(projects.platform.platformBom))

    // Core project dependencies
    implementation(projects.core.coreUtils)
    implementation(projects.platform.platformDependencies)

    // Spring Cloud Gateway und Service Discovery (Bundle)
    implementation(libs.bundles.spring.cloud.gateway)

    // Spring Boot Service Complete Bundle (Web, Security, Data, Observability)
    // Provides: spring-boot-starter-web, validation, actuator, security,
    //           oauth2-client, oauth2-resource-server, data-jpa, data-redis,
    //           micrometer-prometheus, tracing-bridge-brave, zipkin-reporter-brave
    implementation(libs.bundles.spring.boot.service.complete)

    // Reactive WebFlux for Gateway
    implementation(libs.spring.boot.starter.webflux)

    // Resilience4j Bundle (Circuit Breaker support)
    implementation(libs.bundles.resilience)

    // Spring Cloud CircuitBreaker for Gateway Filter Integration
    implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j")

    // Jackson Kotlin support
    implementation(libs.bundles.jackson.kotlin)

    // Logging Bundle (kotlin-logging, logback-classic, logback-core, slf4j-api)
    implementation(libs.bundles.logging)

    // Infrastructure dependencies
    implementation(projects.infrastructure.auth.authClient)
    implementation(projects.infrastructure.monitoring.monitoringClient)

    // Test dependencies
    testImplementation(projects.platform.platformTesting)
    testImplementation(libs.bundles.testing.jvm)
    testImplementation(libs.bundles.logging)
}

tasks.test {
    useJUnitPlatform()
}

// Konfiguration für Integration Tests
sourceSets {
    val integrationTest by creating {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

val integrationTestImplementation by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}

tasks.register<Test>("integrationTest") {
    description = "Führt die Integration Tests aus"
    group = "verification"

    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath

    useJUnitPlatform()

    shouldRunAfter("test")

    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = false
        showExceptions = true
        showCauses = true
        showStackTraces = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

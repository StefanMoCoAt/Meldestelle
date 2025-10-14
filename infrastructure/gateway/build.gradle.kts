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
    implementation(platform(projects.platform.platformBom))

    // === Core Dependencies ===
    implementation(projects.core.coreUtils)
    implementation(projects.platform.platformDependencies)
    implementation(projects.infrastructure.monitoring.monitoringClient)

    // === GATEWAY-SPEZIFISCHE ABHÄNGIGKEITEN ===
    implementation(libs.bundles.spring.cloud.gateway)
    implementation(libs.bundles.spring.boot.security)
    implementation(libs.bundles.resilience)
    implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j")
    implementation(libs.spring.boot.starter.actuator) // Wichtig für Health & Metrics
    implementation(libs.bundles.logging)
    implementation(libs.bundles.jackson.kotlin)

    // === Test Dependencies ===
    testImplementation(projects.platform.platformTesting)
    testImplementation(libs.bundles.testing.jvm)
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

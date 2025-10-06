import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Dieses Modul ist das API-Gateway und der einzige öffentliche Einstiegspunkt
// für alle externen Anfragen an das Meldestelle-System.
plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSpring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependencyManagement)
}

// Konfiguriert die Hauptklasse für das ausführbare JAR und Build-Informationen.
springBoot {
    mainClass.set("at.mocode.infrastructure.gateway.GatewayApplicationKt")
    buildInfo()
}

// Optimiert Kotlin-Compiler-Einstellungen für bessere Performance.
tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xjsr305=strict",
            "-opt-in=kotlin.RequiresOptIn"
        )
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

dependencies {
    // Stellt sicher, dass alle Versionen aus der zentralen BOM kommen.
    implementation(platform(projects.platform.platformBom))
    // Stellt Utilities bereit
    implementation(projects.core.coreUtils)
    // Stellt gemeinsame Abhängigkeiten bereit.
    implementation(projects.platform.platformDependencies)
    // Stellt die Spring Cloud Gateway und Consul Discovery Abhängigkeiten bereit
    implementation(libs.bundles.spring.cloud.gateway)
    // Circuit Breaker (Resilience4j) für Gateway Filter - optimiert mit libs reference
    implementation(libs.resilience4j.spring.boot3)
    implementation(libs.resilience4j.reactor)
    implementation(libs.spring.boot.starter.aop) // Benötigt für Resilience4j AOP
    // Spring Cloud CircuitBreaker für Gateway Filter Integration
    implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j")
    // Reaktiver Webserver (Netty) - now properly referenced from libs
    implementation(libs.spring.boot.starter.webflux)
    // Spring Security (WebFlux) – benötigt für SecurityWebFilterChain-Konfiguration
    implementation(libs.spring.boot.starter.security)
    // OAuth2 Resource Server für JWT-Token-Validierung mit Keycloak
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    // Jackson Kotlin Module für JSON-Parsing in KeycloakJwtAuthenticationFilter
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    // Bindet die wiederverwendbare Logik zur JWT-Validierung ein.
    implementation(projects.infrastructure.auth.authClient)
    // Bindet die wiederverwendbare Logik für Metriken und Tracing ein.
    implementation(projects.infrastructure.monitoring.monitoringClient)
    // Explizite Actuator-Abhängigkeit für Health Indicators (benötigt für GatewayHealthIndicator)
    // Obwohl bereits im monitoring-client Bundle, wird durch 'implementation' nicht transitiv verfügbar
    implementation(libs.spring.boot.starter.actuator)
    // Logback-Abhängigkeiten - Versionen werden von Spring Boot BOM verwaltet
    implementation("ch.qos.logback:logback-classic")
    implementation("ch.qos.logback:logback-core")
    implementation("org.slf4j:slf4j-api")
    // Stellt alle Test-Abhängigkeiten gebündelt bereit.
    testImplementation(projects.platform.platformTesting)
    testImplementation(libs.bundles.testing.jvm)
    // Ensure Logback dependencies are available in test classpath
    testImplementation("ch.qos.logback:logback-classic")
    testImplementation("ch.qos.logback:logback-core")
    testImplementation("org.slf4j:slf4j-api")
    // Redundante Security-Abhängigkeit im Testkontext entfernt (bereits durch platform-testing abgedeckt)
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
val compileKotlin: KotlinCompile by tasks
compileKotlin.compilerOptions {
    freeCompilerArgs.set(listOf("-Xannotation-default-target=param-property"))
}

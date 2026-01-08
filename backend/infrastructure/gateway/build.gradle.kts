import org.gradle.api.tasks.testing.logging.TestExceptionFormat

// Dieses Modul ist das API-Gateway und der einzige öffentliche Einstiegspunkt
// für alle externen Anfragen an das Meldestelle-System.
plugins {
  alias(libs.plugins.kotlinJvm)
  alias(libs.plugins.kotlinSpring)
  alias(libs.plugins.kotlinJpa)
  alias(libs.plugins.spring.boot)
}

// Konfiguriert die Hauptklasse für das ausführbare JAR
springBoot {
  mainClass.set("at.mocode.infrastructure.gateway.GatewayApplicationKt")
}

dependencies {
  // Wiederherstellung des Standardzustands: Das Gateway verwendet das reparierte lokale BOM.
  implementation(platform(projects.platform.platformBom))

  // === Core Dependencies ===
  implementation(projects.core.coreUtils)
  implementation(projects.platform.platformDependencies)
  implementation(projects.backend.infrastructure.monitoring.monitoringClient)

  // === GATEWAY-SPEZIFISCHE ABHÄNGIGKEITEN ===
  // Die WebFlux-Abhängigkeit wird jetzt korrekt durch das BOM bereitgestellt.
  implementation(libs.spring.boot.starter.webflux)

  // Kern-Gateway inkl. Security, Actuator, CircuitBreaker, Discovery
  // implementation(libs.bundles.gateway.core)
  implementation(libs.spring.cloud.starter.gateway.server.webflux)
  implementation(libs.spring.cloud.starter.consul.discovery)
  implementation(libs.spring.boot.starter.actuator)
  implementation(libs.spring.boot.starter.security)
  implementation(libs.spring.boot.starter.oauth2.resource.server)
  implementation(libs.spring.security.oauth2.jose)
  implementation(libs.spring.cloud.starter.circuitbreaker.resilience4j)

  // Ergänzende Observability (Logging, Jackson)
  // implementation(libs.bundles.gateway.observability)
  implementation(libs.kotlin.logging.jvm)
  implementation(libs.logback.classic)
  implementation(libs.logback.core)
  implementation(libs.jackson.module.kotlin)
  implementation(libs.jackson.datatype.jsr310)

  // Redis-Unterstützung für verteiltes Rate Limiting (RequestRateLimiter)
  // implementation(libs.bundles.gateway.redis)
  implementation(libs.spring.boot.starter.data.redis)

  // === Tracing Dependencies (Micrometer Tracing) ===
  // Ermöglicht verteiltes Tracing über Thread-Grenzen hinweg (ersetzt manuellen MDC-Filter)
  implementation(libs.micrometer.tracing.bridge.brave)
  // Optional: Zipkin Reporter, falls du Traces an Zipkin senden willst (bereits im monitoringClient enthalten, aber hier explizit schadet nicht)
  // implementation(libs.zipkin.reporter.brave)

  // === Test Dependencies ===
  testImplementation(projects.platform.platformTesting)
  // testImplementation(libs.bundles.testing.jvm)
  testImplementation(libs.junit.jupiter.api)
  testImplementation(libs.junit.jupiter.engine)
  testImplementation(libs.junit.jupiter.params)
  testImplementation(libs.junit.platform.launcher)
  testImplementation(libs.mockk)
  testImplementation(libs.assertj.core)
  testImplementation(libs.kotlinx.coroutines.test)
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

val integrationTestImplementation: Configuration? by configurations.getting {
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
    exceptionFormat = TestExceptionFormat.FULL
  }
}

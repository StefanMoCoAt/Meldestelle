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
  // implementation(libs.spring.boot.starter.webflux)

  // Kern-Gateway inkl. Security, Actuator, CircuitBreaker, Discovery
  implementation(libs.bundles.gateway.core)
  // Ergänzende Observability (Logging, Jackson)
  implementation(libs.bundles.gateway.observability)
  // Redis-Unterstützung für verteiltes Rate Limiting (RequestRateLimiter)
  implementation(libs.bundles.gateway.redis)

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

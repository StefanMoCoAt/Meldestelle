import org.gradle.api.tasks.testing.logging.TestExceptionFormat

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
  implementation(projects.backend.infrastructure.monitoring.monitoringClient)

  // === GATEWAY-SPEZIFISCHE ABHÄNGIGKEITEN ===
  // Kern-Gateway inkl. Security, Actuator, CircuitBreaker, Discovery
  implementation(libs.bundles.gateway.core)
  // Ergänzende Observability (Logging, Jackson)
  implementation(libs.bundles.gateway.observability)
  // Redis-Unterstützung für verteiltes Rate Limiting (RequestRateLimiter)
  // Umgestellt auf das spezifische Gateway-Redis-Bundle (einfach, leicht zu konfigurieren)
  implementation(libs.bundles.gateway.redis)

  // Hinweis: Der Gateway benötigt keinen Datenbanktreiber → entfernt

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

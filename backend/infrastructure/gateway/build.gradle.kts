import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
  alias(libs.plugins.kotlinJvm)
  alias(libs.plugins.kotlinSpring)
  alias(libs.plugins.spring.boot)
}

springBoot {
  mainClass.set("at.mocode.infrastructure.gateway.GatewayApplicationKt")
}

dependencies {
  implementation(platform(projects.platform.platformBom))

  implementation(projects.core.coreUtils)
  implementation(projects.platform.platformDependencies)
  implementation(projects.backend.infrastructure.monitoring.monitoringClient)

  // Wir nutzen das Security-Modul NICHT direkt, um Servlet-Abhängigkeiten zu vermeiden.
  // Stattdessen definieren wir die benötigten Reactive-Dependencies hier explizit.
  // implementation(projects.backend.infrastructure.security)

  // === GATEWAY-SPEZIFISCHE ABHÄNGIGKEITEN ===
  implementation(libs.spring.boot.starter.webflux)
  implementation(libs.spring.cloud.starter.gateway.server.webflux)
  implementation(libs.spring.cloud.starter.consul.discovery)
  implementation(libs.spring.boot.starter.actuator)

  // Security (Reactive)
  implementation(libs.spring.boot.starter.security)
  implementation(libs.spring.boot.starter.oauth2.resource.server)
  implementation(libs.spring.security.oauth2.jose)

  implementation(libs.spring.cloud.starter.circuitbreaker.resilience4j)

  implementation(libs.kotlin.logging.jvm)
  implementation(libs.logback.classic)
  implementation(libs.logback.core)
  implementation(libs.jackson.module.kotlin)
  implementation(libs.jackson.datatype.jsr310)

  implementation(libs.spring.boot.starter.data.redis)

  implementation(libs.micrometer.tracing.bridge.brave)

  testImplementation(projects.platform.platformTesting)
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

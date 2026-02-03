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

  // === GATEWAY-SPEZIFISCHE ABHÄNGIGKEITEN ===
  implementation(libs.spring.boot.starter.webflux)
  implementation(libs.spring.cloud.starter.gateway.server.webflux)
  implementation(libs.spring.cloud.starter.consul.discovery)
  implementation(libs.spring.boot.starter.actuator)

  // Security (Reactive)
  implementation(libs.spring.boot.starter.security)
  implementation(libs.spring.boot.starter.oauth2.resource.server)
  implementation(libs.spring.security.oauth2.jose)

  // Resilience (Reactive) - WICHTIG: Reactor-Variante für WebFlux!
  implementation(libs.spring.cloud.starter.circuitbreaker.reactor.resilience4j)

  implementation(libs.spring.boot.starter.data.redis)
  implementation(libs.micrometer.tracing.bridge.brave)

  testImplementation(projects.platform.platformTesting)
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

tasks.register("integrationTest", Test::class) {
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

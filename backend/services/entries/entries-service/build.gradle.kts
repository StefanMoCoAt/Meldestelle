plugins {
  alias(libs.plugins.kotlinJvm)
  alias(libs.plugins.kotlinSpring)
  alias(libs.plugins.kotlinJpa)
  alias(libs.plugins.spring.boot)
  alias(libs.plugins.spring.dependencyManagement)
}

springBoot {
  mainClass.set("at.mocode.entries.service.EntriesServiceApplicationKt")
}

dependencies {
  implementation(platform(projects.platform.platformBom))
  implementation(projects.platform.platformDependencies)
  implementation(projects.backend.services.entries.entriesApi)
  implementation(projects.backend.infrastructure.monitoring.monitoringClient)

  // Standard dependencies for a secure microservice (centralized bundle)
  implementation(libs.bundles.spring.boot.secure.service)
  // Common service extras
  implementation(libs.spring.boot.starter.validation)
  implementation(libs.spring.boot.starter.json)
  implementation(libs.postgresql.driver)

  // KORREKTUR: Jackson Bundle aufgelöst, da Accessor fehlschlägt
  implementation(libs.jackson.module.kotlin)
  implementation(libs.jackson.datatype.jsr310)

  implementation(libs.kotlin.reflect)
  implementation(libs.caffeine)
  implementation(libs.spring.web)

  // Resilience Dependencies (manuell aufgelöst)
  implementation(libs.resilience4j.spring.boot3)
  implementation(libs.resilience4j.reactor)
  implementation(libs.spring.boot.starter.aop)

  implementation(libs.springdoc.openapi.starter.webmvc.ui)

  testImplementation(projects.platform.platformTesting)
  testImplementation(libs.bundles.testing.jvm)
  testImplementation(libs.spring.boot.starter.test)
}

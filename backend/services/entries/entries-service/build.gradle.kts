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

  implementation(libs.bundles.spring.boot.service.complete)
  implementation(libs.postgresql.driver)
  implementation(libs.spring.boot.starter.web)
  implementation(libs.bundles.jackson.kotlin)
  implementation(libs.kotlin.reflect)
  implementation(libs.spring.cloud.starter.consul.discovery)
  implementation(libs.caffeine)
  implementation(libs.spring.web)
  implementation(libs.bundles.resilience)
  implementation(libs.springdoc.openapi.starter.webmvc.ui)

  testImplementation(projects.platform.platformTesting)
  testImplementation(libs.bundles.testing.jvm)
  testImplementation(libs.spring.boot.starter.test)
  testImplementation(libs.spring.boot.starter.web)
}

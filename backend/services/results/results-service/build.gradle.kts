plugins {
  alias(libs.plugins.kotlinJvm)
  alias(libs.plugins.kotlinSpring)
  alias(libs.plugins.kotlinJpa)
  alias(libs.plugins.spring.boot)
  alias(libs.plugins.spring.dependencyManagement)
}

springBoot {
  mainClass.set("at.mocode.results.service.ResultsServiceApplicationKt")
}

dependencies {
  implementation(platform(projects.platform.platformBom))
  implementation(projects.platform.platformDependencies)
  implementation(projects.backend.infrastructure.monitoring.monitoringClient)

  implementation(libs.bundles.spring.boot.service.complete)
  implementation(libs.postgresql.driver)
  implementation(libs.spring.boot.starter.web)

  // KORREKTUR: Jackson Bundle aufgelöst
  implementation(libs.jackson.module.kotlin)
  implementation(libs.jackson.datatype.jsr310)

  implementation(libs.kotlin.reflect)
  implementation(libs.spring.cloud.starter.consul.discovery)
  implementation(libs.caffeine)
  implementation(libs.spring.web)

  // KORREKTUR: Resilience Bundle aufgelöst
  implementation(libs.resilience4j.spring.boot3)
  implementation(libs.resilience4j.reactor)
  implementation(libs.spring.boot.starter.aop)

  implementation(libs.springdoc.openapi.starter.webmvc.ui)

  testImplementation(projects.platform.platformTesting)
  testImplementation(libs.bundles.testing.jvm)
  testImplementation(libs.spring.boot.starter.test)
  testImplementation(libs.spring.boot.starter.web)
}

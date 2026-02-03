// Dieses Modul bündelt alle für JVM-Tests notwendigen Abhängigkeiten.
// Jedes Modul, das Tests enthält, sollte dieses Modul mit `testImplementation` einbinden.
plugins {
  alias(libs.plugins.kotlinJvm)
}

dependencies {
  // Importiert die zentrale BOM für konsistente Versionen.
  api(platform(projects.platform.platformBom))

  // Testing Libraries
  api(libs.junit.jupiter.api)
  api(libs.junit.jupiter.engine)
  api(libs.junit.jupiter.params)
  api(libs.junit.platform.launcher)
  api(libs.mockk)
  api(libs.assertj.core)
  api(libs.kotlinx.coroutines.test)

  // Logging für Tests (wichtig, um NoClassDefFoundError bei Logback zu vermeiden)
  api(libs.logback.classic)

  // Testcontainers
  api(libs.testcontainers.core)
  api(libs.testcontainers.junit.jupiter)
  api(libs.testcontainers.postgresql)
  api(libs.testcontainers.keycloak)
  api(libs.testcontainers.kafka)

  // Reactor & Spring Testing
  api(libs.reactor.test)
  api(libs.spring.boot.starter.test)
  api(libs.h2.driver)
}

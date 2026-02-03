// Dieses Modul ist ein eigenständiger Spring Boot Service, der den
// Zipkin-Server mit seiner UI hostet, um Tracing-Daten zu visualisieren.
plugins {
  alias(libs.plugins.kotlinJvm)
  alias(libs.plugins.kotlinSpring)
  alias(libs.plugins.spring.boot)
  alias(libs.plugins.spring.dependencyManagement)
}

// Konfiguriert die Hauptklasse für das ausführbare JAR.
springBoot {
  mainClass.set("at.mocode.infrastructure.monitoring.MonitoringServerApplicationKt")
}

dependencies {
  // Stellt sicher, dass alle Versionen aus der zentralen BOM kommen.
  implementation(platform(projects.platform.platformBom))
  // Stellt gemeinsame Abhängigkeiten bereit.
  implementation(projects.platform.platformDependencies)
  // Spring Boot Starter für einen einfachen Web-Service.
  implementation(libs.spring.boot.starter.web)
  implementation(libs.spring.boot.starter.actuator)
  // Abhängigkeiten für den Zipkin-Server (UI ist via zipkin-lens bereits enthalten).
  implementation(libs.zipkin.server)
  // Prometheus client für Zipkin Metriken
  implementation(libs.micrometer.prometheus)
  // Stellt alle Test-Abhängigkeiten gebündelt bereit.
  testImplementation(projects.platform.platformTesting)

  // Logging explizit für Tests erzwingen, um Versionskonflikte zu vermeiden
  testImplementation(libs.logback.classic)
  testImplementation(libs.logback.core)
}

tasks.test {
  useJUnitPlatform()
}

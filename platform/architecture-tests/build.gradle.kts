plugins {
  alias(libs.plugins.kotlinJvm)
}

// This module tests the architecture of the entire project.
// It needs explicit dependencies on all modules that contain code to be checked.
dependencies {
  // ArchUnit
  implementation(libs.archunit.junit5.api)
  runtimeOnly(libs.archunit.junit5.engine)

  // Standard Kotlin Test-Bibliothek
  implementation(libs.kotlin.test)
  implementation(libs.kotlin.test.junit5)

  // --- ADD ALL MODULES WITH CODE TO BE TESTED HERE ---
  // This list must be maintained manually.

  // --- CONTRACTS ---
  implementation(project(":contracts:ping-api"))

  // --- CORE ---
  implementation(project(":core:core-domain"))
  implementation(project(":core:core-utils"))

  // --- BACKEND ---
  implementation(project(":backend:services:ping:ping-service"))

  // --- FRONTEND ---
  implementation(project(":frontend:features:ping-feature"))
  implementation(project(":frontend:core:auth"))
  implementation(project(":frontend:core:domain"))
  implementation(project(":frontend:core:design-system"))
  implementation(project(":frontend:core:navigation"))
  implementation(project(":frontend:core:network"))
  implementation(project(":frontend:core:local-db"))
  implementation(project(":frontend:core:sync"))
  implementation(project(":frontend:shells:meldestelle-portal"))
}

tasks.withType<Test>().configureEach {
  useJUnitPlatform()
}

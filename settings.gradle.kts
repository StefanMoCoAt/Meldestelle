rootProject.name = "Meldestelle"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
  repositories {
    gradlePluginPortal()
    mavenCentral()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://us-central1-maven.pkg.dev/varabyte-repos/public")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://jitpack.io")
  }
}

plugins {
  // Settings plugins cannot easily use version catalogs because the catalog is loaded
  // as part of the settings evaluation. We must hard-code the version here.
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
  repositories {
    gradlePluginPortal()
    mavenCentral()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://us-central1-maven.pkg.dev/varabyte-repos/public")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://jitpack.io")
  }
}

// ==========================================================================
// CONTRACTS
// ==========================================================================
include(":contracts:ping-api")

// ==========================================================================
// Backend
// ==========================================================================

// === BACKEND - INFRASTRUCTURE ===
// --- CACHE ---
include(":backend:infrastructure:cache:cache-api")
include(":backend:infrastructure:cache:redis-cache")

// --- EVENT STORE ---
include(":backend:infrastructure:event-store:event-store-api")
include(":backend:infrastructure:event-store:redis-event-store")

// --- GATEWAY ---
include(":backend:infrastructure:gateway")

// --- MESSAGING ---
include(":backend:infrastructure:messaging:messaging-client")
include(":backend:infrastructure:messaging:messaging-config")

// --- MONITORING ---
include(":backend:infrastructure:monitoring:monitoring-client")
include(":backend:infrastructure:monitoring:monitoring-server")

// --- PERSISTENCE ---
include(":backend:infrastructure:persistence")

// --- SECURITY ---
include(":backend:infrastructure:security")

// === BACKEND - SERVICES ===
// --- ENTRIES (Nennungen) ---
include(":backend:services:entries:entries-api")
include(":backend:services:entries:entries-service")

// --- PING (Ping Service) ---
include(":backend:services:ping:ping-service")

// ==========================================================================
// CORE
// ==========================================================================
include(":core:core-domain")
include(":core:core-utils")

// ==========================================================================
// DOCUMENTATION
// ==========================================================================
include(":docs")

// ==========================================================================
// FRONTEND
// ==========================================================================
// --- CORE ---
// frontend/core/auth
include(":frontend:core:auth")
include(":frontend:core:domain")
include(":frontend:core:design-system")
include(":frontend:core:navigation")
include(":frontend:core:network")
include(":frontend:core:local-db")
include(":frontend:core:sync")

// --- FEATURES ---
// include(":frontend:features:members-feature")
include(":frontend:features:ping-feature")

// --- SHELLS ---
include(":frontend:shells:meldestelle-portal")

// ==========================================================================
// PLATFORM
// ==========================================================================
// --- BOM ---
include(":platform:platform-bom")

// --- DEPENDENCIES ---
include(":platform:platform-dependencies")

// --- TESTING ---
include(":platform:platform-testing")
include(":platform:architecture-tests")

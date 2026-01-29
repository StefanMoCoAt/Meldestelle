rootProject.name = "Meldestelle"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
  repositories {
    gradlePluginPortal()
    mavenCentral()
    google() // Removed content filtering to ensure all artifacts are found
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://us-central1-maven.pkg.dev/varabyte-repos/public")
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") } // Added snapshots for plugins
  }
}

plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
  repositories {
    mavenCentral()
    google()
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
    maven { url = uri("https://us-central1-maven.pkg.dev/varabyte-repos/public") }
    // Add JetBrains Compose repository for RC versions
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
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

// --- EVENTS (Event Management) ---
// include(":backend:services:events:events-api")
// include(":backend:services:events:events-common")
// include(":backend:services:events:events-domain")
// include(":backend:services:events:events-infrastructure")
// include(":backend:services:events:events-service")

// --- HORSES (Horse Management) ---
// include(":backend:services:horses:horses-api")
// include(":backend:services:horses:horses-common")
// include(":backend:services:horses:horses-domain")
// include(":backend:services:horses:horses-infrastructure")
// include(":backend:services:horses:horses-service")

// --- MASTERDATA (The Rulebook) ---
// include(":backend:services:masterdata:masterdata-api")
// include(":backend:services:masterdata:masterdata-common")
// include(":backend:services:masterdata:masterdata-domain")
// include(":backend:services:masterdata:masterdata-infrastructure")
// include(":backend:services:masterdata:masterdata-service")

// --- MEMBERS (Member Management) ---
// include(":backend:services:members:members-api")
// include(":backend:services:members:members-common")
// include(":backend:services:members:members-domain")
// include(":backend:services:members:members-infrastructure")
// include(":backend:services:members:members-service")

// --- PING (Ping Service) ---
include(":backend:services:ping:ping-service")

// --- REGISTRY (Single Source of Truth) ---
// Verwaltet Personen, Pferde & Vereine (ZNS Importe).
// Ersetzt das alte 'members' und 'horses' Modul.
include(":backend:services:registry:oeps-importer") // NEU: Der Gatekeeper für ZNS Daten
include(":backend:services:registry:registry-api")
include(":backend:services:registry:registry-domain")
include(":backend:services:registry:registry-service")

// --- RESULTS (Ergebnisse) ---
include(":backend:services:results:results-service")

// --- SCHEDULING (Zeitplan/Abteilungen) ---
include(":backend:services:scheduling:scheduling-service")

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
include(":frontend:core:auth") // MOVED from features
include(":frontend:core:domain")
include(":frontend:core:design-system")
include(":frontend:core:navigation")
include(":frontend:core:network")
include(":frontend:core:local-db")
include(":frontend:core:sync")

// --- FEATURES ---
// include(":frontend:features:members-feature")
include(":frontend:features:ping-feature")

// --- SHARED
include(":frontend:shared")

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

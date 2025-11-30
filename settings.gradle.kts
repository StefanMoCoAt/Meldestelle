rootProject.name = "Meldestelle"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
  repositories {
    gradlePluginPortal()
    mavenCentral()
    google {
      mavenContent {
        includeGroupAndSubgroups("androidx")
        includeGroupAndSubgroups("com.android")
        includeGroupAndSubgroups("com.google")
      }
    }
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://us-central1-maven.pkg.dev/varabyte-repos/public")
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
  }
}

// ==========================================================================
// CORE & PLATFORM
// ==========================================================================
include(":core:core-domain")
include(":core:core-utils")

include(":platform:platform-bom")
include(":platform:platform-dependencies")
include(":platform:platform-testing")

// ==========================================================================
// INFRASTRUCTURE
// ==========================================================================
include(":backend:gateway")
include(":infrastructure:messaging:messaging-client")
include(":infrastructure:messaging:messaging-config")
include(":infrastructure:cache:cache-api")
include(":infrastructure:cache:redis-cache")
include(":infrastructure:event-store:event-store-api")
include(":infrastructure:event-store:redis-event-store")
include(":infrastructure:monitoring:monitoring-client")
include(":infrastructure:monitoring:monitoring-server")

// ==========================================================================
// BUSINESS DOMAINS (Clean Architecture)
// ==========================================================================

// --- EVENTS (Competition Management) ---
// include(":domains:events:events-api")
// include(":domains:events:events-common")
// include(":domains:events:events-domain")
// include(":domains:events:events-infrastructure")
// include(":domains:events:events-service")

// --- HORSES (Horse Management) ---
// include(":domains:horses:horses-api")
// include(":domains:horses:horses-common")
// include(":domains:horses:horses-domain")
// include(":domains:horses:horses-infrastructure")
// include(":domains:horses:horses-service")

// --- MASTERDATA (The Rulebook) ---
// include(":domains:masterdata:masterdata-api")
// include(":domains:masterdata:masterdata-common")
// include(":domains:masterdata:masterdata-domain")
// include(":domains:masterdata:masterdata-infrastructure")
// include(":domains:masterdata:masterdata-service")

// --- REGISTRY (Single Source of Truth) ---
// Verwaltet Personen, Pferde & Vereine (ZNS Importe).
// Ersetzt das alte 'members' und 'horses' Modul.
include(":domains:registry:oeps-importer") // NEU: Der Gatekeeper für ZNS Daten
include(":domains:registry:registry-api")
include(":domains:registry:registry-domain")
include(":domains:registry:registry-service")

// ==========================================================================
// TECHNICAL SERVICES
// ==========================================================================
include(":backend:services:ping:ping-api")
include(":backend:services:ping:ping-service")

// ==========================================================================
// CLIENTS (Frontend)
// ==========================================================================
include(":frontend:shells:meldestelle-portal")
include(":clients:auth-feature")
include(":clients:ping-feature")
include(":clients:shared")
include(":frontend:core:design-system")
include(":frontend:core:navigation")
include(":frontend:core:network")

// ==========================================================================
// DOCUMENTATION
// ==========================================================================
include(":docs")

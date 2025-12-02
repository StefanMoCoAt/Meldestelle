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
// Backend
// ==========================================================================
include(":backend:infrastructure:cache:cache-api")
include(":backend:infrastructure:cache:redis-cache")

include(":backend:infrastructure:event-store:event-store-api")
include(":backend:infrastructure:event-store:redis-event-store")

<<<<<<< HEAD
include(":backend:infrastructure:gateway")
=======
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
>>>>>>> origin/main

include(":backend:infrastructure:messaging:messaging-client")

include(":backend:infrastructure:messaging:messaging-client")
include(":backend:infrastructure:messaging:messaging-config")

include(":backend:infrastructure:monitoring:monitoring-client")
include(":backend:infrastructure:monitoring:monitoring-server")

// --- EVENTS (Competition Management) ---
<<<<<<< HEAD
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

// --- MEMBERS (The ) ---
// include(":backend:services:members:members-api")
// include(":backend:services:members:members-common")
// include(":backend:services:members:members-domain")
// include(":backend:services:members:members-infrastructure")
// include(":backend:services:members:members-service")

include(":backend:services:ping:ping-api")
include(":backend:services:ping:ping-service")
=======
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
>>>>>>> origin/main

// --- REGISTRY (Single Source of Truth) ---
// Verwaltet Personen, Pferde & Vereine (ZNS Importe).
// Ersetzt das alte 'members' und 'horses' Modul.
include(":backend:services:registry:oeps-importer") // NEU: Der Gatekeeper für ZNS Daten
include(":backend:services:registry:registry-api")
include(":backend:services:registry:registry-domain")
include(":backend:services:registry:registry-service")

// ==========================================================================
// CORE
// ==========================================================================
<<<<<<< HEAD
include(":core:core-domain")
include(":core:core-utils")
=======
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
>>>>>>> origin/main

// ==========================================================================
// DOCUMENTATION
// ==========================================================================
include(":docs")

// ==========================================================================
// FRONTEND
// ==========================================================================
include(":frontend:shared")
include(":frontend:core:design-system")
include(":frontend:core:navigation")
include(":frontend:core:network")

include(":frontend:features:auth-feature")
include(":frontend:features:members-feature")
include(":frontend:features:ping-feature")

include(":frontend:shells:meldestelle-portal")

// ==========================================================================
// PLATFORM
// ==========================================================================
include(":platform:platform-bom")
include(":platform:platform-dependencies")
include(":platform:platform-testing")

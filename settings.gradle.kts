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

// Core modules
include(":core:core-domain")
include(":core:core-utils")

// Platform modules
include(":platform:platform-bom")
include(":platform:platform-dependencies")
include(":platform:platform-testing")

// Infrastructure modules
include(":infrastructure:gateway")
include(":infrastructure:messaging:messaging-client")
include(":infrastructure:messaging:messaging-config")
include(":infrastructure:cache:cache-api")
include(":infrastructure:cache:redis-cache")
include(":infrastructure:event-store:event-store-api")
include(":infrastructure:event-store:redis-event-store")
include(":infrastructure:monitoring:monitoring-client")
include(":infrastructure:monitoring:monitoring-server")

// Temporary modules
include(":services:ping:ping-api")
include(":services:ping:ping-service")

// Client modules
include(":clients:shared")
include(":clients:app")
include(":clients:ping-feature")
include(":clients:auth-feature")
include(":clients:shared:common-ui")
include(":clients:shared:navigation")
include(":clients:members-feature")

// Documentation module
include(":docs")

/*
// Business modules (temporarily disabled - require multiplatform configuration updates)
// Note: We enable only the Members modules needed for API contracts to support the Members client feature.
*/
// Members modules are currently disabled to keep the client build lean.
// We consume the Members REST API from the client without compiling backend modules here.
// include(":services:members:members-domain")
// include(":services:members:members-application")
// include(":services:members:members-api")
// other business modules remain disabled

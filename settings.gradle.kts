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
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        // Add JitPack repository
        maven {
            url = uri("https://jitpack.io")
        }
        // Add Sonatype snapshots repository
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
        }
    }
}

// Platform modules
include(":platform:platform-bom")
include(":platform:platform-dependencies")
include(":platform:platform-testing")

// Core modules
include(":core:core-domain")
include(":core:core-utils")

// Members modules
include(":members:members-domain")
include(":members:members-application")
include(":members:members-infrastructure")
include(":members:members-api")
include(":members:members-service")

// Horses modules
include(":horses:horses-domain")
include(":horses:horses-application")
include(":horses:horses-infrastructure")
include(":horses:horses-api")
include(":horses:horses-service")

// Events modules
include(":events:events-domain")
include(":events:events-application")
include(":events:events-infrastructure")
include(":events:events-api")
include(":events:events-service")

// Masterdata modules
include(":masterdata:masterdata-domain")
include(":masterdata:masterdata-application")
include(":masterdata:masterdata-infrastructure")
include(":masterdata:masterdata-api")
include(":masterdata:masterdata-service")

// Infrastructure modules
include(":infrastructure:gateway")
include(":infrastructure:auth:auth-client")
include(":infrastructure:auth:auth-server")
include(":infrastructure:messaging:messaging-client")
include(":infrastructure:messaging:messaging-config")
include(":infrastructure:cache:cache-api")
include(":infrastructure:cache:redis-cache")
include(":infrastructure:event-store:event-store-api")
include(":infrastructure:event-store:redis-event-store")
include(":infrastructure:monitoring:monitoring-client")
include(":infrastructure:monitoring:monitoring-server")

// Client modules
include(":client:common-ui")
include(":client:web-app")
include(":client:desktop-app")

// Legacy modules have been removed after successful migration

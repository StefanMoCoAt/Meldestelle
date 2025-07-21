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
        // Add JCenter repository (archive)
        maven {
            url = uri("https://jcenter.bintray.com")
        }
        // Add JitPack repository
        maven {
            url = uri("https://jitpack.io")
        }
        // Add Sonatype snapshots repository
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
        }
        // Add Maven repository for Ecwid libraries
        maven {
            url = uri("https://dl.bintray.com/ecwid/maven")
        }
    }
}

// Self-Contained Systems modules
include(":shared-kernel")
include(":master-data")
include(":member-management")
include(":horse-registry")
include(":event-management")
include(":api-gateway")

// Frontend module
include(":composeApp")

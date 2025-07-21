plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvm {
        @OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
        mainRun {
            mainClass.set("at.mocode.gateway.ApplicationKt")
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared-kernel"))
            implementation(project(":master-data"))
            implementation(project(":member-management"))
            implementation(project(":horse-registry"))
            implementation(project(":event-management"))

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.uuid)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }

        jvmMain.dependencies {
            implementation(libs.ktor.server.core)
            implementation(libs.ktor.server.netty)
            implementation(libs.ktor.server.contentNegotiation)
            implementation(libs.ktor.server.cors)
            implementation(libs.ktor.server.auth)
            implementation(libs.ktor.server.authJwt)
            implementation(libs.ktor.server.callLogging)
            implementation(libs.ktor.server.statusPages)
            implementation(libs.ktor.server.serializationKotlinxJson)
            implementation(libs.ktor.server.openapi)
            implementation(libs.ktor.server.swagger)
            implementation(libs.logback)

            // Datenbankabhängigkeiten für Migrationen
            implementation("com.zaxxer:HikariCP:5.0.1")
            implementation(libs.exposed.core)
            implementation(libs.exposed.dao)
            implementation(libs.exposed.jdbc)
            implementation(libs.exposed.kotlinDatetime)
            implementation(libs.postgresql.driver)
        }

        jvmTest.dependencies {
            implementation(libs.ktor.server.tests)
        }
    }
}

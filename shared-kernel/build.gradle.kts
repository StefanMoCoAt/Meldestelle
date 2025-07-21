plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvm()
    js(IR) {
        browser()
        nodejs()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.uuid)
            implementation(libs.bignum)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        jvmMain.dependencies {
            // Datenbankabhängigkeiten
            implementation("com.zaxxer:HikariCP:5.0.1")
            implementation(libs.exposed.core)
            implementation(libs.exposed.dao)
            implementation(libs.exposed.jdbc)
            implementation(libs.exposed.kotlinDatetime)
            implementation(libs.postgresql.driver)

            // Service Discovery dependencies
            implementation("com.orbitz.consul:consul-client:1.5.3")
            implementation("com.ecwid.consul:consul-api:1.4.5") // Downgraded from 2.2.10 to 1.4.5 which is available on Maven Central
            implementation("io.ktor:ktor-client-core:${libs.versions.ktor.get()}")
            implementation("io.ktor:ktor-client-cio:${libs.versions.ktor.get()}")
            implementation("io.ktor:ktor-client-content-negotiation:${libs.versions.ktor.get()}")
            implementation("io.ktor:ktor-serialization-kotlinx-json:${libs.versions.ktor.get()}")
        }

        jvmTest.dependencies {
            // Ktor server dependencies
            implementation(libs.ktor.server.core)
            implementation(libs.ktor.server.netty)
            implementation(libs.ktor.server.tests)

            // H2 database for testing
            implementation(libs.h2.driver)

            // Dependencies on other modules
            implementation(project(":api-gateway"))
            implementation(project(":master-data"))
            implementation(project(":event-management"))

            // Coroutines testing
            implementation(libs.kotlinx.coroutines.test)
        }

        jsMain.dependencies {
            // Kotlin React dependencies with explicit stable versions (for shared components)
            implementation("org.jetbrains.kotlin-wrappers:kotlin-react:18.2.0-pre.467")
            implementation("org.jetbrains.kotlin-wrappers:kotlin-emotion:11.10.5-pre.467")

            // NPM dependencies
            implementation(npm("react", "18.2.0"))
            implementation(npm("react-dom", "18.2.0"))
        }
    }
}

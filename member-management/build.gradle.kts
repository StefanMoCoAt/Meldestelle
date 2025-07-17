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
            implementation(project(":shared-kernel"))
            implementation(project(":master-data"))

            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.uuid)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        jvmMain.dependencies {
            implementation(libs.exposed.core)
            implementation(libs.exposed.dao)
            implementation(libs.exposed.jdbc)
            implementation(libs.exposed.kotlinDatetime)
            implementation(libs.ktor.server.core)
            implementation(libs.ktor.server.contentNegotiation)
            implementation(libs.ktor.server.serializationKotlinxJson)
        }

        jsMain.dependencies {
            // Kotlin React dependencies with explicit versions
            implementation("org.jetbrains.kotlin-wrappers:kotlin-react:${libs.versions.kotlinWrappers.get()}")
            implementation("org.jetbrains.kotlin-wrappers:kotlin-emotion:${libs.versions.kotlinWrappers.get()}")

            // NPM dependencies
            implementation(npm("react", "18.2.0"))
            implementation(npm("react-dom", "18.2.0"))
            implementation(npm("react-to-web-component", "2.0.2"))
        }
    }
}

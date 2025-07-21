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
            implementation(project(":member-management"))

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
            implementation(libs.exposed.core)
            implementation(libs.exposed.dao)
            implementation(libs.exposed.jdbc)
            implementation(libs.exposed.kotlinDatetime)
            implementation(libs.ktor.server.core)
            implementation(libs.ktor.server.contentNegotiation)
            implementation(libs.ktor.server.serializationKotlinxJson)
        }

        jsMain.dependencies {
            // Kotlin React dependencies with explicit stable versions
            implementation("org.jetbrains.kotlin-wrappers:kotlin-react:18.2.0-pre.467")
            implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom:18.2.0-pre.467")
            implementation("org.jetbrains.kotlin-wrappers:kotlin-emotion:11.10.5-pre.467")

            // Ktor client dependencies for API calls
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.js)
            implementation(libs.ktor.client.contentNegotiation)
            implementation(libs.ktor.client.serializationKotlinxJson)

            // Coroutines for async operations
            implementation(libs.kotlinx.coroutines.core)

            // NPM dependencies
            implementation(npm("react", "18.2.0"))
            implementation(npm("react-dom", "18.2.0"))
            implementation(npm("@r2wc/react-to-web-component", "2.0.4"))
        }
    }
}

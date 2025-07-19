plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvm()

    js(IR) {
        browser {
            commonWebpackConfig {
                outputFileName = "event-management.js"
            }
            @OptIn(org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalDistributionDsl::class)
            distribution {
                outputDirectory = layout.buildDirectory.dir("dist")
            }
        }
        binaries.executable()

        // NPM dependencies
        useCommonJs()
        nodejs {
            testTask {
                useMocha {
                    timeout = "10s"
                }
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared-kernel"))

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

            // Ktor client for data loading
            implementation("io.ktor:ktor-client-core:3.1.2")
            implementation("io.ktor:ktor-client-js:3.1.2")
            implementation("io.ktor:ktor-client-content-negotiation:3.1.2")
            implementation("io.ktor:ktor-serialization-kotlinx-json:3.1.2")

            // NPM dependencies
            implementation(npm("react", "18.2.0"))
            implementation(npm("react-dom", "18.2.0"))
            implementation(npm("@r2wc/react-to-web-component", "2.0.4"))
        }
    }
}

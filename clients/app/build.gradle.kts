plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

group = "at.mocode.clients"
version = "1.0.0"

kotlin {
    jvm()
    js {
        browser()
    }

    jvmToolchain(21)

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Feature modules
                implementation(project(":clients:ping-feature"))

                // Shared modules
                implementation(project(":clients:shared:common-ui"))
                implementation(project(":clients:shared:navigation"))

                // Compose dependencies
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)

                // ViewModel lifecycle
                implementation(libs.androidx.lifecycle.viewmodelCompose)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}

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

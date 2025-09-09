plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose)
}

group = "at.mocode.client.kobweb"
version = "1.0-SNAPSHOT"

// Configure Java 17 toolchain (required by modern Compose/Kobweb)
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

kobweb {
    app {
        index {
            description.set("Meldestelle Kobweb Application")
        }
    }
}

kotlin {
    js {
        moduleName = "kobweb-app"
        compilerOptions.target = "es2015"
        browser()
        binaries.executable()
    }

    sourceSets {
        jsMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.kobweb.core)
            implementation(libs.kobweb.silk.core)
            implementation(libs.kobwebx.markdown)
            implementation(libs.compose.html.core)

            // Common UI module via published artifact (decoupled build)
            implementation("at.mocode.client:common-ui:1.0.0-SNAPSHOT")

            // Additional web-specific dependencies
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.ktor.client.js)
        }
    }
}

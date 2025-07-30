plugins {
    // KORREKTUR: Von JVM zu Multiplattform wechseln
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    jvm()
    js(IR) {
        browser()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Diese sind plattformunabhängig und können geteilt werden
                api(projects.platform.platformDependencies)
                api(projects.core.coreDomain)
                api(libs.kotlinx.coroutines.core)
                api(libs.bignum)
            }
        }
        val jvmMain by getting {
            dependencies {
                // DIESE SIND NUR FÜR DIE JVM!
                api(libs.exposed.core)
                api(libs.exposed.dao)
                api(libs.exposed.jdbc)
                api(libs.exposed.kotlin.datetime)
                api(libs.hikari.cp)
                api(libs.flyway.core)
                api(libs.flyway.postgresql)
                api(libs.consul.client)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(projects.platform.platformTesting)
            }
        }
    }
}

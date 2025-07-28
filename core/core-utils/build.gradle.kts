plugins {
    kotlin("jvm")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    api(projects.platform.platformDependencies)

    // UUID handling
    api("com.benasher44:uuid:0.8.2")

    // Serialization
    api("org.jetbrains.kotlinx:kotlinx-serialization-json")
    api("org.jetbrains.kotlinx:kotlinx-datetime")

    // Database
    api("org.jetbrains.exposed:exposed-core")
    api("org.jetbrains.exposed:exposed-dao")
    api("org.jetbrains.exposed:exposed-jdbc")
    api("org.jetbrains.exposed:exposed-kotlin-datetime")
    api("com.zaxxer:HikariCP")
    api("org.flywaydb:flyway-core:9.22.3")

    // BigDecimal
    api("com.ionspin.kotlin:bignum:0.3.8")

    // Service Discovery
    api("com.orbitz.consul:consul-client:1.5.3")

    testImplementation(projects.platform.platformTesting)
}

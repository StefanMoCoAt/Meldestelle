import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    application
}

group = "at.mocode"
version = "1.0.0"

// Enable Gradle caching and parallel execution for better build performance
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21) // Set appropriate JVM target
        freeCompilerArgs.set(listOf("-Xjsr305=strict", "-opt-in=kotlin.RequiresOptIn"))
    }
}

// Configure application
application {
    mainClass.set("at.mocode.server.ApplicationKt")
    applicationDefaultJvmArgs = listOf(
        "-Dio.ktor.development=${extra["io.ktor.development"] ?: "false"}",
//        "-XX:+UseG1GC", // Use G1 Garbage Collector
//        "-XX:MaxGCPauseMillis=100", // Target max GC pause time
//        "-Djava.awt.headless=true" // Headless mode for server
    )
}

// Configure tests
tasks.withType<Test> {
    useJUnitPlatform() // Use JUnit 5 platform
    testLogging {
        events("passed", "skipped", "failed")
    }
    // Parallel test execution if tests are independent
    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1
}

dependencies {
    // Project dependencies
    implementation(projects.shared)
    // Kotlin and related libraries
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)
    implementation(libs.uuid)
    implementation(libs.bignum)

    // Ktor server components
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.config.yaml)
    implementation(libs.ktor.server.html.builder)

    // Ktor server plugins
    implementation("io.ktor:ktor-server-content-negotiation:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-serialization-kotlinx-json:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-server-cors:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-server-call-logging:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-server-default-headers:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-server-status-pages:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-server-auth:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-server-auth-jwt:${libs.versions.ktor.get()}")

    // Database - Exposed ORM
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.kotlin.datetime)

    // Connection pooling
    implementation(libs.hikari.cp)

    // Logging
    implementation(libs.logback)

    // Database drivers
    runtimeOnly(libs.postgresql.driver) // Production
    runtimeOnly(libs.h2.driver) // Development and testing

    // Testing
    testImplementation(libs.ktor.server.tests)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.junit.jupiter)
}

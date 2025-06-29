import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    application
}

group = "at.mocode"
version = "1.0.0"

// Enable Gradle caching and parallel execution for better build performance
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        freeCompilerArgs = listOf(
            "-Xjsr305=strict",
            "-opt-in=kotlin.RequiresOptIn"
        )
    }
}

// Configure application
application {
    mainClass.set("at.mocode.ApplicationKt")
    applicationDefaultJvmArgs = listOf(
        "-Dio.ktor.development=${extra["io.ktor.development"] ?: "false"}",
        "-XX:+UseG1GC", // Use G1 Garbage Collector
        "-XX:MaxGCPauseMillis=100", // Target max GC pause time
        "-Djava.awt.headless=true" // Headless mode for server
    )
}

dependencies {
    // Projekt-Abhängigkeiten
    implementation(projects.shared)

    // Kotlin und verwandte Bibliotheken
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)
    implementation(libs.uuid)
    implementation(libs.bignum)

    // Ktor Server-Komponenten
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.config.yaml)
    implementation(libs.ktor.server.html.builder)

    // Ktor Server-Plugins
    implementation(libs.ktor.server.contentNegotiation)
    implementation(libs.ktor.server.serializationKotlinxJson)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.callLogging)
    implementation(libs.ktor.server.defaultHeaders)
    implementation(libs.ktor.server.statusPages)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.authJwt)

    // Datenbank - Exposed ORM
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.kotlinDatetime)

    // Connection Pooling
    implementation(libs.hikari.cp)

    // Logging
    implementation(libs.logback)

    // Datenbanktreiber
    runtimeOnly(libs.postgresql.driver)
    runtimeOnly(libs.h2.driver)

    // Testing
    testImplementation(libs.ktor.server.tests)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.junitJupiter)

}

// Configure tests
tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1
}

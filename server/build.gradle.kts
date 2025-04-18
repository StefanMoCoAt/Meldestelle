plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    application
}

group = "at.mocode"
version = "1.0.0"
application {
    mainClass.set("at.mocode.ApplicationKt")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=${extra["io.ktor.development"] ?: "false"}")
}

dependencies {
    implementation(projects.shared)
    implementation(libs.logback)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    testImplementation(libs.ktor.server.tests)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.jupiter.junit.jupiter)
    implementation(libs.ktor.server.config.yaml)
    testImplementation(libs.junit.junit.jupiter)

    // Exposed für Datenbankzugriff (Core, DAO-Pattern, JDBC-Implementierung)
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)

    // JDBC Treiber für PostgreSQL (nur zur Laufzeit benötigt)
    runtimeOnly(libs.postgresql.driver)

    // HikariCP für Connection Pooling
    implementation(libs.hikari.cp)

}
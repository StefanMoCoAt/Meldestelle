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
    implementation(project(":shared"))
    implementation(libs.logback)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.config.yaml)
    implementation(libs.ktor.server.html.builder)

    testImplementation(libs.ktor.server.tests)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.jupiter.junit.jupiter)

    testImplementation(libs.junit.junit.jupiter)

    // Exposed für Datenbankzugriff (Core, DAO-Pattern, JDBC-Implementierung)
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)

    // JDBC Treiber für PostgreSQL (nur zur Laufzeit benötigt)
    runtimeOnly(libs.postgresql.driver)

    // H2 Datenbank für Tests und lokale Entwicklung (legacy)
    runtimeOnly(libs.h2.driver)

    // SQLite Datenbank für Tests und lokale Entwicklung
    runtimeOnly("org.xerial:sqlite-jdbc:3.43.0.0")

    // HikariCP für Connection Pooling
    implementation(libs.hikari.cp)

    // Jakarta Mail für E-Mail-Funktionalität
    implementation("com.sun.mail:jakarta.mail:2.0.1")
    implementation("jakarta.activation:jakarta.activation-api:2.1.2")
    implementation("org.eclipse.angus:angus-activation:2.0.1")
}

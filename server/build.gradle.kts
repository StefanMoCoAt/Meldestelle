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

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)
    implementation(libs.uuid)
    implementation(libs.bignum)

    testImplementation(libs.ktor.server.tests)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.junit.jupiter)

    // Exposed für Datenbankzugriff (Core, DAO-Pattern, JDBC-Implementierung)
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.kotlin.datetime)

    // JDBC Treiber für PostgreSQL (nur zur Laufzeit benötigt)
    runtimeOnly(libs.postgresql.driver)

    // H2 Datenbank für Tests und lokale Entwicklung
    runtimeOnly(libs.h2.driver)

    // HikariCP für Connection Pooling
    implementation(libs.hikari.cp)

}

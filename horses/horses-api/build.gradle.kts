plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktor)
    application
}

application {
    mainClass.set("at.mocode.horses.api.ApplicationKt")
}

dependencies {
    implementation(projects.platform.platformDependencies)

    implementation(projects.horses.horsesDomain)
    implementation(projects.horses.horsesApplication)
    implementation(projects.core.coreDomain)
    implementation(projects.core.coreUtils)

    // Spring dependencies
    implementation("org.springframework:spring-web")
    implementation("org.springdoc:springdoc-openapi-starter-common")

    // Ktor Server
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.contentNegotiation)
    implementation(libs.ktor.server.serializationKotlinxJson)
    implementation(libs.ktor.server.statusPages)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.authJwt)

    testImplementation(projects.platform.platformTesting)
    testImplementation(libs.ktor.server.tests)
}

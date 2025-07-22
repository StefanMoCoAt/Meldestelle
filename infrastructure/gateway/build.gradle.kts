plugins {
    kotlin("jvm")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktor)
}

application {
    mainClass.set("at.mocode.infrastructure.gateway.ApplicationKt")
}

// Configure tests to use JUnit Platform and exclude ApiIntegrationTest
tasks.withType<Test> {
    useJUnitPlatform()
    filter {
        // Exclude ApiIntegrationTest from test execution (but not from compilation)
        excludeTestsMatching("at.mocode.infrastructure.gateway.ApiIntegrationTest")
    }
}

dependencies {
    implementation(projects.platform.platformDependencies)
    implementation(projects.core.coreDomain)
    implementation(projects.core.coreUtils)
    implementation(projects.infrastructure.auth.authClient)
    implementation(projects.infrastructure.monitoring.monitoringClient)

    // Domain modules
    implementation(projects.masterdata.masterdataDomain)
    implementation(projects.members.membersDomain)
    implementation(projects.horses.horsesDomain)
    implementation(projects.events.eventsDomain)

    // Infrastructure modules
    implementation(projects.masterdata.masterdataInfrastructure)
    implementation(projects.members.membersInfrastructure)
    implementation(projects.horses.horsesInfrastructure)
    implementation(projects.events.eventsInfrastructure)

    // Ktor Server
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.contentNegotiation)
    implementation(libs.ktor.server.serializationKotlinxJson)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.callLogging)
    implementation(libs.ktor.server.defaultHeaders)
    implementation(libs.ktor.server.statusPages)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.authJwt)
    implementation(libs.ktor.server.openapi)
    implementation(libs.ktor.server.swagger)
    implementation(libs.ktor.server.rateLimit)
    implementation(libs.ktor.server.metrics.micrometer)

    // Monitoring and metrics
    implementation(libs.micrometer.prometheus)

    // Rate limiting
    implementation("io.github.resilience4j:resilience4j-ratelimiter:2.2.0")

    // Documentation
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.3.0")

    // Ktor Client
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.contentNegotiation)
    implementation(libs.ktor.client.serializationKotlinxJson)

    testImplementation(projects.platform.platformTesting)
    testImplementation(libs.ktor.server.tests)
}

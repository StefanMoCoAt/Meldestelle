plugins {
//    kotlin("jvm")
//    kotlin("plugin.spring")
//    id("org.springframework.boot")

    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)

    // KORREKTUR: Dieses Plugin ist entscheidend. Es schaltet den `springBoot`-Block
    // und alle Spring-Boot-spezifischen Gradle-Tasks frei.
    alias(libs.plugins.spring.boot)

    // Dependency Management für konsistente Spring-Versionen
    alias(libs.plugins.spring.dependencyManagement)
}

// Configure main class for bootJar task
springBoot {
    mainClass.set("at.mocode.infrastructure.auth.AuthServerApplicationKt")
}

dependencies {
    implementation(projects.platform.platformDependencies)
    implementation(projects.infrastructure.auth.authClient)

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.keycloak:keycloak-admin-client:23.0.0")

    testImplementation(projects.platform.platformTesting)
}

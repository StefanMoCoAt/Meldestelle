plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
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

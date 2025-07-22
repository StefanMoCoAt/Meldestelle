plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
}

dependencies {
    implementation(projects.platform.platformDependencies)
    implementation(projects.core.coreDomain)
    implementation(projects.core.coreUtils)

    // Spring Security
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.security:spring-security-oauth2-jose")

    // JWT
    implementation("com.auth0:java-jwt:4.4.0")

    testImplementation(projects.platform.platformTesting)
}

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSpring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependencyManagement)
}

// Library module: do not create an executable Spring Boot jar here.
tasks.bootJar {
    enabled = false
}

tasks.jar {
    enabled = true
}

dependencies {
    implementation(platform(projects.platform.platformBom))
    implementation(projects.platform.platformDependencies)

    // Spring Security & OAuth2
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.oauth2.resource.server)
    implementation(libs.spring.security.oauth2.jose)

    // Web (for CORS config)
    implementation(libs.spring.web)

    // Utils
    implementation(libs.slf4j.api)
    implementation(libs.jackson.module.kotlin)

    // Testing
    testImplementation(projects.platform.platformTesting)
    testImplementation(libs.spring.security.test)
}

tasks.test {
    useJUnitPlatform()
}

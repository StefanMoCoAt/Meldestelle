plugins {
    `java-library`
    kotlin("jvm")
}

dependencies {
    api(platform(projects.platform.platformBom))

    // Kotlin Test
    api("org.jetbrains.kotlin:kotlin-test")
    api("org.jetbrains.kotlin:kotlin-test-junit")

    // JUnit
    api("org.junit.jupiter:junit-jupiter-api")
    api("org.junit.jupiter:junit-jupiter-engine")
    api("org.junit.jupiter:junit-jupiter-params")
    api("org.junit.platform:junit-platform-launcher")

    // Mocking and Assertions
    api("io.mockk:mockk:1.13.8")
    api("org.assertj:assertj-core:3.24.2")

    // Coroutines Testing
    api("org.jetbrains.kotlinx:kotlinx-coroutines-test")

    // Spring Boot Testing
    api("org.springframework.boot:spring-boot-starter-test")

    // Database Testing
    api("com.h2database:h2")

    // Test Containers
    api("org.testcontainers:testcontainers:1.19.5")
    api("org.testcontainers:junit-jupiter:1.19.5")
    api("org.testcontainers:postgresql:1.19.5")
}

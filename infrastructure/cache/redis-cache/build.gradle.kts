plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
}

dependencies {
    api(platform(projects.platform.platformBom))
    implementation(projects.infrastructure.cache.cacheApi)

    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("io.lettuce:lettuce-core")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    testImplementation(projects.platform.platformTesting)
    testImplementation("org.testcontainers:testcontainers")
}

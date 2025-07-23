plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
}

dependencies {
    // Apply platform BOM for version management
    implementation(platform(projects.platform.platformBom))

    implementation(projects.infrastructure.eventStore.eventStoreApi)
    implementation(projects.core.coreDomain)
    implementation(projects.core.coreUtils)

    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("io.lettuce:lettuce-core")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("javax.annotation:javax.annotation-api:1.3.2")

    testImplementation(projects.platform.platformTesting)
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
}

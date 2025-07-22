plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
}

dependencies {
    implementation(projects.platform.platformDependencies)

    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.springframework.boot:spring-boot-starter-json")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    testImplementation(projects.platform.platformTesting)
}

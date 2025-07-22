plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
}

// Configure main class for bootJar task
springBoot {
    mainClass.set("at.mocode.infrastructure.monitoring.MonitoringServerApplicationKt")
}

dependencies {
    implementation(projects.platform.platformDependencies)

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("io.zipkin.java:zipkin-server:2.12.9")
    implementation("io.zipkin.java:zipkin-autoconfigure-ui:2.12.9")

    testImplementation(projects.platform.platformTesting)
}

plugins {
    `java-platform`
    `maven-publish`
}

javaPlatform {
    allowDependencies()
}

dependencies {
    api(platform("org.springframework.boot:spring-boot-dependencies:3.2.0"))
    api(platform("org.jetbrains.kotlin:kotlin-bom:2.1.20"))
    api(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.10.1"))

    constraints {
        api("com.github.ben-manes.caffeine:caffeine:3.1.8")
        api("io.projectreactor.kafka:reactor-kafka:1.3.22")
        api("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
        api("org.springdoc:springdoc-openapi-starter-webflux-ui:2.3.0")
        api("org.springdoc:springdoc-openapi-starter-common:2.3.0")
        api("org.redisson:redisson:3.27.1")
        api("io.lettuce:lettuce-core:6.3.1.RELEASE")
        api("io.github.microutils:kotlin-logging-jvm:3.0.5")
        api("org.jetbrains.exposed:exposed-core:0.52.0")
        api("org.jetbrains.exposed:exposed-dao:0.52.0")
        api("org.jetbrains.exposed:exposed-jdbc:0.52.0")
        api("org.jetbrains.exposed:exposed-kotlin-datetime:0.52.0")
        api("org.postgresql:postgresql:42.7.3")
        api("com.zaxxer:HikariCP:5.1.0")
        api("com.h2database:h2:2.2.224")
        api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
        api("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
        api("com.benasher44:uuid:0.8.2")
        api("com.ionspin.kotlin:bignum:0.3.8")
        api("com.orbitz.consul:consul-client:1.5.3")
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["javaPlatform"])
        }
    }
}

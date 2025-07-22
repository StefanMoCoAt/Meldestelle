plugins {
    kotlin("jvm") version "2.1.20" apply false
    kotlin("plugin.spring") version "2.1.20" apply false
    id("org.springframework.boot") version "3.2.0" apply false
    id("io.spring.dependency-management") version "1.1.4" apply false
    base
}

allprojects {
    group = "at.mocode.meldestelle"
    version = "0.1.0-SNAPSHOT"
}

subprojects {
    repositories {
        mavenCentral()
    }

    // Enable dependency locking for all configurations
    dependencyLocking {
        lockAllConfigurations()
    }

    // Add task to write lock files
    tasks.register("resolveAndLockAll") {
        doFirst {
            require(gradle.startParameter.isWriteDependencyLocks)
        }
        doLast {
            configurations.filter {
                // Only lock configurations that can be resolved
                it.isCanBeResolved
            }.forEach { it.resolve() }
        }
    }

    // Configure Kotlin compiler options
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = "21"
            freeCompilerArgs = listOf("-Xjsr305=strict")
        }
    }

    // Configure parallel test execution
    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        // Enable parallel test execution
        maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1
        // Optimize JVM args for tests
        jvmArgs = listOf("-Xmx512m", "-XX:+UseG1GC")
    }
}

// Wrapper task configuration for the root project
tasks.wrapper {
    gradleVersion = "8.14"
    distributionType = Wrapper.DistributionType.BIN
}

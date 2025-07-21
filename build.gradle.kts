// root/build.gradle.kts
plugins {
    // Apply base plugin to provide lifecycle tasks like assemble, build, clean
    base
    // Dies ist notwendig, um zu verhindern, dass die Plugins mehrfach geladen werden
    // im Classloader jedes Subprojekts
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.compose.compiler) apply false
}

// Apply dependency locking to all subprojects
subprojects {
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
            // Add any compiler arguments here if needed
            // The -Xbuild-cache-if-possible flag has been removed as it's not supported in Kotlin 2.1.x
        }
    }

    // Configure parallel test execution
    tasks.withType<Test>().configureEach {
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

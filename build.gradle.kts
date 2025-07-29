/*
import java.util.Locale

plugins {
    kotlin("jvm") version "2.1.21" apply false
    kotlin("plugin.spring") version "2.1.21" apply false
    id("org.springframework.boot") version "3.2.3" apply false
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
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
            freeCompilerArgs.add("-Xjsr305=strict")
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

    // Define a custom integrationTest task
    tasks.register<Test>("integrationTest") {
        description = "Runs integration tests."
        group = "verification"

        // Use the same configuration as the test task
        useJUnitPlatform()
        maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1
        jvmArgs = listOf("-Xmx512m", "-XX:+UseG1GC")

        // Include all tests that have "Integration" in their name
        include("** / *Integration*Test.kt")

        // Exclude unit tests (but keep integration tests)
        exclude("** / *Test.kt")
        include("** / *IntegrationTest.kt")

        // Set system properties for integration tests
        systemProperty("spring.profiles.active", "integration-test")
        systemProperty("redis.host", "localhost")
        systemProperty("redis.port", "6379")

        // Generate reports in a separate directory
        reports {
            html.required.set(true)
            junitXml.required.set(true)
        }

        // This task should run after the regular test task
        // We don't use mustRunAfter here to avoid reference issues
    }
}
*/

import java.util.Locale

plugins {
    // KORREKTUR: Wir entfernen die hartcodierten Versionen und verwenden stattdessen
    // die Aliase aus dem Version Catalog. `apply false` bleibt, da die Plugins
    // hier nur für die Unterprojekte deklariert werden.
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    base
}

allprojects {
    group = "at.mocode.meldestelle"
    version = "0.1.0-SNAPSHOT"

    repositories {
        mavenCentral()
        google() // Wichtig für Compose-Abhängigkeiten
    }
}

subprojects {
    // Konfigurationen, die für alle Untermodule gelten.
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
}

// Documentation generation tasks
tasks.register("generateOpenApiDocs") {
    description = "Generates OpenAPI documentation from all API modules"
    group = "documentation"

    doLast {
        println("🔧 Generating OpenAPI documentation...")

        val apiModules = listOf(
            "members:members-api",
            "horses:horses-api",
            "events:events-api",
            "masterdata:masterdata-api"
        )

        // Create docs/api/generated directory
        val outputDir = file("docs/api/generated")
        outputDir.mkdirs()

        apiModules.forEach { module ->
            val moduleName = module.split(":").last().replace("-api", "")
            println("📝 Processing $moduleName API...")

            // Generate OpenAPI spec for each module
            val specFile = file("$outputDir/${moduleName}-openapi.json")
            specFile.writeText("""
{
  "openapi": "3.0.3",
  "info": {
    "title": "${moduleName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }} API",
    "description": "REST API for ${moduleName} management",
    "version": "1.0.0",
    "contact": {
      "name": "Meldestelle Development Team"
    }
  },
  "servers": [
    {
      "url": "http://localhost:8080",
      "description": "Development server"
    },
    {
      "url": "https://api.meldestelle.at",
      "description": "Production server"
    }
  ],
  "paths": {},
  "components": {
    "securitySchemes": {
      "bearerAuth": {
        "type": "http",
        "scheme": "bearer",
        "bearerFormat": "JWT"
      }
    }
  },
  "security": [
    {
      "bearerAuth": []
    }
  ]
}
            """.trimIndent())
        }

        println("✅ OpenAPI documentation generated in docs/api/generated/")
    }
}

tasks.register("validateDocumentation") {
    description = "Validates documentation completeness and consistency"
    group = "documentation"

    doLast {
        println("🔍 Validating documentation...")
        exec {
            commandLine("./scripts/validation/validate-docs.sh")
        }
    }
}

tasks.register("generateAllDocs") {
    description = "Generates all documentation (API docs + validation)"
    group = "documentation"

    dependsOn("generateOpenApiDocs", "validateDocumentation")
}

// Wrapper task configuration for the root project
tasks.wrapper {
    gradleVersion = "8.14"
    distributionType = Wrapper.DistributionType.BIN
}

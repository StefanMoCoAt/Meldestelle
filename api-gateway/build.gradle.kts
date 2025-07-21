plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    id("org.openapi.generator") version "7.3.0" // Updated to latest version
    id("com.github.johnrengelman.shadow") version "8.1.1" // Shadow plugin for creating fat JARs
}

// Get project version for documentation versioning
val projectVersion = project.version.toString()

// Configure OpenAPI Generator
openApiGenerate {
    generatorName.set("html2")
    inputSpec.set("$projectDir/src/jvmMain/resources/openapi/documentation.yaml")
    outputDir.set("$projectDir/build/generated-docs")

    // Configure HTML2 generator options
    configOptions.set(mapOf(
        "infoUrl" to "https://meldestelle.at",
        "infoEmail" to "support@meldestelle.at",
        "title" to "Meldestelle API Documentation v$projectVersion"
    ))

    // Validate OpenAPI specification before generation
    validateSpec.set(true)
}

// Task to validate OpenAPI specification
tasks.register("validateOpenApi") {
    group = "documentation"
    description = "Validates the OpenAPI specification"

    doLast {
        // Use the OpenAPI Generator's validate task
        tasks.named("openApiValidate").get().actions.forEach { action ->
            action.execute(tasks.named("openApiValidate").get())
        }
        println("OpenAPI specification validated successfully")
    }
}

// Task to generate API documentation
tasks.register("generateApiDocs") {
    group = "documentation"
    description = "Generates API documentation from OpenAPI specification"

    doFirst {
        // Validate the OpenAPI specification before generating documentation
        println("Validating OpenAPI specification...")
        tasks.named("validateOpenApi").get().actions.forEach { action ->
            action.execute(tasks.named("validateOpenApi").get())
        }
    }

    doLast {
        try {
            // Ensure the output directory exists
            mkdir("$projectDir/build/docs")

            // Create version directory for documentation versioning
            val docsVersionDir = file("$projectDir/src/jvmMain/resources/static/docs/v$projectVersion")
            mkdir(docsVersionDir)

            // Copy all generated documentation files to the static docs directory
            copy {
                from("$projectDir/build/generated-docs")
                into("$projectDir/src/jvmMain/resources/static/docs")
                include("**/*")
            }

            // Also copy to the versioned directory
            copy {
                from("$projectDir/build/generated-docs")
                into(docsVersionDir)
                include("**/*")
            }

            // Create a version.json file with version information
            val timestamp = System.currentTimeMillis()
            file("$projectDir/src/jvmMain/resources/static/docs/version.json").writeText("""
                {
                    "version": "$projectVersion",
                    "generatedAt": "$timestamp",
                    "latestVersionUrl": "/docs/v$projectVersion"
                }
            """.trimIndent())

            println("API documentation generated successfully at:")
            println("- Latest: $projectDir/src/jvmMain/resources/static/docs/")
            println("- Versioned: $projectDir/src/jvmMain/resources/static/docs/v$projectVersion/")
        } catch (e: Exception) {
            println("Error generating API documentation: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    // This task depends on the openApiGenerate task
    dependsOn("openApiGenerate")
}

kotlin {
    jvm {
        @OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
        mainRun {
            mainClass.set("at.mocode.gateway.ApplicationKt")
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared-kernel"))
            implementation(project(":master-data"))
            implementation(project(":member-management"))
            implementation(project(":horse-registry"))
            implementation(project(":event-management"))

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.uuid)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }

        jvmMain.dependencies {
            implementation(libs.ktor.server.core)
            implementation(libs.ktor.server.netty)
            implementation(libs.ktor.server.contentNegotiation)
            implementation(libs.ktor.server.cors)
            implementation(libs.ktor.server.auth)
            implementation(libs.ktor.server.authJwt)
            implementation(libs.ktor.server.callLogging)
            implementation(libs.ktor.server.statusPages)
            implementation(libs.ktor.server.serializationKotlinxJson)
            implementation(libs.ktor.server.openapi)
            implementation(libs.ktor.server.swagger)
            implementation(libs.ktor.server.rateLimit)
            implementation(libs.logback)

            // Ktor client dependencies for service discovery
            implementation("io.ktor:ktor-client-core:${libs.versions.ktor.get()}")
            implementation("io.ktor:ktor-client-cio:${libs.versions.ktor.get()}")
            implementation("io.ktor:ktor-client-content-negotiation:${libs.versions.ktor.get()}")
            implementation("io.ktor:ktor-serialization-kotlinx-json:${libs.versions.ktor.get()}")

            // Monitoring dependencies
            implementation("io.ktor:ktor-server-metrics-micrometer:${libs.versions.ktor.get()}")
            implementation("io.micrometer:micrometer-registry-prometheus:${libs.versions.micrometer.get()}")

            // Caching dependencies
            implementation("org.redisson:redisson:${libs.versions.redisson.get()}")
            implementation("com.github.ben-manes.caffeine:caffeine:${libs.versions.caffeine.get()}")

            // Database dependencies
            implementation("com.zaxxer:HikariCP:${libs.versions.hikari.get()}")
            implementation(libs.exposed.core)
            implementation(libs.exposed.dao)
            implementation(libs.exposed.jdbc)
            implementation(libs.exposed.kotlinDatetime)
            implementation(libs.postgresql.driver)
        }

        jvmTest.dependencies {
            implementation(libs.ktor.server.tests)
        }
    }
}

/**
 * Configure the shadowJar task to create a fat JAR with all dependencies included.
 * This is required for the Docker build process, which uses this JAR to create the runtime image.
 * The Dockerfile expects this task to be available with the name 'shadowJar'.
 *
 * The Shadow plugin is used to create a single JAR file that includes all dependencies,
 * making it easier to distribute and run the application in a containerized environment.
 */
tasks {
    val shadowJar = register<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
        // Set the main class for the executable JAR
        manifest {
            attributes(mapOf(
                "Main-Class" to "at.mocode.gateway.ApplicationKt"
            ))
        }

        // Configure the JAR base name and classifier
        archiveBaseName.set("api-gateway")
        archiveClassifier.set("")

        // Configure the Shadow plugin
        mergeServiceFiles()
        exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")

        // Set the configurations to be included in the fat JAR
        val jvmMain = kotlin.jvm().compilations.getByName("main")
        from(jvmMain.output)
        configurations = listOf(jvmMain.compileDependencyFiles as Configuration)
    }
}

// Make the build task depend on shadowJar
tasks.named("build") {
    dependsOn("shadowJar")
}

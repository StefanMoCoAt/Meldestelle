plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    id("org.openapi.generator") version "7.3.0" // Updated to latest version
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
            implementation(libs.logback)

            // Datenbankabhängigkeiten für Migrationen
            implementation("com.zaxxer:HikariCP:5.0.1")
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

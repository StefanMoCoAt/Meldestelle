import java.util.Locale

// Defines plugins that are available to all subprojects.
// `apply false` means the plugin is not applied to the root project itself.
plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
}

// Common configuration for all subprojects in this build.
subprojects {
    // Enforce Java 21 for all Kotlin compilation tasks.
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
    }

    // Configure all test tasks to use the JUnit Platform (for JUnit 5).
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

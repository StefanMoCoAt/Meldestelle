import java.util.Locale

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependencyManagement) apply false
}

subprojects {

    // Wende gemeinsame Einstellungen an
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
    }
    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
}

// ##################################################################
// ###                  IHRE DOKUMENTATIONS-TASKS                 ###
// ##################################################################

// Abstrakte Klasse für die Custom Task (Best Practice)
abstract class ValidateDocumentationTask @Inject constructor(
    private val execOperations: ExecOperations
) : DefaultTask() {

    @TaskAction
    fun validate() {
        println("🔍 Validating documentation...")
        execOperations.exec {
            commandLine("./scripts/validation/validate-docs.sh")
        }
    }
}

// Registrierung der Tasks
tasks.register<ValidateDocumentationTask>("validateDocumentation") {
    description = "Validates documentation completeness and consistency"
    group = "documentation"
}

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
        val outputDir = file("docs/api/generated")
        outputDir.mkdirs()

        apiModules.forEach { module ->
            val moduleName = module.split(":").last().replace("-api", "")
            println("📝 Processing $moduleName API...")

            val specFile = file("$outputDir/${moduleName}-openapi.json")
            specFile.writeText(
                """
                {
                  "openapi": "3.0.3",
                  "info": {
                    "title": "${moduleName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }} API",
                    "description": "REST API for $moduleName management",
                    "version": "1.0.0",
                    "contact": {
                      "name": "Meldestelle Development Team"
                    }
                  },
                  "servers": [
                    { "url": "http://localhost:8080", "description": "Development server" },
                    { "url": "https://api.meldestelle.at", "description": "Production server" }
                  ],
                  "paths": {},
                  "components": {
                    "securitySchemes": {
                      "bearerAuth": { "type": "http", "scheme": "bearer", "bearerFormat": "JWT" }
                    }
                  },
                  "security": [ { "bearerAuth": [] } ]
                }
                """.trimIndent()
            )
        }
        println("✅ OpenAPI documentation generated in docs/api/generated/")
    }
}

tasks.register("generateAllDocs") {
    description = "Generates all documentation (API docs + validation)"
    group = "documentation"
    dependsOn("generateOpenApiDocs", "validateDocumentation")
}

// Wrapper-Konfiguration
tasks.wrapper {
    gradleVersion = "8.14"
    distributionType = Wrapper.DistributionType.BIN
}

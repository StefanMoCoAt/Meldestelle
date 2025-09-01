import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.*
import java.util.Locale
import javax.inject.Inject

plugins {
    // no special plugins required for these custom tasks
}

abstract class ValidateDocumentationTask @Inject constructor(
    private val execOperations: ExecOperations
) : DefaultTask() {

    @get:InputFile
    abstract val scriptFile: RegularFileProperty

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val docsSources: ConfigurableFileCollection

    // When true, a non-zero exit from the validation script will fail the task
    @get:Input
    abstract val strict: Property<Boolean>

    @TaskAction
    fun validate() {
        logger.lifecycle("Validating documentation…")
        val result = execOperations.exec {
            workingDir(project.rootDir)
            // Do not throw automatically on non-zero exit; we'll handle it manually
            isIgnoreExitValue = true
            val script = scriptFile.get().asFile
            val os = System.getProperty("os.name").lowercase(Locale.getDefault())
            if (os.contains("win")) {
                commandLine("bash", script.absolutePath)
            } else {
                commandLine(script.absolutePath)
            }
        }
        val code = result.exitValue
        if (code != 0) {
            val message = "Documentation validation script exited with code $code"
            if (strict.getOrElse(false)) {
                throw GradleException(message)
            } else {
                logger.warn("$message — continuing because strict mode is disabled. Use -PdocsValidateStrict=true to enforce failures.")
            }
        }
    }
}

@CacheableTask
abstract class GenerateOpenApiDocsTask : DefaultTask() {
    @get:Input
    abstract val apiModules: ListProperty<String>

    @get:Input
    abstract val apiVersion: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val out = outputDir.get().asFile.apply { mkdirs() }
        apiModules.get().forEach { module ->
            val moduleName = module.substringAfterLast(":").removeSuffix("-api")
            val specFile = out.resolve("${moduleName}-openapi.json")
            specFile.writeText(
                """
                {
                  "openapi": "3.0.3",
                  "info": {
                    "title": "${moduleName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }} API",
                    "description": "REST API für $moduleName Verwaltung",
                    "version": "${apiVersion.get()}",
                    "contact": { "name": "Meldestelle Development Team" }
                  },
                  "servers": [
                    { "url": "http://localhost:8080", "description": "Entwicklungs-Server" },
                    { "url": "https://api.meldestelle.at", "description": "Produktions-Server" }
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
            logger.lifecycle("Generated ${specFile.relativeTo(project.rootDir)}")
        }
    }
}

val generatedDir = layout.projectDirectory.dir("api/generated")

tasks.register<GenerateOpenApiDocsTask>("generateOpenApiDocs") {
    group = "documentation"
    description = "Generates OpenAPI docs for all API modules"
    apiModules.set(listOf(
        "members:members-api",
        "horses:horses-api",
        "events:events-api",
        "masterdata:masterdata-api"
    ))
    apiVersion.set("1.0.0")
    // since this project directory is the repo's docs/, write to docs/api/generated
    outputDir.set(generatedDir)
}

tasks.register<ValidateDocumentationTask>("validateDocumentation") {
    group = "documentation"
    description = "Validates documentation completeness and consistency (use -PdocsValidateStrict=true to fail on issues)"
    // Ensure generated OpenAPI docs are available before validation
    dependsOn(tasks.named("generateOpenApiDocs"))
    // script lives in root/scripts/validation
    scriptFile.set(layout.projectDirectory.file("../scripts/validation/validate-docs.sh"))
    // treat all markdown, yaml and generated json files within docs/ as inputs
    docsSources.from(
        layout.projectDirectory.asFileTree.matching { include("**/*.md") },
        layout.projectDirectory.asFileTree.matching { include("**/*.yml", "**/*.yaml") },
        layout.projectDirectory.dir("api/generated").asFileTree.matching { include("**/*.json") }
    )
    // strict mode from Gradle property (default: false)
    strict.set(
        providers.gradleProperty("docsValidateStrict")
            .map { it.equals("true", ignoreCase = true) }
            .orElse(false)
    )
}

tasks.register("generateAllDocs") {
    group = "documentation"
    description = "Generates all documentation (API + validation)"
    dependsOn("generateOpenApiDocs", "validateDocumentation")
}

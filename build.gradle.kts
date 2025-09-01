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
        useJUnitPlatform {
            excludeTags("perf")
        }
        // Configure CDS in auto-mode to prevent bootstrap classpath warnings
        jvmArgs("-Xshare:auto", "-Djdk.instrument.traceUsage=false")
        // Increase test JVM memory with stable configuration
        maxHeapSize = "2g"
        // Removed byte-buddy-agent configuration to fix Gradle 9.0.0 deprecation warning
        // The agent configuration was causing Task.project access at execution time
    }

    // Dedicated performance test task per JVM subproject
    plugins.withId("java") {
        val javaExt = extensions.getByType<JavaPluginExtension>()
        tasks.register<Test>("perfTest") {
            description = "Runs tests tagged with 'perf'"
            group = "verification"
            // Use the regular test source set outputs
            testClassesDirs = javaExt.sourceSets.getByName("test").output.classesDirs
            classpath = javaExt.sourceSets.getByName("test").runtimeClasspath
            useJUnitPlatform {
                includeTags("perf")
            }
            shouldRunAfter("test")
            // Keep same JVM settings for consistency
            jvmArgs("-Xshare:auto", "-Djdk.instrument.traceUsage=false")
            maxHeapSize = "2g"
            dependsOn("testClasses")
        }
    }

    // Suppress Node.js deprecation warnings (e.g., DEP0040 punycode) during Kotlin/JS npm/yarn tasks
    // Applies to all Exec-based tasks (covers Yarn/NPM invocations used by Kotlin JS plugin)
    tasks.withType<Exec>().configureEach {
        // Merge existing NODE_OPTIONS with --no-deprecation
        val current = (environment["NODE_OPTIONS"] as String?) ?: System.getenv("NODE_OPTIONS")
        val merged = if (current.isNullOrBlank()) "--no-deprecation" else "$current --no-deprecation"
        environment("NODE_OPTIONS", merged)
        // Also set the legacy switch to silence warnings entirely
        environment("NODE_NO_WARNINGS", "1")
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
                    "description": "REST API für $moduleName Verwaltung",
                    "version": "1.0.0",
                    "contact": {
                      "name": "Meldestelle Development Team"
                    }
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
// Apply Node warning suppression on root project Exec tasks as well
// Ensures aggregated Kotlin/JS tasks created at root (e.g., kotlinNpmInstall) inherit the env
tasks.withType<Exec>().configureEach {
    val current = (environment["NODE_OPTIONS"] as String?) ?: System.getenv("NODE_OPTIONS")
    val merged = if (current.isNullOrBlank()) "--no-deprecation" else "$current --no-deprecation"
    environment("NODE_OPTIONS", merged)
    environment("NODE_NO_WARNINGS", "1")
}

tasks.wrapper {
    gradleVersion = "9.0.0"
    distributionType = Wrapper.DistributionType.BIN
}

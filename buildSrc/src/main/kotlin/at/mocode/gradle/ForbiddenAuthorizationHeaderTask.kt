package at.mocode.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class ForbiddenAuthorizationHeaderTask : DefaultTask() {

    @TaskAction
    fun check() {
        val forbiddenPatterns = listOf(
            ".header(\"Authorization\"",
            "setHeader(\"Authorization\"",
            "headers[\"Authorization\"]",
            "headers['Authorization']",
            ".header(HttpHeaders.Authorization",
            "header(HttpHeaders.Authorization",
        )
        // Scope: Frontend-only enforcement. Backend/Test code is excluded.
        val srcDirs = listOf("clients", "frontend")
        val violations = mutableListOf<File>()

        srcDirs.map { project.file(it) }
            .filter { it.exists() }
            .forEach { rootDir ->
                rootDir.walkTopDown()
                    .filter { it.isFile && (it.extension == "kt" || it.extension == "kts") }
                    .forEach { f ->
                        val text = f.readText()
                        // Skip test sources
                        val path = f.invariantSeparatorsPath
                        val isTest = path.contains("/src/commonTest/") ||
                                path.contains("/src/jsTest/") ||
                                path.contains("/src/jvmTest/") ||
                                path.contains("/src/test/")
                        if (!isTest && forbiddenPatterns.any { text.contains(it) }) {
                            violations += f
                        }
                    }
            }

        if (violations.isNotEmpty()) {
            val msg = buildString {
                appendLine("Forbidden manual Authorization header usage found in:")
                violations.take(50).forEach { appendLine(" - ${it.path}") }
                if (violations.size > 50) appendLine(" ... and ${violations.size - 50} more files")
                appendLine()
                appendLine("Policy: Use DI-provided apiClient (Koin named \"apiClient\").")
            }
            throw GradleException(msg)
        }
    }
}

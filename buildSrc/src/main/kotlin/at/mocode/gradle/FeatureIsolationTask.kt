package at.mocode.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.tasks.TaskAction

abstract class FeatureIsolationTask : DefaultTask() {

    @TaskAction
    fun check() {
        val featurePrefix = ":frontend:features:"
        val violations = mutableListOf<String>()

        project.rootProject.subprojects.forEach { p ->
            if (p.path.startsWith(featurePrefix)) {
                // Check all configurations except test-related ones
                p.configurations
                    .matching { cfg ->
                        val n = cfg.name.lowercase()
                        !n.contains("test") && !n.contains("debug") // ignore test/debug configs
                    }
                    .forEach { cfg ->
                        cfg.dependencies.withType(ProjectDependency::class.java).forEach { dep ->
                            // Use reflection to avoid compile-time issues with dependencyProject property
                            val target = try {
                                val method = dep.javaClass.getMethod("getDependencyProject")
                                val proj = method.invoke(dep) as Project
                                proj.path
                            } catch (e: Exception) {
                                ""
                            }
                            if (target.startsWith(featurePrefix) && target != p.path) {
                                violations += "${p.path} -> $target (configuration: ${cfg.name})"
                            }
                        }
                    }
            }
        }

        if (violations.isNotEmpty()) {
            val msg = buildString {
                appendLine("Feature isolation violation(s) detected:")
                violations.forEach { appendLine(" - $it") }
                appendLine()
                appendLine("Policy: frontend features must not depend on other features. Use navigation/shared domain in :frontend:core instead.")
            }
            throw GradleException(msg)
        }
    }
}

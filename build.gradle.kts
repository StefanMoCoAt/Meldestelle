import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType
import java.io.File

plugins {
  // Version management plugin for dependency updates
  id("com.github.ben-manes.versions") version "0.51.0"

  // Custom convention plugins
  id("at.mocode.bundle-budget") apply false // Apply to root, but a task runs on subprojects

  // Kotlin plugins declared here with 'apply false' to centralize version management
  // This prevents "plugin loaded multiple times" errors in Gradle 9.2.1+
  // Subprojects apply these plugins via version catalog: alias(libs.plugins.kotlinJvm)
  alias(libs.plugins.kotlinJvm) apply false
  alias(libs.plugins.kotlinMultiplatform) apply false
  alias(libs.plugins.kotlinSerialization) apply false
  alias(libs.plugins.kotlinSpring) apply false
  alias(libs.plugins.kotlinJpa) apply false
  alias(libs.plugins.composeMultiplatform) apply false
  alias(libs.plugins.composeCompiler) apply false
  alias(libs.plugins.spring.boot) apply false
  alias(libs.plugins.spring.dependencyManagement) apply false

  // Dokka plugin applied at root to create multi-module collector tasks
  alias(libs.plugins.dokka)

  // Static analysis (enabled at root and inherited by subprojects)
  id("io.gitlab.arturbosch.detekt") version "1.23.6"
  id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
}

// ##################################################################
// ###                  ALL-PROJECTS CONFIGURATION                 ###
// ##################################################################

allprojects {
  group = "at.mocode"
  version = "1.0.0-SNAPSHOT"

  // The 'repositories' block was removed from here.
  // Repository configuration is now centralized in 'settings.gradle.kts'
  // as per modern Gradle best practices. This resolves dependency resolution
  // conflicts with platforms and Spring Boot 4+.
}

subprojects {
  // FINALE KORREKTUR: Konsistente JVM-Target-Konfiguration für Java und Kotlin
  // basierend auf der Tech-Stack-Referenz.
  plugins.withType<org.jetbrains.kotlin.gradle.plugin.KotlinBasePluginWrapper> {
    extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension> {
      jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
      }
    }
  }

  tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_25)
      freeCompilerArgs.add("-Xannotation-default-target=param-property")
    }
  }

  tasks.withType<Test>().configureEach {
    useJUnitPlatform {
      excludeTags("perf")
    }
    // Configure CDS in auto-mode to prevent bootstrap classpath warnings
    jvmArgs("-Xshare:auto", "-Djdk.instrument.traceUsage=false")
    // Increase test JVM memory with a stable configuration
    minHeapSize = "512m"
    maxHeapSize = "2g"
    // Parallel test execution for better performance
    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
    // Removed byte-buddy-agent configuration to fix Gradle 9.0.0 deprecation warning
    // The agent configuration was causing Task.project access at execution time
  }

  // Dedicated performance test task per JVM subproject
  plugins.withId("java") {
    val javaExt = extensions.getByType<JavaPluginExtension>()
    // Ensure a full JDK toolchain with compiler is available (Gradle will auto-download if missing)
    javaExt.toolchain.languageVersion.set(JavaLanguageVersion.of(25))

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
      // Keep the same JVM settings for consistency
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
    // Set a Chrome binary path to avoid snap permission issues
    environment("CHROME_BIN", "/usr/bin/google-chrome-stable")
    environment("CHROMIUM_BIN", "/usr/bin/chromium")
    environment("PUPPETEER_EXECUTABLE_PATH", "/usr/bin/chromium")
  }

  // ------------------------------
  // Detekt & Ktlint default setup
  // ------------------------------
  plugins.withId("io.gitlab.arturbosch.detekt") {
    extensions.configure(DetektExtension::class.java) {
      buildUponDefaultConfig = true
      allRules = false
      autoCorrect = false
      config.setFrom(files(rootProject.file("config/detekt/detekt.yml")))
      basePath = rootDir.absolutePath
    }
    tasks.withType<Detekt>().configureEach {
      jvmTarget = "25"
      reports {
        xml.required.set(false)
        txt.required.set(false)
        sarif.required.set(false)
        html.required.set(true)
      }
    }
  }

  plugins.withId("org.jlleitschuh.gradle.ktlint") {
    extensions.configure(KtlintExtension::class.java) {
      android.set(false)
      outputToConsole.set(true)
      ignoreFailures.set(false)
      reporters {
        reporter(ReporterType.CHECKSTYLE)
        reporter(ReporterType.PLAIN)
      }
    }
  }
}

// ==================================================================
// Architecture Guards (lightweight, fast checks)
// ==================================================================

// Fails if any source file contains manual Authorization header setting.
// Policy: Authorization must be injected by the DI-provided HttpClient (apiClient).
tasks.register("archGuardForbiddenAuthorizationHeader") {
  group = "verification"
  description = "Fail build if code sets Authorization header manually."
  doLast {
    val forbiddenPatterns =
      listOf(
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
    srcDirs.map { file(it) }
      .filter { it.exists() }
      .forEach { rootDir ->
        rootDir.walkTopDown()
          .filter { it.isFile && (it.extension == "kt" || it.extension == "kts") }
          .forEach { f ->
            val text = f.readText()
            // Skip test sources
            val path = f.invariantSeparatorsPath
            val isTest =
              path.contains("/src/commonTest/") ||
                path.contains("/src/jsTest/") ||
                path.contains("/src/jvmTest/") ||
                path.contains("/src/test/")
            if (!isTest && forbiddenPatterns.any { text.contains(it) }) {
              violations += f
            }
          }
      }
    if (violations.isNotEmpty()) {
      val msg =
        buildString {
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

// Guard: Frontend Feature Isolation (no feature -> feature project dependencies)
tasks.register("archGuardNoFeatureToFeatureDeps") {
  group = "verification"
  description = "Fail build if a :frontend:features:* module depends on another :frontend:features:* module"
  doLast {
    val featurePrefix = ":frontend:features:"
    val violations = mutableListOf<String>()

    rootProject.subprojects.forEach { p ->
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
              val proj =
                try {
                  dep.javaClass.getMethod("getDependencyProject").invoke(dep) as Project
                } catch (_: Throwable) {
                  null
                }
              val target = proj?.path ?: ""
              if (target.startsWith(featurePrefix) && target != p.path) {
                violations += "${p.path} -> $target (configuration: ${cfg.name})"
              }
            }
          }
      }
    }

    if (violations.isNotEmpty()) {
      val msg =
        buildString {
          appendLine("Feature isolation violation(s) detected:")
          violations.forEach { appendLine(" - $it") }
          appendLine()
          appendLine("Policy: frontend features must not depend on other features. Use navigation/shared domain in :frontend:core instead.")
        }
      throw GradleException(msg)
    }
  }
}

// Aggregate convenience task
tasks.register("archGuards") {
  group = "verification"
  description = "Run all architecture guard checks"
  dependsOn("archGuardForbiddenAuthorizationHeader")
  dependsOn("archGuardNoFeatureToFeatureDeps")
}

// Composite verification task including static analyzers if present
tasks.register("staticAnalysis") {
  group = "verification"
  description = "Run static analysis (detekt, ktlint) and architecture guards"
  // Plugins provide these tasks; only 'depend on' if tasks exist
  dependsOn(
    tasks.matching { it.name == "detekt" },
    tasks.matching { it.name == "ktlintCheck" },
    tasks.named("archGuards"),
  )
}

// ##################################################################
// ###                     DOKKA (Multi-Module)                   ###
// ##################################################################

// Apply Dokka (V2) automatically to Kotlin subprojects
subprojects {
  plugins.withId("org.jetbrains.kotlin.jvm") { apply(plugin = "org.jetbrains.dokka") }
  plugins.withId("org.jetbrains.kotlin.multiplatform") { apply(plugin = "org.jetbrains.dokka") }
}

// Aggregate tasks to build multi-module docs in Markdown (GFM) and HTML
// Unified V2 aggregator: builds docs via `dokkaGenerate` in subprojects and aggregates outputs
val dokkaAll =
  tasks.register("dokkaAll") {
    group = "documentation"
    description = "Builds Dokka (V2) for all modules and aggregates outputs under build/dokka/all"
    // Trigger Dokka generation in all subprojects that have the Dokka plugin
    dependsOn(
      subprojects
        .filter { it.plugins.hasPlugin("org.jetbrains.dokka") }
        .map { "${it.path}:dokkaGenerate" },
    )
    doLast {
      val dest = layout.buildDirectory.dir("dokka/all").get().asFile
      if (dest.exists()) dest.deleteRecursively()
      dest.mkdirs()
      subprojects.filter { it.plugins.hasPlugin("org.jetbrains.dokka") }.forEach { p ->
        // Dokka V2 writes into build/dokka; copy everything to keep format/plugins agnostic
        val out = p.layout.buildDirectory.dir("dokka").get().asFile
        if (out.exists()) {
          out.copyRecursively(File(dest, p.path.trimStart(':').replace(':', '/')), overwrite = true)
        }
      }
      println("[DOKKA] Aggregated Dokka V2 outputs into ${dest.absolutePath}")
    }
  }

// ##################################################################
// ###                     DOKU-AGGREGATOR                        ###
// ##################################################################

// Leichter Aggregator im Root-Projekt, ruft die eigentlichen Tasks im :docs Subprojekt auf
tasks.register("docs") {
  description = "Aggregates documentation tasks from :docs"
  group = "documentation"
  dependsOn(":docs:generateAllDocs")
}

// Wrapper-Konfiguration
// Apply Node warning suppression on root project Exec tasks as well
// Ensures aggregated Kotlin/JS tasks created at root (e.g., kotlinNpmInstall) inherit the env
tasks.withType<Exec>().configureEach {
  val current = (environment["NODE_OPTIONS"] as String?) ?: System.getenv("NODE_OPTIONS")
  val merged = if (current.isNullOrBlank()) "--no-deprecation" else "$current --no-deprecation"
  environment("NODE_OPTIONS", merged)
  environment("NODE_NO_WARNINGS", "1")
  // Set a Chrome binary path to avoid snap permission issues
  environment("CHROME_BIN", "/usr/bin/google-chrome-stable")
  environment("CHROMIUM_BIN", "/usr/bin/chromium")
  environment("PUPPETEER_EXECUTABLE_PATH", "/usr/bin/chromium")
}

tasks.wrapper {
  gradleVersion = "9.2.1"
  distributionType = Wrapper.DistributionType.BIN
}

import groovy.json.JsonSlurper
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

plugins {
  // Version management plugin for dependency updates
  alias(libs.plugins.benManesVersions)

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
  alias(libs.plugins.detekt)
  alias(libs.plugins.ktlint)
}

// Workaround for Gradle 9 / KMP Race Condition:
// Wir erzwingen die Initialisierung des NodeJsRootPlugins im Root-Projekt
apply<NodeJsRootPlugin>()

// ##################################################################
// ###                  ALLPROJECTS CONFIGURATION                 ###
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

  // ---------------------------------------------------------------------------
  // Frontend/JS build noise reduction
  // ---------------------------------------------------------------------------
  // (B) Avoid noisy "will be copied ... overwriting" logs for Kotlin/JS *CompileSync tasks.
  // The Kotlin JS plugin wires multiple resource sourcesets into the same destination.
  // We keep the first occurrence and exclude duplicates.
  tasks.withType<Copy>().configureEach {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
  }
  tasks.withType<Sync>().configureEach {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
  }

  // (A) Source map configuration is handled via `gradle.properties` (global Kotlin/JS settings)
  // to avoid compiler-flag incompatibilities across toolchains.

  // (B) JS test executable compilation/sync is currently very noisy (duplicate resource copying from jsMain + jsTest).
  // We disable JS/WASM JS test executables in CI/build to keep output warning-free.
  tasks.matching {
    val n = it.name
    n.contains("jsTest", ignoreCase = true) ||
      n.contains("compileTestDevelopmentExecutableKotlinJs") ||
      n.contains("compileTestDevelopmentExecutableKotlinWasmJs") ||
      n.contains("TestDevelopmentExecutableCompileSync")
  }.configureEach {
    enabled = false
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

// ------------------------------------------------------------------
// Bundle Size Budgets for Frontend Shells (Kotlin/JS)
// ------------------------------------------------------------------
// ✅ FIX: Klasse auf Top-Level verschieben
data class Budget(val rawBytes: Long, val gzipBytes: Long)

tasks.register("checkBundleBudget") {
  group = "verification"
  description = "Checks JS bundle sizes of frontend shells against configured budgets"
  doLast {
    val budgetsFile = file("config/bundles/budgets.json")
    if (!budgetsFile.exists()) {
      throw GradleException("Budgets file not found: ${budgetsFile.path}")
    }

    // Load budgets JSON as simple Map<String, Budget>
    val text = budgetsFile.readText()

    @Suppress("UNCHECKED_CAST")
    val parsed =
      JsonSlurper().parseText(text) as Map<String, Map<String, Any?>>
    val budgets =
      parsed.mapValues { (_, v) ->
        val raw = (v["rawBytes"] as Number).toLong()
        val gz = (v["gzipBytes"] as Number).toLong()
        Budget(raw, gz)
      }

    fun gzipSize(bytes: ByteArray): Long {
      val baos = ByteArrayOutputStream()
      GZIPOutputStream(baos).use { it.write(bytes) }
      return baos.toByteArray().size.toLong()
    }

    val errors = mutableListOf<String>()
    val report = StringBuilder()
    report.appendLine("Bundle Budget Report (per shell)")

    // Consider modules under :frontend:shells: as shells
    val shellPrefix = ":frontend:shells:"
    val shells = rootProject.subprojects.filter { it.path.startsWith(shellPrefix) }
    if (shells.isEmpty()) {
      report.appendLine("No frontend shells found under $shellPrefix")
    }

    shells.forEach { shell ->
      val key = shell.path.trimStart(':').replace(':', '/') // or use a colon form for budgets keys below
      val colonKey = shell.path.trimStart(':').replace('/', ':').trim() // ensure ":a:b:c"
      // Budgets are keyed by a Gradle path with colons but without leading colon in config for readability
      val budgetKeyCandidates =
        listOf(
          // e.g., frontend:shells:meldestelle-portal
          shell.path.removePrefix(":"),
          colonKey.removePrefix(":"),
          shell.name,
        )

      val budgetEntry = budgetKeyCandidates.firstNotNullOfOrNull { budgets[it] }
      if (budgetEntry == null) {
        report.appendLine("- ${shell.path}: No budget configured (skipping)")
        return@forEach
      }

      // Locate distributions directory
      val distDir = shell.layout.buildDirectory.dir("distributions").get().asFile
      if (!distDir.exists()) {
        report.appendLine("- ${shell.path}: distributions dir not found (expected build/distributions) – did you build the JS bundle?")
        return@forEach
      }

      // Collect JS files under distributions (avoid .map and .txt)
      val jsFiles =
        distDir.walkTopDown().filter { it.isFile && it.extension == "js" && !it.name.endsWith(".map") }.toList()
      if (jsFiles.isEmpty()) {
        report.appendLine("- ${shell.path}: no JS artifacts found in ${distDir.path}")
        return@forEach
      }

      var totalRaw = 0L
      var totalGzip = 0L
      val topFiles = mutableListOf<Pair<String, Long>>()
      jsFiles.forEach { f ->
        val bytes = f.readBytes()
        val raw = bytes.size.toLong()
        val gz = gzipSize(bytes)
        totalRaw += raw
        totalGzip += gz
        topFiles += f.name to raw
      }
      val top = topFiles.sortedByDescending { it.second }.take(5)

      report.appendLine("- ${shell.path}:")
      report.appendLine("    raw:  $totalRaw bytes (budget ${budgetEntry.rawBytes})")
      report.appendLine("    gzip: $totalGzip bytes (budget ${budgetEntry.gzipBytes})")
      report.appendLine("    top files:")
      top.forEach { (n, s) -> report.appendLine("      - $n: $s bytes") }

      if (totalRaw > budgetEntry.rawBytes || totalGzip > budgetEntry.gzipBytes) {
        errors += "${shell.path}: raw=$totalRaw/${budgetEntry.rawBytes}, gzip=$totalGzip/${budgetEntry.gzipBytes}"
      }
    }

    val outDir = layout.buildDirectory.dir("reports/bundles").get().asFile
    outDir.mkdirs()
    file(outDir.resolve("bundle-budgets.txt")).writeText(report.toString())

    if (errors.isNotEmpty()) {
      val msg =
        buildString {
          appendLine("Bundle budget violations:")
          errors.forEach { appendLine(" - $it") }
          appendLine()
          appendLine("See report: ${outDir.resolve("bundle-budgets.txt").path}")
        }
      throw GradleException(msg)
    } else {
      println(report.toString())
      println("Bundle budgets OK. Report saved to ${outDir.resolve("bundle-budgets.txt").path}")
    }
  }
}

// Composite verification task including static analyzers if present
tasks.register("staticAnalysis") {
  group = "verification"
  description = "Run static analysis (detekt, ktlint) and architecture guards"
  // Plugins provide these tasks; only 'depend on' if tasks exist
  dependsOn(
    tasks.matching { it.name == "detekt" },
    tasks.matching { it.name == "ktlintCheck" },
    // ARCHITECTURE-TESTS: Replaced old archGuards with the new test module
    project(":platform:architecture-tests").tasks.named("test"),
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

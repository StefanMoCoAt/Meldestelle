plugins {
  // Version management plugin for dependency updates
  id("com.github.ben-manes.versions") version "0.51.0"

  // Kotlin plugins declared here with 'apply false' to centralize version management
  // This prevents "plugin loaded multiple times" errors in Gradle 9.1.0+
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
// ###                  ALLPROJECTS CONFIGURATION                 ###
// ##################################################################

allprojects {
  group = "at.mocode"
  version = "1.0.0-SNAPSHOT"

  // Apply common repository configuration
  repositories {
    mavenCentral()
    google()
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
    maven { url = uri("https://us-central1-maven.pkg.dev/varabyte-repos/public") }
  }
}

subprojects {
  // Note: Kotlin compiler configuration is handled by individual modules
  // a Root project doesn't apply Kotlin plugins, so we can't configure KotlinCompile tasks here

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

  // Erzwinge eine stabile Version von kotlinx-serialization-json für alle Konfigurationen,
  // um Auflösungsfehler (z.B. 1.10.2, nicht verfügbar auf Maven Central) zu vermeiden
  configurations.configureEach {
    resolutionStrategy {
      force("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    }
  }

  // Dedicated performance test task per JVM subproject
  plugins.withId("java") {
    val javaExt = extensions.getByType<JavaPluginExtension>()
    // Ensure a full JDK toolchain with compiler is available (Gradle will auto-download if missing)
    javaExt.toolchain.languageVersion.set(JavaLanguageVersion.of(21))

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
    // Set Chrome binary path to avoid snap permission issues
    environment("CHROME_BIN", "/usr/bin/google-chrome-stable")
    environment("CHROMIUM_BIN", "/usr/bin/chromium")
    environment("PUPPETEER_EXECUTABLE_PATH", "/usr/bin/chromium")
  }

  tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
      freeCompilerArgs.add("-Xannotation-default-target=param-property")
    }
  }

  // ------------------------------
  // Detekt & Ktlint default setup
  // ------------------------------
  plugins.withId("io.gitlab.arturbosch.detekt") {
    extensions.configure(io.gitlab.arturbosch.detekt.extensions.DetektExtension::class.java) {
      buildUponDefaultConfig = true
      allRules = false
      autoCorrect = false
      config.setFrom(files(rootProject.file("config/detekt/detekt.yml")))
      basePath = rootDir.absolutePath
    }
    tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
      jvmTarget = "21"
      reports {
        xml.required.set(false)
        txt.required.set(false)
        sarif.required.set(false)
        html.required.set(true)
      }
    }
  }

  plugins.withId("org.jlleitschuh.gradle.ktlint") {
    extensions.configure(org.jlleitschuh.gradle.ktlint.KtlintExtension::class.java) {
      android.set(false)
      outputToConsole.set(true)
      ignoreFailures.set(false)
      reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
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
        "headers[\'Authorization\']",
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
            cfg.dependencies.withType(org.gradle.api.artifacts.ProjectDependency::class.java).forEach { dep ->
              // Use reflection to avoid compile-time issues with dependencyProject property
              val proj =
                try {
                  dep.javaClass.getMethod("getDependencyProject").invoke(dep) as org.gradle.api.Project
                } catch (e: Throwable) {
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
      groovy.json.JsonSlurper().parseText(text) as Map<String, Map<String, Any?>>
    val budgets =
      parsed.mapValues { (_, v) ->
        val raw = (v["rawBytes"] as Number).toLong()
        val gz = (v["gzipBytes"] as Number).toLong()
        Budget(raw, gz)
      }

    fun gzipSize(bytes: ByteArray): Long {
      val baos = java.io.ByteArrayOutputStream()
      java.util.zip.GZIPOutputStream(baos).use { it.write(bytes) }
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
      val key = shell.path.trimStart(':').replace(':', '/') // or use colon form for budgets keys below
      val colonKey = shell.path.trimStart(':').replace('/', ':').trim() // ensure ":a:b:c"
      // Budgets are keyed by a Gradle path with colons but without leading colon in config for readability
      val budgetKeyCandidates =
        listOf(
          // e.g., frontend:shells:meldestelle-portal
          shell.path.removePrefix(":"),
          colonKey.removePrefix(":"),
          shell.name,
        )

      val budgetEntry = budgetKeyCandidates.asSequence().mapNotNull { budgets[it] }.firstOrNull()
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
      val jsFiles = distDir.walkTopDown().filter { it.isFile && it.extension == "js" && !it.name.endsWith(".map") }.toList()
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
  // These tasks are provided by plugins; only depend if tasks exist
  dependsOn(
    tasks.matching { it.name == "detekt" },
    tasks.matching { it.name == "ktlintCheck" },
    tasks.named("archGuards"),
  )
}

// ##################################################################
// ###                     DOKKA (Multi-Module)                   ###
// ##################################################################

// Apply Dokka automatically to Kotlin subprojects to enable per-module docs
subprojects {
  plugins.withId("org.jetbrains.kotlin.jvm") {
    apply(plugin = "org.jetbrains.dokka")
  }
  plugins.withId("org.jetbrains.kotlin.multiplatform") {
    apply(plugin = "org.jetbrains.dokka")
  }

  // Minimal sourceLink configuration when running in GitHub Actions
  tasks.withType(org.jetbrains.dokka.gradle.DokkaTask::class.java).configureEach {
    dokkaSourceSets.configureEach {
      val repo = System.getenv("GITHUB_REPOSITORY")
      if (!repo.isNullOrBlank()) {
        sourceLink {
          localDirectory.set(project.file("src"))
          remoteUrl.set(
            java.net.URI.create(
              "https://github.com/$repo/blob/main/" + project.path.trimStart(':').replace(':', '/') + "/src",
            ).toURL(),
          )
        }
      }
      // Keep module names short and stable
      moduleName.set(project.path.trimStart(':'))
    }
  }
}

// Aggregate tasks to build multi-module docs in Markdown (GFM) and HTML
val dokkaGfmAll =
  tasks.register("dokkaGfmAll") {
    group = "documentation"
    description = "Builds Dokka GFM for all modules and aggregates outputs under build/dokka/gfm"
    // Depend on all dokkaGfm tasks that exist in subprojects
    dependsOn(
      subprojects
        .filter { it.plugins.hasPlugin("org.jetbrains.dokka") }
        .map { "${it.path}:dokkaGfm" },
    )
    doLast {
      val dest = layout.buildDirectory.dir("dokka/gfm").get().asFile
      if (dest.exists()) dest.deleteRecursively()
      dest.mkdirs()
      subprojects.filter { it.plugins.hasPlugin("org.jetbrains.dokka") }.forEach { p ->
        val out = p.layout.buildDirectory.dir("dokka/gfm").get().asFile
        if (out.exists()) {
          out.copyRecursively(File(dest, p.path.trimStart(':').replace(':', '/')), overwrite = true)
        }
      }
      println("[DOKKA] Aggregated GFM into ${dest.absolutePath}")
    }
  }

val dokkaHtmlAll =
  tasks.register("dokkaHtmlAll") {
    group = "documentation"
    description = "Builds Dokka HTML for all modules and aggregates outputs under build/dokka/html"
    dependsOn(
      subprojects
        .filter { it.plugins.hasPlugin("org.jetbrains.dokka") }
        .map { "${it.path}:dokkaHtml" },
    )
    doLast {
      val dest = layout.buildDirectory.dir("dokka/html").get().asFile
      if (dest.exists()) dest.deleteRecursively()
      dest.mkdirs()
      subprojects.filter { it.plugins.hasPlugin("org.jetbrains.dokka") }.forEach { p ->
        val out = p.layout.buildDirectory.dir("dokka/html").get().asFile
        if (out.exists()) {
          out.copyRecursively(File(dest, p.path.trimStart(':').replace(':', '/')), overwrite = true)
        }
      }
      println("[DOKKA] Aggregated HTML into ${dest.absolutePath}")
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
  // Set Chrome binary path to avoid snap permission issues
  environment("CHROME_BIN", "/usr/bin/google-chrome-stable")
  environment("CHROMIUM_BIN", "/usr/bin/chromium")
  environment("PUPPETEER_EXECUTABLE_PATH", "/usr/bin/chromium")
}

tasks.wrapper {
  gradleVersion = "9.1.0"
  distributionType = Wrapper.DistributionType.BIN
}

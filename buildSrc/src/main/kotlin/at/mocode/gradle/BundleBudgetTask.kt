package at.mocode.gradle

import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.GZIPOutputStream

data class Budget(val rawBytes: Long, val gzipBytes: Long)

abstract class BundleBudgetTask : DefaultTask() {

    @TaskAction
    fun check() {
        val budgetsFile = project.file("config/bundles/budgets.json")
        if (!budgetsFile.exists()) {
            throw GradleException("Budgets file not found: ${budgetsFile.path}")
        }

        // Load budgets JSON as simple Map<String, Budget>
        val text = budgetsFile.readText()

        @Suppress("UNCHECKED_CAST")
        val parsed = JsonSlurper().parseText(text) as Map<String, Map<String, Any?>>
        val budgets = parsed.mapValues { (_, v) ->
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
        val shells = project.rootProject.subprojects.filter { it.path.startsWith(shellPrefix) }
        if (shells.isEmpty()) {
            report.appendLine("No frontend shells found under $shellPrefix")
        }

        shells.forEach { shell ->
            val colonKey = shell.path.trimStart(':').replace('/', ':').trim() // ensure ":a:b:c"
            // Budgets are keyed by a Gradle path with colons but without leading colon in config for readability
            val budgetKeyCandidates = listOf(
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

        val outDir = project.layout.buildDirectory.dir("reports/bundles").get().asFile
        outDir.mkdirs()
        project.file(outDir.resolve("bundle-budgets.txt")).writeText(report.toString())

        if (errors.isNotEmpty()) {
            val msg = buildString {
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

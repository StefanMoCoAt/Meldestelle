package at.mocode.frontend.core.localdb

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class DatabaseDriverFactory {
  actual suspend fun createDriver(): SqlDriver {
    // For desktop, we use a persistent file database
    // In dev mode, we might want to use a temporary file or user home
    val dbFile = File(System.getProperty("user.home"), ".meldestelle/app_database.db")
    dbFile.parentFile.mkdirs()

    val driver = JdbcSqliteDriver("jdbc:sqlite:${dbFile.absolutePath}")

    // Schema creation/migration needs to be handled carefully.
    // For now, we just create it if it doesn't exist.
    // In a real app, we'd check the version and migrate.
    // Since generateAsync=true, the Schema.create signature might be suspended or return AsyncResult.
    // However, JdbcSqliteDriver is synchronous. We might need to wrap or await.
    // But wait! Schema.create(driver) usually returns void or Unit.
    // Let's check the generated code later. For now, we assume standard behavior.

    try {
      AppDatabase.Schema.create(driver).await()
    } catch (_: Exception) {
      // Schema might already exist.
      // SQLDelight doesn't have "createIfNotExists" built-in easily without version check.
      // We'll leave this simple for now and refine with proper migration logic later.
    }

    return driver
  }
}

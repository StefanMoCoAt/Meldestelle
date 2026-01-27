package at.mocode.frontend.core.localdb

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.worker.WebWorkerDriver
import org.w3c.dom.Worker

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class DatabaseDriverFactory {
  actual suspend fun createDriver(): SqlDriver {
    // Wir nutzen eine Helper-Funktion, um den Worker zu erstellen.
    val worker = createWorker()
    val driver = WebWorkerDriver(worker)

    try {
      val version = getVersion(driver)
      val schemaVersion = AppDatabase.Schema.version

      console.log("Database version check: Current=$version, Schema=$schemaVersion")

      if (version == 0L) {
        console.log("Creating Database Schema...")
        try {
          AppDatabase.Schema.create(driver).await()
          setVersion(driver, schemaVersion)
          console.log("Database Schema created and version set to $schemaVersion")
        } catch (e: Throwable) {
          // If tables already exist but version was 0 (e.g. previous broken run), we might get here.
          val msg = e.message ?: ""
          if (msg.contains("already exists", ignoreCase = true)) {
            console.warn("Tables already exist but version was 0. Assuming DB is initialized. Setting version to $schemaVersion.")
            setVersion(driver, schemaVersion)
          } else {
            throw e
          }
        }
      } else if (version < schemaVersion) {
        console.log("Migrating Database Schema from $version to $schemaVersion...")
        AppDatabase.Schema.migrate(driver, version, schemaVersion).await()
        setVersion(driver, schemaVersion)
        console.log("Database Schema migrated")
      } else {
        console.log("Database Schema is up to date.")
      }
    } catch (e: Throwable) {
      console.error("Error initializing database schema:", e)
      throw e
    }

    return driver
  }

  private suspend fun getVersion(driver: SqlDriver): Long {
    // Workaround for QueryResult issues:
    // We capture the cursor in a local variable and return the Boolean result from next().
    // Then we read from the captured cursor.

    var cursorRef: SqlCursor? = null

    // executeQuery returns QueryResult<Boolean> because mapper returns QueryResult<Boolean>
    val hasNext = driver.executeQuery<Boolean>(
      identifier = null,
      sql = "PRAGMA user_version;",
      mapper = { cursor ->
        cursorRef = cursor
        cursor.next()
      },
      parameters = 0
    ).await()

    return if (hasNext) {
      cursorRef?.getLong(0) ?: 0L
    } else {
      0L
    }
  }

  private suspend fun setVersion(driver: SqlDriver, version: Long) {
    driver.execute(null, "PRAGMA user_version = $version;", 0).await()
  }
}

// Helper function to create the worker
private fun createWorker(): Worker {
  // Try the relative path again, as an absolute path might fail depending on base href
  return js("new Worker('sqlite.worker.js')")
}

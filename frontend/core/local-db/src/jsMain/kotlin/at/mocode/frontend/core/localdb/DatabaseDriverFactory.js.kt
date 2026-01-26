package at.mocode.frontend.core.localdb

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.worker.WebWorkerDriver
import org.w3c.dom.Worker

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class DatabaseDriverFactory {
  actual suspend fun createDriver(): SqlDriver {
    // Wir nutzen eine Helper-Funktion, um den Worker zu erstellen.
    val worker = createWorker()
    val driver = WebWorkerDriver(worker)

    // Initialize schema asynchronously
    AppDatabase.Schema.create(driver).await()

    return driver
  }
}

// Helper function to create the worker
private fun createWorker(): Worker {
  // Try the relative path again, as an absolute path might fail depending on base href
  return js("new Worker('sqlite.worker.js')")
}

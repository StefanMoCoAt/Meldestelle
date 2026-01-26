package at.mocode.frontend.core.localdb

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.worker.WebWorkerDriver
import org.w3c.dom.Worker

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class DatabaseDriverFactory {
  actual suspend fun createDriver(): SqlDriver {
    // Wir nutzen eine Helper-Funktion, um den Worker zu erstellen.
    // Dies ermöglicht uns, 'new URL(..., import.meta.url)' in JS zu verwenden,
    // was Webpack dazu bringt, den Pfad korrekt aufzulösen.
    val worker = createWorker()
    val driver = WebWorkerDriver(worker)

    // Initialize schema asynchronously
    AppDatabase.Schema.create(driver).await()

    return driver
  }
}

// Helper function to create the worker using proper URL resolution
private fun createWorker(): Worker {
  return js(
    """
        new Worker(new URL('sqlite.worker.js', import.meta.url), { type: 'module' })
    """
  )
}

package at.mocode.frontend.core.localdb

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.worker.WebWorkerDriver
import org.w3c.dom.Worker

actual class DatabaseDriverFactory {
    actual suspend fun createDriver(): SqlDriver {
        // Load the worker script.
        // We use a simple string path instead of `new URL(..., import.meta.url)` to prevent Webpack
        // from trying to resolve/bundle this file at build time.
        // The file 'sqlite.worker.js' is copied to the root of the distribution by the Gradle build script.
        val worker = Worker("sqlite.worker.js")

        val driver = WebWorkerDriver(worker)

        // Initialize schema asynchronously
        AppDatabase.Schema.create(driver).await()

        return driver
    }
}

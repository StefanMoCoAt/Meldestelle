package at.mocode.frontend.core.localdb

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.worker.WebWorkerDriver
import org.w3c.dom.Worker

actual class DatabaseDriverFactory {
    actual suspend fun createDriver(): SqlDriver {
        // Load the worker script. This assumes the worker is bundled correctly by Webpack.
        // We use a custom worker entry point to support OPFS if needed (as per report).
        // For now, we point to a resource we will create.
        val worker = Worker(
            js("""new URL("sqlite.worker.js", import.meta.url)""")
        )
        val driver = WebWorkerDriver(worker)

        // Initialize schema asynchronously
        AppDatabase.Schema.create(driver).await()

        return driver
    }
}

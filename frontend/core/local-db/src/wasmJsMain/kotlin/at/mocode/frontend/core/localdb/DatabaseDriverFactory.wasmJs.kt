package at.mocode.frontend.core.localdb

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.worker.WebWorkerDriver
import org.w3c.dom.Worker

actual class DatabaseDriverFactory {
    actual suspend fun createDriver(): SqlDriver {
        // Same as JS, we use a Web Worker for Wasm to support OPFS
        val worker = Worker(
            js("""new URL("sqlite.worker.js", import.meta.url)""")
        )
        val driver = WebWorkerDriver(worker)

        AppDatabase.Schema.create(driver).await()

        return driver
    }
}

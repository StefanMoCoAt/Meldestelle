package at.mocode.frontend.core.localdb

/*
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.worker.WebWorkerDriver
import org.w3c.dom.Worker

actual class DatabaseDriverFactory {
    actual suspend fun createDriver(): SqlDriver {
        // In Kotlin/Wasm, we cannot use the js() function inside a function body like in Kotlin/JS.
        // We need to use a helper function or a different approach.
        // However, for WebWorkerDriver, we need a Worker instance.

        // Workaround for Wasm: Use a helper function to create the Worker
        val worker = createWorker()
        val driver = WebWorkerDriver(worker)

        AppDatabase.Schema.create(driver).await()

        return driver
    }
}

// Helper function to create a Worker in Wasm
// Note: Kotlin/Wasm JS interop is stricter.
// We must return a type that Wasm understands as an external JS reference.
// 'Worker' from org.w3c.dom is correct, but we need to ensure the stdlib is available.
private fun createWorker(): Worker = js("new Worker(new URL('sqlite.worker.js', import.meta.url))")
*/

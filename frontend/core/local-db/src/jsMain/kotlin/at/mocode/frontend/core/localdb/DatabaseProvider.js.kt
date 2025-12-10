@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package at.mocode.frontend.core.localdb

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.worker.WebWorkerDriver
import org.w3c.dom.Worker

actual class DatabaseDriverFactory {
  actual suspend fun createDriver(): SqlDriver {
    val worker = js(
      "new Worker(new URL('@cashapp/sqldelight-sqljs-worker/sqljs.worker.js', import.meta.url))"
    ) as Worker
    val driver = WebWorkerDriver(worker)
    // Create schema asynchronously
    MeldestelleDb.Schema.create(driver).await()
    return driver
  }
}

actual class DatabaseProvider {
  actual suspend fun createDatabase(): MeldestelleDb {
    val driver = DatabaseDriverFactory().createDriver()
    return MeldestelleDb(driver)
  }
}

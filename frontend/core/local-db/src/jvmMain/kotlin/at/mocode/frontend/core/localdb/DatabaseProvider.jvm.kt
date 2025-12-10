@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package at.mocode.frontend.core.localdb

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver

actual class DatabaseDriverFactory {
  actual suspend fun createDriver(): SqlDriver {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    // Create schema on first run (in-memory is always new)
    MeldestelleDb.Schema.create(driver)
    return driver
  }
}

actual class DatabaseProvider {
  actual suspend fun createDatabase(): MeldestelleDb {
    val driver = DatabaseDriverFactory().createDriver()
    return MeldestelleDb(driver)
  }
}

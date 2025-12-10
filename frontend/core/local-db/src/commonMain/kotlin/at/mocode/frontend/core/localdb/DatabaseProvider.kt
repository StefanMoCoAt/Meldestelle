@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package at.mocode.frontend.core.localdb

import app.cash.sqldelight.db.SqlDriver
import org.koin.dsl.module

// Generated database class name from SQLDelight configuration
expect class DatabaseDriverFactory() {
  suspend fun createDriver(): SqlDriver
}

// Convenience to create the typed database from a driver
expect class DatabaseProvider() {
  suspend fun createDatabase(): MeldestelleDb
}

// Koin module that exposes the database as a singleton
val localDbModule = module {
  single { DatabaseDriverFactory() }
  // Provide only the suspend-capable provider; consumers create the DB in a coroutine
  single { DatabaseProvider() }
}

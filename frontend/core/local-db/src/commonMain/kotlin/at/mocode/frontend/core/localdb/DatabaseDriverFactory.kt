package at.mocode.frontend.core.localdb

import app.cash.sqldelight.db.SqlDriver

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class DatabaseDriverFactory {
    suspend fun createDriver(): SqlDriver
}

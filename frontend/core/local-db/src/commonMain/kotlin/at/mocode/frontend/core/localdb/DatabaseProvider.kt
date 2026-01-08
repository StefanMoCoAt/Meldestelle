@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package at.mocode.frontend.core.localdb

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.dsl.module

// Abstract Room Database class definition
// @Database(entities = [MyEntity::class], version = 1) // Entities need to be defined
abstract class MeldestelleDb : RoomDatabase() {
    // abstract fun myDao(): MyDao
}

// Factory to create the database builder platform-specifically
expect class DatabaseBuilderFactory() {
    fun create(): RoomDatabase.Builder<MeldestelleDb>
}

class DatabaseProvider(private val factory: DatabaseBuilderFactory) {
    fun createDatabase(): MeldestelleDb {
        return factory.create()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }
}

val localDbModule = module {
    single { DatabaseBuilderFactory() }
    single { DatabaseProvider(get()).createDatabase() }
}

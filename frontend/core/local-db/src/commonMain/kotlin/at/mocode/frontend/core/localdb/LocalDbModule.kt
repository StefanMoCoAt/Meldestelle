package at.mocode.frontend.core.localdb

import org.koin.dsl.module

/**
 * Thin wrapper around SQLDelight `AppDatabase` creation.
 *
 * The platform-specific part is the `DatabaseDriverFactory` (expect/actual),
 * which provides the appropriate SQLDelight driver (JVM sqlite driver, JS WebWorkerDriver, ...).
 */
class DatabaseProvider(
  private val driverFactory: DatabaseDriverFactory
) {
  suspend fun createDatabase(): AppDatabase {
    val driver = driverFactory.createDriver()
    return AppDatabase(driver)
  }
}

/**
 * Koin module to provide the SQLDelight database for all frontend targets.
 */
val localDbModule = module {
  single<DatabaseDriverFactory> { DatabaseDriverFactory() }
  single<DatabaseProvider> { DatabaseProvider(get()) }
}

package at.mocode.infrastructure.gateway.config

import at.mocode.infrastructure.gateway.migrations.*
import at.mocode.core.utils.database.DatabaseMigrator

/**
 * Konfiguriert und führt alle Datenbankmigrationen aus.
 */
object MigrationSetup {
    /**
     * Registriert alle Migrationen und führt sie aus.
     */
    fun runMigrations() {
        // Migrationen registrieren
        DatabaseMigrator.registerAll(
            // Master Data Migrationen
            MasterDataTablesCreation(),

            // Member Management Migrationen
            MemberManagementTablesCreation(),

            // Horse Registry Migrationen
            HorseRegistryTablesCreation(),

            // Event Management Migrationen
            EventManagementTablesCreation()
        )

        // Migrationen ausführen
        DatabaseMigrator.migrate()
    }
}

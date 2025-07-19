package at.mocode.gateway.config

import at.mocode.gateway.migrations.*
import at.mocode.shared.database.DatabaseMigrator

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

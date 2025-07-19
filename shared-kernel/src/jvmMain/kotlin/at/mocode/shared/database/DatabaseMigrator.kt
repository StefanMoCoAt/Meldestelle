package at.mocode.shared.database

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

/**
 * Führt Datenbankmigrationen durch.
 * Diese Klasse verwaltet und führt alle notwendigen Datenbankmigrationen aus.
 */
object DatabaseMigrator {
    private val migrations = mutableListOf<Migration>()
    private val executedMigrations = mutableSetOf<String>()

    /**
     * Registriert eine Migration.
     * @param migration Die zu registrierende Migration
     */
    fun register(migration: Migration) {
        migrations.add(migration)
    }

    /**
     * Registriert mehrere Migrationen auf einmal.
     * @param migrations Die zu registrierenden Migrationen
     */
    fun registerAll(vararg migrations: Migration) {
        this.migrations.addAll(migrations)
    }

    /**
     * Führt alle registrierten Migrationen aus, die noch nicht ausgeführt wurden.
     */
    fun migrate() {
        // Erstelle die Migrationstabelle, wenn sie nicht existiert
        transaction {
            SchemaUtils.create(MigrationTable)

            // Lade bereits ausgeführte Migrationen
            MigrationTable.selectAll().forEach {
                executedMigrations.add(it[MigrationTable.id])
            }

            // Sortiere Migrationen nach Version
            val sortedMigrations = migrations.sortedBy { it.version }

            // Führe noch nicht ausgeführte Migrationen aus
            for (migration in sortedMigrations) {
                if (!executedMigrations.contains(migration.id)) {
                    println("Ausführen der Migration: ${migration.id}")
                    try {
                        migration.up()

                        // Markiere Migration als ausgeführt
                        MigrationTable.insert {
                            it[id] = migration.id
                            it[version] = migration.version
                            it[description] = migration.description
                        }

                        commit()
                        println("Migration erfolgreich: ${migration.id}")
                    } catch (e: Exception) {
                        rollback()
                        println("Migration fehlgeschlagen: ${migration.id} - ${e.message}")
                        throw e
                    }
                }
            }
        }
    }
}

/**
 * Tabelle zur Verfolgung ausgeführter Migrationen.
 */
object MigrationTable : Table("_migrations") {
    val id = varchar("id", 100)
    val version = long("version")
    val description = varchar("description", 255)
    val executedAt = timestamp("executed_at").defaultExpression(CurrentTimestamp)

    override val primaryKey = PrimaryKey(id)
}

/**
 * Basisklasse für Datenbankmigrationen.
 */
abstract class Migration(val version: Long, val description: String) {
    /**
     * Eindeutige ID der Migration, bestehend aus Version und Beschreibung.
     */
    val id: String = "V${version}_${description.replace("\\s+".toRegex(), "_")}"

    /**
     * Führt die Migration aus.
     */
    abstract fun up()
}

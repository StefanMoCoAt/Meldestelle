package at.mocode.database

import at.mocode.model.Turnier
import at.mocode.model.Bewerb
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SortOrder

/**
 * Repository for Turnier (Tournament) operations
 */
class TurnierRepository {
    /**
     * Get all tournaments
     * @return List of all tournaments with their competitions
     */
    fun getAllTurniere(): List<Turnier> = transaction {
        TurnierEntity.all()
            .orderBy(Turniere.datum to SortOrder.ASC)
            .map { it.toModel() }
    }

    /**
     * Get a tournament by its number
     * @param number The tournament number
     * @return The tournament or null if not found
     */
    fun getTurnierByNumber(number: Int): Turnier? = transaction {
        TurnierEntity.find { Turniere.number eq number }
            .firstOrNull()
            ?.toModel()
    }

    /**
     * Create a new tournament
     * @param turnier The tournament to create
     * @return The created tournament with its ID
     */
    fun createTurnier(turnier: Turnier): Turnier = transaction {
        // Create the tournament
        val turnierEntity = TurnierEntity.new {
            name = turnier.name
            datum = turnier.datum
            number = turnier.number
        }

        // Create the competitions
        turnier.bewerbe.forEach { bewerb ->
            BewerbEntity.new {
                nummer = bewerb.nummer
                titel = bewerb.titel
                klasse = bewerb.klasse
                task = bewerb.task
                this.turnier = turnierEntity
            }
        }

        turnierEntity.toModel()
    }

    /**
     * Update an existing tournament
     * @param number The tournament number
     * @param turnier The updated tournament data
     * @return The updated tournament or null if not found
     */
    fun updateTurnier(number: Int, turnier: Turnier): Turnier? = transaction {
        val turnierEntity = TurnierEntity.find { Turniere.number eq number }.firstOrNull() ?: return@transaction null

        // Update tournament fields
        turnierEntity.name = turnier.name
        turnierEntity.datum = turnier.datum

        // Delete existing competitions and create new ones
        turnierEntity.bewerbe.forEach { it.delete() }

        // Create new competitions
        turnier.bewerbe.forEach { bewerb ->
            BewerbEntity.new {
                nummer = bewerb.nummer
                titel = bewerb.titel
                klasse = bewerb.klasse
                task = bewerb.task
                this.turnier = turnierEntity
            }
        }

        turnierEntity.toModel()
    }

    /**
     * Delete a tournament
     * @param number The tournament number
     * @return true if deleted, false if not found
     */
    fun deleteTurnier(number: Int): Boolean = transaction {
        val turnierEntity = TurnierEntity.find { Turniere.number eq number }.firstOrNull() ?: return@transaction false

        // Delete all competitions first
        turnierEntity.bewerbe.forEach { it.delete() }

        // Delete the tournament
        turnierEntity.delete()

        true
    }

    /**
     * Add sample data to the database if it's empty
     */
    fun addSampleDataIfEmpty() = transaction {
        if (TurnierEntity.count() == 0L) {
            // Sample data from ApiRouting.kt
            val turniere = listOf(
                Turnier(
                    name = "CSN-C NEU CSNP-C NEU NEUMARKT/M., OÖ",
                    datum = "7.JUNI 2025",
                    number = 25319,
                    bewerbe = listOf(
                        Bewerb(1, "Pony Stilspringprüfung", "60 cm", null),
                        Bewerb(2, "Stilspringprüfung", "60 cm", null),
                        Bewerb(3, "Pony Stilspringprüfung", "70 cm", null),
                        Bewerb(4, "Stilspringprüfung", "80 cm", null),
                        Bewerb(5, "Pony Stilspringprüfung", "95 cm", null),
                        Bewerb(6, "Stilspringprüfung", "95 cm", null),
                        Bewerb(7, "Einlaufspringprüfung", "95cm", null),
                        Bewerb(8, "Springpferdeprüfung", "105 cm", null),
                        Bewerb(9, "Stilspringprüfung", "105 cm", null),
                        Bewerb(10, "Standardspringprüfung", "105cm", null)
                    )
                ),
                Turnier(
                    name = "CSN-B LAMBACH, OÖ",
                    datum = "14.JUNI 2025",
                    number = 25320,
                    bewerbe = listOf(
                        Bewerb(1, "Stilspringprüfung", "80 cm", null),
                        Bewerb(2, "Stilspringprüfung", "95 cm", null),
                        Bewerb(3, "Standardspringprüfung", "105 cm", null)
                    )
                )
            )

            turniere.forEach { createTurnier(it) }
        }
    }
}

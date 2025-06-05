package at.mocode.repository

import at.mocode.model.Bewerb
import at.mocode.model.Turnier
import at.mocode.tables.BewerbeTable
import at.mocode.tables.TurniereTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

/**
 * Repository class for handling tournament-related database operations.
 */
class TurnierRepository {
    private val log = LoggerFactory.getLogger(this::class.java)

    /**
     * Creates a new tournament with competitions.
     * @param number The tournament number
     * @param name The tournament name
     * @param datum The tournament date
     * @param bewerbe List of competitions for the tournament
     * @return The created tournament or null if creation failed
     */
    fun createTurnier(number: Int, name: String, datum: String, bewerbe: List<Bewerb>): Turnier? = transaction {
        try {
            // Check if a tournament with this number already exists
            val existingTurnier = TurniereTable.selectAll().where { TurniereTable.number eq number }.singleOrNull()
            if (existingTurnier != null) {
                log.error("Tournament with number $number already exists")
                return@transaction null
            }

            // Insert tournament
            TurniereTable.insert {
                it[TurniereTable.number] = number
                it[TurniereTable.name] = name
                it[TurniereTable.datum] = datum
            }

            // Insert competitions
            bewerbe.forEach { bewerb ->
                BewerbeTable.insert {
                    it[BewerbeTable.nummer] = bewerb.nummer
                    it[BewerbeTable.titel] = bewerb.titel
                    it[BewerbeTable.klasse] = bewerb.klasse
                    it[BewerbeTable.task] = bewerb.task
                    it[BewerbeTable.turnierNumber] = number
                }
            }

            // Return the created tournament
            Turnier(
                number = number,
                name = name,
                datum = datum,
                bewerbe = bewerbe
            )
        } catch (e: Exception) {
            log.error("Error creating tournament", e)
            null
        }
    }

    /**
     * Updates an existing tournament with competitions.
     * @param number The tournament number
     * @param name The new tournament name
     * @param datum The new tournament date
     * @param bewerbe The new list of competitions
     * @return The updated tournament or null if update failed
     */
    fun updateTurnier(number: Int, name: String, datum: String, bewerbe: List<Bewerb>): Turnier? = transaction {
        try {
            // Check if the tournament exists
            val existingTurnier = TurniereTable.selectAll().where { TurniereTable.number eq number }.singleOrNull()
            if (existingTurnier == null) {
                log.error("Tournament with number $number not found")
                return@transaction null
            }

            // Update tournament
            TurniereTable.update({ TurniereTable.number eq number }) {
                it[TurniereTable.name] = name
                it[TurniereTable.datum] = datum
            }

            // Delete existing competitions
            BewerbeTable.deleteWhere { BewerbeTable.turnierNumber eq number }

            // Insert new competitions
            bewerbe.forEach { bewerb ->
                BewerbeTable.insert {
                    it[BewerbeTable.nummer] = bewerb.nummer
                    it[BewerbeTable.titel] = bewerb.titel
                    it[BewerbeTable.klasse] = bewerb.klasse
                    it[BewerbeTable.task] = bewerb.task
                    it[BewerbeTable.turnierNumber] = number
                }
            }

            // Return the updated tournament
            Turnier(
                number = number,
                name = name,
                datum = datum,
                bewerbe = bewerbe
            )
        } catch (e: Exception) {
            log.error("Error updating tournament", e)
            null
        }
    }

    /**
     * Retrieves all tournaments from the database with their associated competitions.
     * @return List of Turnier objects
     */
    fun getAllTurniere(): List<Turnier> = transaction {
        log.info("Fetching all tournaments from database...")

        try {
            // Get all tournaments
            val turniere = TurniereTable.selectAll().map { row ->
                Turnier(
                    name = row[TurniereTable.name],
                    datum = row[TurniereTable.datum],
                    number = row[TurniereTable.number]
                )
            }

            log.info("Found ${turniere.size} tournaments in database")

            // For each tournament, get its competitions
            turniere.forEach { turnier ->
                try {
                    val bewerbeList = BewerbeTable.selectAll().where { BewerbeTable.turnierNumber eq turnier.number }.map { row ->
                        Bewerb(
                            nummer = row[BewerbeTable.nummer],
                            titel = row[BewerbeTable.titel],
                            klasse = row[BewerbeTable.klasse],
                            task = row[BewerbeTable.task]
                        )
                    }
                    turnier.bewerbe = bewerbeList
                    log.info("Found ${bewerbeList.size} competitions for tournament ${turnier.number}")
                } catch (e: Exception) {
                    log.error("Error fetching competitions for tournament ${turnier.number}", e)
                    turnier.bewerbe = emptyList() // Set empty list to avoid null pointer exceptions
                }
            }

            turniere
        } catch (e: Exception) {
            log.error("Error fetching tournaments from database", e)
            emptyList() // Return empty list instead of throwing exception
        }
    }

    /**
     * Inserts dummy tournaments with competitions if the table is empty.
     */
    fun insertDummyTurnierIfEmpty() = transaction {
        try {
            // First check if the table is empty
            val count = TurniereTable.selectAll().count()
            log.info("Current tournament count in database: $count")

            if (count == 0L) {
                log.info("Turnier table is empty, inserting dummy tournaments...")

                try {
                    // Insert first tournament
                    val turnierNumber1 = 25319
                    TurniereTable.insert {
                        it[TurniereTable.number] = turnierNumber1
                        it[TurniereTable.name] = "CSN-C Edelhof April 2025"
                        it[TurniereTable.datum] = "14.04.2025 - 15.04.2025"
                    }
                    log.info("Inserted first tournament with number $turnierNumber1")

                    // Insert competitions for first tournament
                    BewerbeTable.insert {
                        it[BewerbeTable.nummer] = 1
                        it[BewerbeTable.titel] = "Stilspringprüfung"
                        it[BewerbeTable.klasse] = "60 cm"
                        it[BewerbeTable.task] = null
                        it[BewerbeTable.turnierNumber] = turnierNumber1
                    }

                    BewerbeTable.insert {
                        it[BewerbeTable.nummer] = 2
                        it[BewerbeTable.titel] = "Dressurprüfung"
                        it[BewerbeTable.klasse] = "Kl. A"
                        it[BewerbeTable.task] = "DRA 1"
                        it[BewerbeTable.turnierNumber] = turnierNumber1
                    }
                    log.info("Inserted competitions for first tournament")

                    // Insert second tournament (as specified in the issue description)
                    val turnierNumber2 = 25320
                    TurniereTable.insert {
                        it[TurniereTable.number] = turnierNumber2
                        it[TurniereTable.name] = "CDN-C CDNP-C NEU Neumarkt/M., OÖ"
                        it[TurniereTable.datum] = "8. JUNI 2025"
                    }
                    log.info("Inserted second tournament with number $turnierNumber2")

                    // Insert competitions for second tournament
                    BewerbeTable.insert {
                        it[BewerbeTable.nummer] = 1
                        it[BewerbeTable.titel] = "Dressurprüfung"
                        it[BewerbeTable.klasse] = "Kl. A"
                        it[BewerbeTable.task] = "DRA 2"
                        it[BewerbeTable.turnierNumber] = turnierNumber2
                    }

                    BewerbeTable.insert {
                        it[BewerbeTable.nummer] = 2
                        it[BewerbeTable.titel] = "Dressurprüfung"
                        it[BewerbeTable.klasse] = "Kl. L"
                        it[BewerbeTable.task] = "DRL 1"
                        it[BewerbeTable.turnierNumber] = turnierNumber2
                    }

                    BewerbeTable.insert {
                        it[BewerbeTable.nummer] = 3
                        it[BewerbeTable.titel] = "Dressurprüfung"
                        it[BewerbeTable.klasse] = "Kl. L"
                        it[BewerbeTable.task] = "DRL 2"
                        it[BewerbeTable.turnierNumber] = turnierNumber2
                    }
                    log.info("Inserted competitions for second tournament")

                    log.info("Dummy tournaments and competitions inserted successfully.")
                } catch (e: Exception) {
                    log.error("Error inserting dummy tournaments", e)
                    // Don't rethrow, allow the application to continue
                }
            } else {
                log.info("Turnier table is not empty, skipping dummy data insertion")
            }
        } catch (e: Exception) {
            log.error("Error checking if tournament table is empty", e)
            // Don't rethrow, allow the application to continue
        }
    }

    /**
     * Gets a tournament by its number.
     * @param number The tournament number
     * @return The tournament or null if not found
     */
    fun getTurnierByNumber(number: Int): Turnier? = transaction {
        log.info("Fetching tournament with number $number")

        try {
            // Get the tournament
            val turnierRow = TurniereTable.selectAll().where { TurniereTable.number eq number }.singleOrNull()

            if (turnierRow == null) {
                log.warn("Tournament with number $number not found")
                return@transaction null
            }

            log.info("Found tournament with number $number: ${turnierRow[TurniereTable.name]}")

            val turnier = Turnier(
                name = turnierRow[TurniereTable.name],
                datum = turnierRow[TurniereTable.datum],
                number = turnierRow[TurniereTable.number]
            )

            try {
                // Get competitions for this tournament
                val bewerbeList = BewerbeTable.selectAll().where { BewerbeTable.turnierNumber eq number }.map { row ->
                    Bewerb(
                        nummer = row[BewerbeTable.nummer],
                        titel = row[BewerbeTable.titel],
                        klasse = row[BewerbeTable.klasse],
                        task = row[BewerbeTable.task]
                    )
                }

                log.info("Found ${bewerbeList.size} competitions for tournament $number")
                turnier.bewerbe = bewerbeList
            } catch (e: Exception) {
                log.error("Error fetching competitions for tournament $number", e)
                turnier.bewerbe = emptyList() // Set empty list to avoid null pointer exceptions
            }

            turnier
        } catch (e: Exception) {
            log.error("Error fetching tournament with number $number", e)
            null
        }
    }
}

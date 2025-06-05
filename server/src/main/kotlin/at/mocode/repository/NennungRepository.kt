package at.mocode.repository

import at.mocode.config.DependencyInjection
import at.mocode.model.Nennung
import at.mocode.tables.NennungEventsTable
import at.mocode.tables.NennungenTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

/**
 * Repository class for handling registration-related database operations.
 */
class NennungRepository {
    private val log = LoggerFactory.getLogger(this::class.java)
    private val turnierRepository by lazy { DependencyInjection.turnierRepository }

    /**
     * Saves a registration to the database.
     * @param nennung The registration to save
     * @return The ID of the saved registration
     */
    fun saveNennung(nennung: Nennung): Int = transaction {
        log.info("Saving registration for ${nennung.riderName} with ${nennung.horseName}")

        // Insert the registration
        val nennungId = NennungenTable.insert {
            it[riderName] = nennung.riderName
            it[horseName] = nennung.horseName
            it[email] = nennung.email
            it[phone] = nennung.phone
            it[comments] = nennung.comments
            it[turnierNumber] = nennung.turnier.number
        } get NennungenTable.id

        // Insert the selected events
        nennung.selectedEvents.forEach { eventNumber ->
            NennungEventsTable.insert {
                it[NennungEventsTable.nennungId] = nennungId
                it[NennungEventsTable.eventNumber] = eventNumber
            }
        }

        log.info("Registration saved with ID: $nennungId")
        nennungId
    }

    /**
     * Creates a Nennung object from form parameters.
     * @param riderName The name of the rider
     * @param horseName The name of the horse
     * @param email The email address for contact
     * @param phone The phone number for contact
     * @param selectedEvents The list of selected event numbers
     * @param comments Additional comments or wishes
     * @param turnierNumber The number of the tournament
     * @return The created Nennung object, or null if the tournament was not found
     */
    fun createNennung(
        riderName: String,
        horseName: String,
        email: String,
        phone: String,
        selectedEvents: List<String>,
        comments: String,
        turnierNumber: Int
    ): Nennung? {
        val turnier = turnierRepository.getTurnierByNumber(turnierNumber) ?: return null

        return Nennung(
            riderName = riderName,
            horseName = horseName,
            email = email,
            phone = phone,
            selectedEvents = selectedEvents,
            comments = comments,
            turnier = turnier
        )
    }
}

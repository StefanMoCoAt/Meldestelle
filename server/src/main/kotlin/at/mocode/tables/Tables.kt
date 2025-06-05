package at.mocode.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Column

/**
 * Defines the structure of the "turniere" (tournaments) table in the database.
 */
object TurniereTable : Table("turniere") {
    /**
     * Unique number for the tournament, used as a primary key.
     */
    val number: Column<Int> = integer("number").uniqueIndex()

    /**
     * Name of the tournament, max 255 characters.
     */
    val name: Column<String> = varchar("name", 255)

    /**
     * Date of the tournament as text, max 100 characters.
     */
    val datum: Column<String> = varchar("datum", 100)

    // Define the 'number' column as the primary key for this table
    override val primaryKey = PrimaryKey(number)
}

/**
 * Defines the structure of the "bewerbe" (competitions) table in the database.
 */
object BewerbeTable : Table("bewerbe") {
    /**
     * Auto-generated ID for the competition.
     */
    val id: Column<Int> = integer("id").autoIncrement()

    /**
     * Amount of the competition.
     */
    val nummer: Column<Int> = integer("nummer")

    /**
     * Title of the competition.
     */
    val titel: Column<String> = varchar("titel", 255)

    /**
     * Class/level of the competition.
     */
    val klasse: Column<String> = varchar("klasse", 100)

    /**
     * Optional task identifier.
     */
    val task: Column<String?> = varchar("task", 100).nullable()

    /**
     * Foreign key to the tournament table.
     */
    val turnierNumber: Column<Int> = integer("turnier_number")

    init {
        foreignKey(turnierNumber to TurniereTable.number)
    }

    // Define the 'id' column as the primary key for this table
    override val primaryKey = PrimaryKey(id)
}

/**
 * Defines the structure of the "nennungen" (registrations) table in the database.
 */
object NennungenTable : Table("nennungen") {
    /**
     * Auto-generated ID for the registration.
     */
    val id: Column<Int> = integer("id").autoIncrement()

    /**
     * Name of the rider.
     */
    val riderName: Column<String> = varchar("rider_name", 255)

    /**
     * Name of the horse.
     */
    val horseName: Column<String> = varchar("horse_name", 255)

    /**
     * Email address for contact.
     */
    val email: Column<String> = varchar("email", 255)

    /**
     * Phone number for contact.
     */
    val phone: Column<String> = varchar("phone", 100)

    /**
     * Additional comments or wishes.
     */
    val comments: Column<String> = text("comments")

    /**
     * Foreign key to the tournament table.
     */
    val turnierNumber: Column<Int> = integer("turnier_number")

    init {
        foreignKey(turnierNumber to TurniereTable.number)
    }

    // Define the 'id' column as the primary key for this table
    override val primaryKey = PrimaryKey(id)
}

/**
 * Defines the structure of the "nennung_events" table in the database.
 * This table stores the selected competitions for each registration.
 */
object NennungEventsTable : Table("nennung_events") {
    /**
     * Auto-generated ID for the entry.
     */
    val id: Column<Int> = integer("id").autoIncrement()

    /**
     * Foreign key to the registration table.
     */
    val nennungId: Column<Int> = integer("nennung_id")

    /**
     * Amount of the selected competition.
     */
    val eventNumber: Column<String> = varchar("event_number", 100)

    init {
        foreignKey(nennungId to NennungenTable.id)
    }

    // Define the 'id' column as the primary key for this table
    override val primaryKey = PrimaryKey(id)
}

package at.mocode.database

import org.jetbrains.exposed.dao.id.IntIdTable

/**
 * Database table for tournaments (Turnier)
 */
object Turniere : IntIdTable() {
    val name = varchar("name", 255)
    val datum = varchar("datum", 50)
    val number = integer("number").uniqueIndex()
}

/**
 * Database table for competitions (Bewerb)
 */
object Bewerbe : IntIdTable() {
    val nummer = integer("nummer")
    val titel = varchar("titel", 255)
    val klasse = varchar("klasse", 50)
    val task = varchar("task", 50).nullable()
    val turnierId = reference("turnier_id", Turniere)

    // Ensure that nummer is unique within a tournament
    init {
        uniqueIndex(turnierId, nummer)
    }
}

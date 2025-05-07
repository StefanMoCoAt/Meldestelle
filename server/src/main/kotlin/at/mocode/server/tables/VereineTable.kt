package at.mocode.server.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

/**
 * Optimized version of VereineTable
 * Changes:
 * - Added indexes for common search fields (name, bundesland)
 * - Added init block for defining indexes
 */
object VereineTable : Table("vereine") {
    val id = uuid("id")
    val oepsVereinsNr = varchar("oeps_vereins_nr", 10).uniqueIndex()
    val name = varchar("name", 255)
    val kuerzel = varchar("kuerzel", 50).nullable()
    val bundesland = varchar("bundesland", 10).nullable()
    val adresse = varchar("adresse", 255).nullable()
    val plz = varchar("plz", 10).nullable()
    val ort = varchar("ort", 100).nullable()
    val email = varchar("email", 255).nullable()
    val telefon = varchar("telefon", 50).nullable()
    val webseite = varchar("webseite", 500).nullable()
    val istAktiv = bool("ist_aktiv").default(true)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)

    init {
        index(false, name)
        index(false, bundesland)
        index(false, ort)
    }
}

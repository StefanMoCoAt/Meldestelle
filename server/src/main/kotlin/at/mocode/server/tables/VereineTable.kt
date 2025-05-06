package at.mocode.server.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

/**
 * Optimized version of VereineTable
 * Changes:
 * - Added indexes for common search fields (name, bundesland)
 * - Added init block for defining indexes
 */
object VereineTable : Table(name = "vereine") {
    val id = uuid(name = "id")
    val oepsVereinsNr = varchar(name = "oeps_vereins_nr", length = 10).uniqueIndex()
    val name = varchar(name = "name", length = 255)
    val kuerzel = varchar(name = "kuerzel", length = 50).nullable()
    val bundesland = varchar(name = "bundesland", length = 10).nullable()
    val adresse = varchar(name = "adresse", length = 255).nullable()
    val plz = varchar(name = "plz", length = 10).nullable()
    val ort = varchar(name = "ort", length = 100).nullable()
    val email = varchar(name = "email", length = 255).nullable()
    val telefon = varchar(name = "telefon", length = 50).nullable()
    val webseite = varchar(name = "webseite", length = 500).nullable()
    val istAktiv = bool(name = "ist_aktiv").default(defaultValue = true)
    val createdAt = timestamp(name = "created_at")
    val updatedAt = timestamp(name = "updated_at")

    override val primaryKey = PrimaryKey(firstColumn = id)

    init {
        index(isUnique = false, name)
        index(isUnique = false, bundesland)
        index(isUnique = false, ort)
    }
}

package at.mocode.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

// --- Tabelle für Vereine ---
object VereineTable : Table("vereine") { // PostgreSQL Tabellenname
    val id = uuid("id") // KMP Uuid -> DB UUID
    val oepsVereinsNr = varchar("oeps_vereins_nr", 10).uniqueIndex() // Ist die OEPS Nummer eindeutig? Ja.
    val name = varchar("name", 255)
    val kuerzel = varchar("kuerzel", 50).nullable()
    val bundesland = varchar("bundesland", 10).nullable() // Kürzel wie NÖ, W, ST etc.
    val adresse = varchar("adresse", 255).nullable()
    val plz = varchar("plz", 10).nullable()
    val ort = varchar("ort", 100).nullable()
    val email = varchar("email", 255).nullable()
    val telefon = varchar("telefon", 50).nullable()
    val webseite = varchar("webseite", 500).nullable()
    val istAktiv = bool("ist_aktiv").default(true)
    val createdAt = timestamp("created_at") // kotlinx.datetime.Instant
    val updatedAt = timestamp("updated_at") // kotlinx.datetime.Instant

    override val primaryKey = PrimaryKey(id)
}

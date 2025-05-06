package at.mocode.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

// --- Tabelle für Artikel (falls noch nicht vorhanden) ---
object ArtikelTable : Table("artikel") {
    val id = uuid("id")
    val bezeichnung = varchar("bezeichnung", 255).uniqueIndex() // Bezeichnung sollte eindeutig sein?

    // Preis als Varchar speichern wegen KMP BigDecimal
    val preis = varchar("preis", 50)
    val einheit = varchar("einheit", 50)
    val istVerbandsabgabe = bool("ist_verbandsabgabe").default(false)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)
}

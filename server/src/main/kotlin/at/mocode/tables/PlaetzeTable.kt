package at.mocode.tables

import at.mocode.model.enums.PlatzTyp
import org.jetbrains.exposed.sql.Table

// --- Tabelle für Plätze (Austragungs- & Vorbereitungsplätze) ---
// Wichtig: Ein Platz gehört immer zu einem spezifischen Turnier!
object PlaetzeTable : Table("plaetze") {
    val id = uuid("id")

    // Fremdschlüssel zur Turniere Tabelle
    val turnierId = uuid("turnier_id").references(TurniereTable.id) // Annahme: TurniereTable existiert

    val name = varchar("name", 100) // z.B. "Sandplatz Austragung", "Halle Vorbereitung"
    val dimension = varchar("dimension", 50).nullable() // z.B. "20x40m", "50x100m"
    val boden = varchar("boden", 100).nullable() // z.B. "Sand", "Gras", "Sand/Vlies"

    // Typ des Platzes (Austragung, Vorbereitung etc.)
    val typ = enumerationByName("typ", 20, PlatzTyp::class)

    override val primaryKey = PrimaryKey(id)

    init {
        index(false, turnierId) // Index auf turnierId für schnelle Abfragen pro Turnier
    }
}

package at.mocode.tables

import at.mocode.model.enums.LizenzTyp
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date

// --- Tabelle für Lizenzen (Beispiel für Normalisierung von List<LizenzInfo>) ---
// Diese Tabelle wäre die "sauberere" Lösung für die Speicherung der Lizenzen aus Person.
// Statt sie als JSON/Text in PersonenTable zu speichern.

object LizenzenTable : Table("lizenzen") {
    val id = uuid("id")
    val personId = uuid("person_id").references(PersonenTable.id) // FK zur Person
    val lizenzTyp = enumerationByName("lizenz_typ", 50, LizenzTyp::class)
    val stufe = varchar("stufe", 20).nullable()
    // val sparte = enumerationByName("sparte", 50, Sparte::class).nullable() // Sparte Enum nötig
    val gueltigBisJahr = integer("gueltig_bis_jahr").nullable()
    val ausgestelltAm = date("ausgestellt_am").nullable()

    override val primaryKey = PrimaryKey(id)
    init {
        index(false, personId) // Index auf personId für schnelle Suche
    }
}

package at.mocode.tables

import at.mocode.model.enums.Geschlecht
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

// --- Tabelle für Personen (Reiter, Richter, Funktionäre etc.) ---
object PersonenTable : Table("personen") {
    val id = uuid("id")
    val oepsSatzNr = varchar("oeps_satz_nr", 10).uniqueIndex().nullable() // OEPS SatzNr ist eindeutig, wenn vorhanden
    val nachname = varchar("nachname", 100)
    val vorname = varchar("vorname", 100)
    val titel = varchar("titel", 50).nullable()
    val geburtsdatum = date("geburtsdatum").nullable() // kotlinx.datetime.LocalDate
    // Speichert den Enum-Namen als String, max 10 Zeichen lang
    val geschlecht = enumerationByName("geschlecht", 10, Geschlecht::class).nullable()
    val nationalitaet = varchar("nationalitaet", 3).nullable() // AUT, GER, ...
    val email = varchar("email", 255).nullable()
    val telefon = varchar("telefon", 50).nullable()
    val adresse = varchar("adresse", 255).nullable()
    val plz = varchar("plz", 10).nullable()
    val ort = varchar("ort", 100).nullable()
    // Fremdschlüssel zur Vereine Tabelle für die Stamm-Mitgliedschaft
    val stammVereinId = uuid("stamm_verein_id").references(VereineTable.id).nullable()
    val mitgliedsNummerIntern = varchar("mitglieds_nr_intern", 50).nullable()
    val letzteZahlungJahr = integer("letzte_zahlung_jahr").nullable()
    val feiId = varchar("fei_id", 20).nullable()
    val istGesperrt = bool("ist_gesperrt").default(false)
    val sperrGrund = text("sperr_grund").nullable() // Längerer Text möglich

    // Listen/Sets -> Als Text speichern für Einfachheit, später evtl. normalisieren
    // Rollen (Set<FunktionaerRolle>) -> CSV oder JSON in Textfeld
    val rollenCsv = text("rollen_csv").nullable()
    // Lizenzen (List<LizenzInfo>) -> Eigene Tabelle "LizenzenTable" wäre besser! Vorerst hier weglassen oder als JSONB.
    // val lizenzenJson = jsonb("lizenzen", ...) // Benötigt spezielle Exposed/Postgres Konfiguration
    // Qualifikationen (List<String>) -> CSV
    val qualifikationenRichterCsv = text("qualifikationen_richter_csv").nullable()
    val qualifikationenParcoursbauerCsv = text("qualifikationen_parcoursbauer_csv").nullable()

    val istAktiv = bool("ist_aktiv").default(true)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)
    // Index für schnelles Suchen nach Namen
    init {
        index(true, nachname, vorname) // Eindeutiger Index auf Nachname+Vorname? Eher nicht. Normaler Index: index(false, ...)
        index(false, nachname) // Index auf Nachname allein
    }
}

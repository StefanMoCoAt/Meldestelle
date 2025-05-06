package at.mocode.server.tables

import at.mocode.server.enums.Geschlecht
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

/**
 * Optimized version of PersonenTable
 * Changes:
 * - Added proper imports for enums
 * - Replaced inline comments with KDoc
 * - Fixed the unique index on nachname+vorname to be non-unique
 * - Added indexes for email and stammVereinId for common queries
 */
object PersonenTable : Table(name = "personen") {
    val id = uuid(name = "id")
    val oepsSatzNr = varchar(name = "oeps_satz_nr", length = 10).uniqueIndex().nullable()
    val nachname = varchar(name = "nachname", length = 100)
    val vorname = varchar(name = "vorname", length = 100)
    val titel = varchar(name = "titel", length = 50).nullable()
    val geburtsdatum = date(name = "geburtsdatum").nullable()
    val geschlecht = enumerationByName(name = "geschlecht", length = 10, klass = Geschlecht::class).nullable()
    val nationalitaet = varchar(name = "nationalitaet", length = 3).nullable()
    val email = varchar(name = "email", length = 255).nullable()
    val telefon = varchar(name = "telefon", length = 50).nullable()
    val adresse = varchar(name = "adresse", length = 255).nullable()
    val plz = varchar(name = "plz", length = 10).nullable()
    val ort = varchar(name = "ort", length = 100).nullable()
    val stammVereinId = uuid(name = "stamm_verein_id").references(ref = VereineTable.id).nullable()
    val mitgliedsNummerIntern = varchar(name = "mitglieds_nr_intern", length = 50).nullable()
    val letzteZahlungJahr = integer(name = "letzte_zahlung_jahr").nullable()
    val feiId = varchar(name = "fei_id", length = 20).nullable()
    val istGesperrt = bool(name = "ist_gesperrt").default(defaultValue = false)
    val sperrGrund = text(name = "sperr_grund").nullable()
    val rollenCsv = text(name = "rollen_csv").nullable()
    val qualifikationenRichterCsv = text(name = "qualifikationen_richter_csv").nullable()
    val qualifikationenParcoursbauerCsv = text(name = "qualifikationen_parcoursbauer_csv").nullable()
    val istAktiv = bool(name = "ist_aktiv").default(true)
    val createdAt = timestamp(name = "created_at")
    val updatedAt = timestamp(name = "updated_at")

    override val primaryKey = PrimaryKey(firstColumn = id)

    init {
        index(isUnique = false, nachname, vorname)
        index(isUnique = false, nachname)
        index(isUnique = false, email)
        index(isUnique = false, stammVereinId)
    }
}

package at.mocode.tables.stammdaten

import at.mocode.enums.GeschlechtE
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object PersonenTable : Table("personen") {
    val id = uuid("id")
    val oepsSatzNr = varchar("oeps_satz_nr", 10).uniqueIndex().nullable()
    val nachname = varchar("nachname", 100)
    val vorname = varchar("vorname", 100)
    val titel = varchar("titel", 50).nullable()
    val geburtsdatum = date("geburtsdatum").nullable()
    val geschlecht = enumerationByName("geschlecht", 10, GeschlechtE::class).nullable()
    val nationalitaet = varchar("nationalitaet", 3).nullable()
    val email = varchar("email", 255).nullable()
    val telefon = varchar("telefon", 50).nullable()
    val adresse = varchar("adresse", 255).nullable()
    val plz = varchar("plz", 10).nullable()
    val ort = varchar("ort", 100).nullable()
    val stammVereinId = uuid("stamm_verein_id").references(VereineTable.id).nullable()
    val mitgliedsNummerIntern = varchar("mitglieds_nr_intern", 50).nullable()
    val letzteZahlungJahr = integer("letzte_zahlung_jahr").nullable()
    val feiId = varchar("fei_id", 20).nullable()
    val istGesperrt = bool("ist_gesperrt").default(false)
    val sperrGrund = text("sperr_grund").nullable()
    val rollenCsv = text("rollen_csv").nullable()
    val qualifikationenRichterCsv = text("qualifikationen_richter_csv").nullable()
    val qualifikationenParcoursbauerCsv = text("qualifikationen_parcoursbauer_csv").nullable()
    val istAktiv = bool("ist_aktiv").default(true)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)

    init {
        index(false, nachname, vorname)
        index(false, nachname)
        index(false, email)
        index(false, stammVereinId)
    }
}

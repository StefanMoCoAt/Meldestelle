package at.mocode.tables.domaene

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object DomPersonTable : Table("dom_personen") {
    val personId = uuid("person_id")
    val oepsSatzNr = varchar("oeps_satz_nr", 10).nullable().uniqueIndex()
    val nachname = varchar("nachname", 255)
    val vorname = varchar("vorname", 255)
    val titel = varchar("titel", 100).nullable()
    val geburtsdatum = date("geburtsdatum").nullable()
    val geschlechtE = varchar("geschlecht", 20).nullable()
    val nationalitaetLandId = uuid("nationalitaet_land_id").nullable() // FK to LandDefinition when implemented
    val feiId = varchar("fei_id", 50).nullable()
    val telefon = varchar("telefon", 50).nullable()
    val email = varchar("email", 255).nullable()

    // Adresse
    val strasse = varchar("strasse", 255).nullable()
    val plz = varchar("plz", 20).nullable()
    val ort = varchar("ort", 255).nullable()
    val adresszusatzZusatzinfo = varchar("adresszusatz_zusatzinfo", 255).nullable()

    val stammVereinId = uuid("stamm_verein_id").nullable() // FK to DomVerein when implemented
    val mitgliedsNummerBeiStammVerein = varchar("mitglieds_nummer_bei_stamm_verein", 50).nullable()

    val istGesperrt = bool("ist_gesperrt").default(false)
    val sperrGrund = varchar("sperr_grund", 500).nullable()

    val altersklasseOepsCodeRaw = varchar("altersklasse_oeps_code_raw", 10).nullable()
    val istJungerReiterOepsFlag = bool("ist_junger_reiter_oeps_flag").default(false)
    val kaderStatusOepsRaw = varchar("kader_status_oeps_raw", 10).nullable()

    val datenQuelle = varchar("daten_quelle", 50).default("MANUELL")
    val istAktiv = bool("ist_aktiv").default(true)
    val notizenIntern = text("notizen_intern").nullable()

    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(personId)

    init {
        index(false, nachname)
        index(false, vorname)
        index(false, oepsSatzNr)
        index(false, feiId)
        index(false, stammVereinId)
        index(false, istGesperrt)
        index(false, istAktiv)
        index(false, datenQuelle)
    }
}

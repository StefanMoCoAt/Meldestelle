package at.mocode.tables.zns_staging

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

// ZNS Staging models tables
object PersonZNSStagingTable : Table("person_zns_staging") {
    val id = uuid("id")
    val oepsSatzNrPerson = varchar("oeps_satz_nr_person", 10).nullable()
    val familiennameRoh = varchar("familienname_roh", 255)
    val vornameRoh = varchar("vorname_roh", 255)
    val geburtsdatumTextRoh = varchar("geburtsdatum_text_roh", 20).nullable()
    val geschlechtCodeRoh = varchar("geschlecht_code_roh", 5).nullable()
    val nationalitaetCodeRoh = varchar("nationalitaet_code_roh", 10).nullable()
    val feiIdPersonRoh = varchar("fei_id_person_roh", 50).nullable()
    val telefonRoh = varchar("telefon_roh", 50).nullable()
    val vereinsnameOepsRoh = varchar("vereinsname_oeps_roh", 255).nullable()
    val bundeslandCodeOepsRoh = varchar("bundesland_code_oeps_roh", 10).nullable()
    val mitgliedNrVereinRoh = varchar("mitglied_nr_verein_roh", 50).nullable()
    val sperrlisteFlagOepsRoh = varchar("sperrliste_flag_oeps_roh", 5).nullable()
    val qualifikationenRawOepsRoh = text("qualifikationen_raw_oeps_roh").nullable()
    val istVerarbeitet = bool("ist_verarbeitet").default(false)
    val verarbeitungsFehler = text("verarbeitungs_fehler").nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)

    init {
        index(false, oepsSatzNrPerson)
        index(false, istVerarbeitet)
        index(false, familiennameRoh)
        index(false, vornameRoh)
    }
}

object PferdZNSStagingTable : Table("pferd_zns_staging") {
    val id = uuid("id")
    val oepsSatzNrPferd = varchar("oeps_satz_nr_pferd", 10).nullable()
    val pferdnameRoh = varchar("pferdname_roh", 255)
    val geburtsdatumTextRoh = varchar("geburtsdatum_text_roh", 20).nullable()
    val geschlechtCodeRoh = varchar("geschlecht_code_roh", 5).nullable()
    val rasseCodeRoh = varchar("rasse_code_roh", 10).nullable()
    val feiIdPferdRoh = varchar("fei_id_pferd_roh", 50).nullable()
    val besitzerNameRoh = varchar("besitzer_name_roh", 255).nullable()
    val istVerarbeitet = bool("ist_verarbeitet").default(false)
    val verarbeitungsFehler = text("verarbeitungs_fehler").nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)

    init {
        index(false, oepsSatzNrPferd)
        index(false, istVerarbeitet)
        index(false, pferdnameRoh)
    }
}

object VereinZNSStagingTable : Table("verein_zns_staging") {
    val id = uuid("id")
    val oepsVereinsNr = varchar("oeps_vereins_nr", 10).nullable()
    val vereinsnameRoh = varchar("vereinsname_roh", 255)
    val bundeslandCodeRoh = varchar("bundesland_code_roh", 10).nullable()
    val istVerarbeitet = bool("ist_verarbeitet").default(false)
    val verarbeitungsFehler = text("verarbeitungs_fehler").nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)

    init {
        index(false, oepsVereinsNr)
        index(false, istVerarbeitet)
        index(false, vereinsnameRoh)
    }
}

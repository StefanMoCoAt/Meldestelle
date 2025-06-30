package at.mocode.tables.oeto_verwaltung

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

// OETO Administration models tables
object OETORegelReferenzTable : Table("oeto_regel_referenzen") {
    val oetoRegelReferenzId = uuid("oeto_regel_referenz_id")
    val regelCode = varchar("regel_code", 50).uniqueIndex()
    val paragraphNummer = varchar("paragraph_nummer", 20).nullable()
    val titel = varchar("titel", 255)
    val beschreibung = text("beschreibung").nullable()
    val regelwerkVersion = varchar("regelwerk_version", 50).nullable()
    val gueltigVon = timestamp("gueltig_von").nullable()
    val gueltigBis = timestamp("gueltig_bis").nullable()
    val istAktiv = bool("ist_aktiv").default(true)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(oetoRegelReferenzId)

    init {
        index(false, regelCode)
        index(false, paragraphNummer)
        index(false, istAktiv)
    }
}

object QualifikationsTypTable : Table("qualifikations_typen") {
    val qualTypId = uuid("qual_typ_id")
    val qualTypCode = varchar("qual_typ_code", 50).uniqueIndex()
    val bezeichnung = varchar("bezeichnung", 255)
    val kategorie = varchar("kategorie", 100).nullable()
    val sparteE = varchar("sparte", 50).nullable()
    val beschreibung = text("beschreibung").nullable()
    val istAktiv = bool("ist_aktiv").default(true)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(qualTypId)

    init {
        index(false, qualTypCode)
        index(false, kategorie)
        index(false, sparteE)
        index(false, istAktiv)
    }
}

object SportlicheStammdatenTable : Table("sportliche_stammdaten") {
    val id = uuid("id")
    val stammdatenTyp = varchar("stammdaten_typ", 100)
    val code = varchar("code", 50)
    val bezeichnung = varchar("bezeichnung", 255)
    val sparteE = varchar("sparte", 50).nullable()
    val kategorie = varchar("kategorie", 100).nullable()
    val beschreibung = text("beschreibung").nullable()
    val istAktiv = bool("ist_aktiv").default(true)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)

    init {
        index(false, stammdatenTyp)
        index(false, code)
        index(false, sparteE)
        index(false, kategorie)
        index(false, istAktiv)
    }
}

// Additional missing tables for stammdaten models
object LizenzInfoTable : Table("lizenz_infos") {
    val id = uuid("id")
    val personId = uuid("person_id") // FK to Person when implemented
    val lizenzTyp = varchar("lizenz_typ", 100)
    val lizenzNummer = varchar("lizenz_nummer", 50).nullable()
    val ausstellungsdatum = timestamp("ausstellungsdatum").nullable()
    val gueltigBis = timestamp("gueltig_bis").nullable()
    val istAktiv = bool("ist_aktiv").default(true)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)

    init {
        index(false, personId)
        index(false, lizenzTyp)
        index(false, lizenzNummer)
        index(false, istAktiv)
    }
}

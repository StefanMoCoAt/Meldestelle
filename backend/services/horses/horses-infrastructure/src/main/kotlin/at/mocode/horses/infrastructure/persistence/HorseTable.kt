package at.mocode.horses.infrastructure.persistence

import at.mocode.core.domain.model.PferdeGeschlechtE
import at.mocode.core.domain.model.DatenQuelleE
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.core.kotlin.datetime.date
import org.jetbrains.exposed.v1.core.kotlin.datetime.timestamp
import org.jetbrains.exposed.v1.core.javaUUID

/**
 * Database table definition for horses in the horse-registry context.
 *
 * This table stores all horse information including identification,
 * ownership, breeding data, and administrative information.
 */
object HorseTable : UUIDTable("horses") {
    // Basic Information
    val pferdeName = varchar("pferde_name", 255)
    val geschlecht = enumerationByName<PferdeGeschlechtE>("geschlecht", 20)
    val geburtsdatum = date("geburtsdatum").nullable()
    val rasse = varchar("rasse", 100).nullable()
    val farbe = varchar("farbe", 100).nullable()

    // Ownership and Responsibility
    val besitzerId = javaUUID("besitzer_id").nullable()
    val verantwortlichePersonId = javaUUID("verantwortliche_person_id").nullable()

    // Breeding Information
    val zuechterName = varchar("zuechter_name", 255).nullable()
    val zuchtbuchNummer = varchar("zuchtbuch_nummer", 100).nullable()

    // Identification Numbers
    val lebensnummer = varchar("lebensnummer", 50).nullable()
    val chipNummer = varchar("chip_nummer", 50).nullable()
    val passNummer = varchar("pass_nummer", 50).nullable()
    val oepsNummer = varchar("oeps_nummer", 50).nullable()
    val feiNummer = varchar("fei_nummer", 50).nullable()

    // Pedigree Information
    val vaterName = varchar("vater_name", 255).nullable()
    val mutterName = varchar("mutter_name", 255).nullable()
    val mutterVaterName = varchar("mutter_vater_name", 255).nullable()

    // Physical Characteristics
    val stockmass = integer("stockmass").nullable()

    // Status and Administrative
    val istAktiv = bool("ist_aktiv").default(true)
    val bemerkungen = text("bemerkungen").nullable()
    val datenQuelle = enumerationByName<DatenQuelleE>("daten_quelle", 20).default(DatenQuelleE.MANUELL)

    // Audit Fields
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    init {
        // Indexes for performance
        index(false, pferdeName)
        index(false, besitzerId)
        index(false, istAktiv)

        // Unique constraints for identification numbers
        // These ensure database-level uniqueness even under concurrent access
        uniqueIndex(lebensnummer)
        uniqueIndex(chipNummer)
        uniqueIndex(passNummer)
        uniqueIndex(oepsNummer)
        uniqueIndex(feiNummer)
    }
}

package at.mocode.server.tables

import at.mocode.shared.model.enums.LizenzTyp
import at.mocode.shared.model.enums.Sparte
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date

/**
 * Optimized version of LizenzenTable
 * Changes:
 * - Added proper imports for enums
 * - Uncommented the sparte field
 * - Added index for lizenzTyp and gueltigBisJahr
 */
object LizenzenTable : Table("lizenzen") {
    val id = uuid("id")
    val personId = uuid("person_id").references(PersonenTable.id)
    val lizenzTyp = enumerationByName("lizenz_typ", 50, LizenzTyp::class)
    val stufe = varchar("stufe", 20).nullable()
    val sparte = enumerationByName("sparte", 50, Sparte::class).nullable()
    val gueltigBisJahr = integer("gueltig_bis_jahr").nullable()
    val ausgestelltAm = date("ausgestellt_am").nullable()

    override val primaryKey = PrimaryKey(id)

    init {
        index(false, personId)
        index(false, lizenzTyp, gueltigBisJahr)
    }
}

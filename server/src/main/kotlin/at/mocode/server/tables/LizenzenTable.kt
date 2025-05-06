package at.mocode.server.tables

import at.mocode.server.enums.LizenzTyp
import at.mocode.server.enums.Sparte
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date

/**
 * Optimized version of LizenzenTable
 * Changes:
 * - Added proper imports for enums
 * - Uncommented the sparte field
 * - Added index for lizenzTyp and gueltigBisJahr
 */
object LizenzenTable : Table(name = "lizenzen") {
    val id = uuid(name = "id")
    val personId = uuid(name = "person_id").references(PersonenTable.id)
    val lizenzTyp = enumerationByName(name = "lizenz_typ", length = 50, klass = LizenzTyp::class)
    val stufe = varchar(name = "stufe", 20).nullable()
    val sparte = enumerationByName(name = "sparte", length = 50, klass = Sparte::class).nullable()
    val gueltigBisJahr = integer(name = "gueltig_bis_jahr").nullable()
    val ausgestelltAm = date(name = "ausgestellt_am").nullable()

    override val primaryKey = PrimaryKey(firstColumn = id)

    init {
        index(isUnique = false, personId)
        index(isUnique = false, lizenzTyp, gueltigBisJahr)
    }
}

package at.mocode.tables


import at.mocode.enums.LizenzTypE
import at.mocode.enums.SparteE
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date

object LizenzenTable : Table("lizenzen") {
    val id = uuid("id")
    val personId = uuid("person_id").references(PersonenTable.id)
    val lizenzTyp = enumerationByName("lizenz_typ", 50, LizenzTypE::class)
    val stufe = varchar("stufe", 20).nullable()
    val sparte = enumerationByName("sparte", 50, SparteE::class).nullable()
    val gueltigBisJahr = integer("gueltig_bis_jahr").nullable()
    val ausgestelltAm = date("ausgestellt_am").nullable()

    override val primaryKey = PrimaryKey(id)

    init {
        index(false, personId)
        index(false, lizenzTyp, gueltigBisJahr)
    }
}

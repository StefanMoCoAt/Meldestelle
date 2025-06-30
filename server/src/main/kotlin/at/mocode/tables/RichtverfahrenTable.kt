package at.mocode.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object RichtverfahrenTable : Table("richtverfahren") {
    val id = uuid("id")
    val code = varchar("code", 100)
    val bezeichnung = varchar("bezeichnung", 500)
    val sparteE = varchar("sparte", 50)
    val basisRegelnBeschreibungKurz = text("basis_regeln_beschreibung_kurz").nullable()
    val oetoParagraphVerweis = varchar("oeto_paragraph_verweis", 255).nullable()
    val hatStechen = bool("hat_stechen").default(false)
    val artDesStechens = varchar("art_des_stechens", 255).nullable()
    val istAktiv = bool("ist_aktiv").default(true)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)

    init {
        index(false, code)
        index(false, sparteE)
        index(false, istAktiv)
        index(false, hatStechen)
    }
}

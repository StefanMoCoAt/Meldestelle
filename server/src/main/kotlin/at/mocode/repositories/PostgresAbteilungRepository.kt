package at.mocode.repositories

import at.mocode.enums.BeginnzeitTypE
import at.mocode.model.Abteilung
import at.mocode.tables.AbteilungTable
import com.benasher44.uuid.Uuid
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal as JavaBigDecimal

class PostgresAbteilungRepository : AbteilungRepository {

    override suspend fun findAll(): List<Abteilung> = transaction {
        AbteilungTable.selectAll().map { rowToAbteilung(it) }
    }

    override suspend fun findById(id: Uuid): Abteilung? = transaction {
        AbteilungTable.selectAll().where { AbteilungTable.id eq id }
            .map { rowToAbteilung(it) }
            .singleOrNull()
    }

    override suspend fun findByBewerbId(bewerbId: Uuid): List<Abteilung> = transaction {
        AbteilungTable.selectAll().where { AbteilungTable.bewerbId eq bewerbId }
            .map { rowToAbteilung(it) }
    }

    override suspend fun create(abteilung: Abteilung): Abteilung = transaction {
        val now = Clock.System.now()
        AbteilungTable.insert {
            it[id] = abteilung.id
            it[bewerbId] = abteilung.bewerbId
            it[abteilungsKennzeichen] = abteilung.abteilungsKennzeichen
            it[bezeichnungIntern] = abteilung.bezeichnungIntern
            it[bezeichnungAufStartliste] = abteilung.bezeichnungAufStartliste
            it[teilungsKriteriumLizenz] = abteilung.teilungsKriteriumLizenz
            it[teilungsKriteriumPferdealter] = abteilung.teilungsKriteriumPferdealter
            it[teilungsKriteriumAltersklasseReiter] = abteilung.teilungsKriteriumAltersklasseReiter
            it[teilungsKriteriumAnzahlMin] = abteilung.teilungsKriteriumAnzahlMin
            it[teilungsKriteriumAnzahlMax] = abteilung.teilungsKriteriumAnzahlMax
            it[teilungsKriteriumFreiText] = abteilung.teilungsKriteriumFreiText
            it[startgeld] = abteilung.startgeld?.let { bg -> JavaBigDecimal(bg.toStringExpanded()) }
            it[platzId] = abteilung.platzId
            it[datum] = abteilung.datum
            it[beginnzeitTypE] = abteilung.beginnzeitTypE.name
            it[beginnzeitFix] = abteilung.beginnzeitFix
            it[beginnNachAbteilungId] = abteilung.beginnNachAbteilungId
            it[beginnzeitCa] = abteilung.beginnzeitCa
            it[dauerProStartGeschaetztSek] = abteilung.dauerProStartGeschaetztSek
            it[umbauzeitNachAbteilungMin] = abteilung.umbauzeitNachAbteilungMin
            it[besichtigungszeitVorAbteilungMin] = abteilung.besichtigungszeitVorAbteilungMin
            it[stechzeitZusaetzlichMin] = abteilung.stechzeitZusaetzlichMin
            it[anzahlStarter] = abteilung.anzahlStarter
            it[istAktiv] = abteilung.istAktiv
            it[createdAt] = now
            it[updatedAt] = now
        }
        abteilung.copy(createdAt = now, updatedAt = now)
    }

    override suspend fun update(id: Uuid, abteilung: Abteilung): Abteilung? = transaction {
        val updateCount = AbteilungTable.update({ AbteilungTable.id eq id }) {
            it[bewerbId] = abteilung.bewerbId
            it[abteilungsKennzeichen] = abteilung.abteilungsKennzeichen
            it[bezeichnungIntern] = abteilung.bezeichnungIntern
            it[bezeichnungAufStartliste] = abteilung.bezeichnungAufStartliste
            it[teilungsKriteriumLizenz] = abteilung.teilungsKriteriumLizenz
            it[teilungsKriteriumPferdealter] = abteilung.teilungsKriteriumPferdealter
            it[teilungsKriteriumAltersklasseReiter] = abteilung.teilungsKriteriumAltersklasseReiter
            it[teilungsKriteriumAnzahlMin] = abteilung.teilungsKriteriumAnzahlMin
            it[teilungsKriteriumAnzahlMax] = abteilung.teilungsKriteriumAnzahlMax
            it[teilungsKriteriumFreiText] = abteilung.teilungsKriteriumFreiText
            it[startgeld] = abteilung.startgeld?.let { bg -> JavaBigDecimal(bg.toStringExpanded()) }
            it[platzId] = abteilung.platzId
            it[datum] = abteilung.datum
            it[beginnzeitTypE] = abteilung.beginnzeitTypE.name
            it[beginnzeitFix] = abteilung.beginnzeitFix
            it[beginnNachAbteilungId] = abteilung.beginnNachAbteilungId
            it[beginnzeitCa] = abteilung.beginnzeitCa
            it[dauerProStartGeschaetztSek] = abteilung.dauerProStartGeschaetztSek
            it[umbauzeitNachAbteilungMin] = abteilung.umbauzeitNachAbteilungMin
            it[besichtigungszeitVorAbteilungMin] = abteilung.besichtigungszeitVorAbteilungMin
            it[stechzeitZusaetzlichMin] = abteilung.stechzeitZusaetzlichMin
            it[anzahlStarter] = abteilung.anzahlStarter
            it[istAktiv] = abteilung.istAktiv
            it[updatedAt] = Clock.System.now()
        }
        if (updateCount > 0) {
            AbteilungTable.selectAll().where { AbteilungTable.id eq id }
                .map { rowToAbteilung(it) }
                .singleOrNull()
        } else null
    }

    override suspend fun delete(id: Uuid): Boolean = transaction {
        AbteilungTable.deleteWhere { AbteilungTable.id eq id } > 0
    }

    override suspend fun search(query: String): List<Abteilung> = transaction {
        AbteilungTable.selectAll().where {
            (AbteilungTable.abteilungsKennzeichen.lowerCase() like "%${query.lowercase()}%") or
                AbteilungTable.bezeichnungIntern.lowerCase().like("%${query.lowercase()}%") or
                AbteilungTable.bezeichnungAufStartliste.lowerCase().like("%${query.lowercase()}%")
        }.map { rowToAbteilung(it) }
    }

    override suspend fun findByAktiv(istAktiv: Boolean): List<Abteilung> = transaction {
        AbteilungTable.selectAll().where { AbteilungTable.istAktiv eq istAktiv }
            .map { rowToAbteilung(it) }
    }

    private fun rowToAbteilung(row: ResultRow): Abteilung {
        return Abteilung(
            id = row[AbteilungTable.id],
            bewerbId = row[AbteilungTable.bewerbId],
            abteilungsKennzeichen = row[AbteilungTable.abteilungsKennzeichen],
            bezeichnungIntern = row[AbteilungTable.bezeichnungIntern],
            bezeichnungAufStartliste = row[AbteilungTable.bezeichnungAufStartliste],
            teilungsKriteriumLizenz = row[AbteilungTable.teilungsKriteriumLizenz],
            teilungsKriteriumPferdealter = row[AbteilungTable.teilungsKriteriumPferdealter],
            teilungsKriteriumAltersklasseReiter = row[AbteilungTable.teilungsKriteriumAltersklasseReiter],
            teilungsKriteriumAnzahlMin = row[AbteilungTable.teilungsKriteriumAnzahlMin],
            teilungsKriteriumAnzahlMax = row[AbteilungTable.teilungsKriteriumAnzahlMax],
            teilungsKriteriumFreiText = row[AbteilungTable.teilungsKriteriumFreiText],
            startgeld = row[AbteilungTable.startgeld]?.let {
                try {
                    BigDecimal.parseString(it.toString())
                } catch (_: Exception) {
                    null
                }
            },
            dotierungen = emptyList(), // TODO: Load from related table when implemented
            platzId = row[AbteilungTable.platzId],
            datum = row[AbteilungTable.datum],
            beginnzeitTypE = try {
                BeginnzeitTypE.valueOf(row[AbteilungTable.beginnzeitTypE])
            } catch (_: Exception) {
                BeginnzeitTypE.ANSCHLIESSEND
            },
            beginnzeitFix = row[AbteilungTable.beginnzeitFix],
            beginnNachAbteilungId = row[AbteilungTable.beginnNachAbteilungId],
            beginnzeitCa = row[AbteilungTable.beginnzeitCa],
            dauerProStartGeschaetztSek = row[AbteilungTable.dauerProStartGeschaetztSek],
            umbauzeitNachAbteilungMin = row[AbteilungTable.umbauzeitNachAbteilungMin],
            besichtigungszeitVorAbteilungMin = row[AbteilungTable.besichtigungszeitVorAbteilungMin],
            stechzeitZusaetzlichMin = row[AbteilungTable.stechzeitZusaetzlichMin],
            anzahlStarter = row[AbteilungTable.anzahlStarter],
            istAktiv = row[AbteilungTable.istAktiv],
            createdAt = row[AbteilungTable.createdAt],
            updatedAt = row[AbteilungTable.updatedAt]
        )
    }
}

package at.mocode.repositories

import at.mocode.model.domaene.DomLizenz
import at.mocode.tables.domaene.DomLizenzTable
import com.benasher44.uuid.Uuid
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class PostgresDomLizenzRepository : DomLizenzRepository {

    override suspend fun findAll(): List<DomLizenz> = transaction {
        DomLizenzTable.selectAll().map { rowToDomLizenz(it) }
    }

    override suspend fun findById(id: Uuid): DomLizenz? = transaction {
        DomLizenzTable.select { DomLizenzTable.lizenzId eq id }
            .map { rowToDomLizenz(it) }
            .singleOrNull()
    }

    override suspend fun findByPersonId(personId: Uuid): List<DomLizenz> = transaction {
        DomLizenzTable.select { DomLizenzTable.personId eq personId }
            .map { rowToDomLizenz(it) }
    }

    override suspend fun findByLizenzTypGlobalId(lizenzTypGlobalId: Uuid): List<DomLizenz> = transaction {
        DomLizenzTable.select { DomLizenzTable.lizenzTypGlobalId eq lizenzTypGlobalId }
            .map { rowToDomLizenz(it) }
    }

    override suspend fun findActiveByPersonId(personId: Uuid): List<DomLizenz> = transaction {
        DomLizenzTable.select {
            (DomLizenzTable.personId eq personId) and (DomLizenzTable.istAktivBezahltOeps eq true)
        }.map { rowToDomLizenz(it) }
    }

    override suspend fun findByValidityYear(year: Int): List<DomLizenz> = transaction {
        DomLizenzTable.select { DomLizenzTable.gueltigBisJahr eq year }
            .map { rowToDomLizenz(it) }
    }

    override suspend fun create(domLizenz: DomLizenz): DomLizenz = transaction {
        val now = Clock.System.now()
        DomLizenzTable.insert {
            it[lizenzId] = domLizenz.lizenzId
            it[personId] = domLizenz.personId
            it[lizenzTypGlobalId] = domLizenz.lizenzTypGlobalId
            it[gueltigBisJahr] = domLizenz.gueltigBisJahr
            it[ausgestelltAm] = domLizenz.ausgestelltAm
            it[istAktivBezahltOeps] = domLizenz.istAktivBezahltOeps
            it[notiz] = domLizenz.notiz
            it[createdAt] = domLizenz.createdAt
            it[updatedAt] = now
        }
        domLizenz.copy(updatedAt = now)
    }

    override suspend fun update(id: Uuid, domLizenz: DomLizenz): DomLizenz? = transaction {
        val now = Clock.System.now()
        val updateCount = DomLizenzTable.update({ DomLizenzTable.lizenzId eq id }) {
            it[personId] = domLizenz.personId
            it[lizenzTypGlobalId] = domLizenz.lizenzTypGlobalId
            it[gueltigBisJahr] = domLizenz.gueltigBisJahr
            it[ausgestelltAm] = domLizenz.ausgestelltAm
            it[istAktivBezahltOeps] = domLizenz.istAktivBezahltOeps
            it[notiz] = domLizenz.notiz
            it[updatedAt] = now
        }
        if (updateCount > 0) {
            domLizenz.copy(lizenzId = id, updatedAt = now)
        } else {
            null
        }
    }

    override suspend fun delete(id: Uuid): Boolean = transaction {
        DomLizenzTable.deleteWhere { lizenzId eq id } > 0
    }

    override suspend fun search(query: String): List<DomLizenz> = transaction {
        DomLizenzTable.select {
            DomLizenzTable.notiz like "%$query%"
        }.map { rowToDomLizenz(it) }
    }

    private fun rowToDomLizenz(row: ResultRow): DomLizenz {
        return DomLizenz(
            lizenzId = row[DomLizenzTable.lizenzId],
            personId = row[DomLizenzTable.personId],
            lizenzTypGlobalId = row[DomLizenzTable.lizenzTypGlobalId],
            gueltigBisJahr = row[DomLizenzTable.gueltigBisJahr],
            ausgestelltAm = row[DomLizenzTable.ausgestelltAm],
            istAktivBezahltOeps = row[DomLizenzTable.istAktivBezahltOeps],
            notiz = row[DomLizenzTable.notiz],
            createdAt = row[DomLizenzTable.createdAt],
            updatedAt = row[DomLizenzTable.updatedAt]
        )
    }
}

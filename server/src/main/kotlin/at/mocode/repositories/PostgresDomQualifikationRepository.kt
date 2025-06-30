package at.mocode.repositories

import at.mocode.model.domaene.DomQualifikation
import at.mocode.tables.domaene.DomQualifikationTable
import com.benasher44.uuid.Uuid
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class PostgresDomQualifikationRepository : DomQualifikationRepository {

    override suspend fun findAll(): List<DomQualifikation> = transaction {
        DomQualifikationTable.selectAll().map { rowToDomQualifikation(it) }
    }

    override suspend fun findById(id: Uuid): DomQualifikation? = transaction {
        DomQualifikationTable.select { DomQualifikationTable.qualifikationId eq id }
            .map { rowToDomQualifikation(it) }
            .singleOrNull()
    }

    override suspend fun findByPersonId(personId: Uuid): List<DomQualifikation> = transaction {
        DomQualifikationTable.select { DomQualifikationTable.personId eq personId }
            .map { rowToDomQualifikation(it) }
    }

    override suspend fun findByQualTypId(qualTypId: Uuid): List<DomQualifikation> = transaction {
        DomQualifikationTable.select { DomQualifikationTable.qualTypId eq qualTypId }
            .map { rowToDomQualifikation(it) }
    }

    override suspend fun findActiveByPersonId(personId: Uuid): List<DomQualifikation> = transaction {
        DomQualifikationTable.select {
            (DomQualifikationTable.personId eq personId) and (DomQualifikationTable.istAktiv eq true)
        }.map { rowToDomQualifikation(it) }
    }

    override suspend fun findByValidityPeriod(fromDate: LocalDate?, toDate: LocalDate?): List<DomQualifikation> = transaction {
        var query = DomQualifikationTable.selectAll()

        if (fromDate != null) {
            query = query.andWhere {
                DomQualifikationTable.gueltigVon.isNull() or (DomQualifikationTable.gueltigVon greaterEq fromDate)
            }
        }

        if (toDate != null) {
            query = query.andWhere {
                DomQualifikationTable.gueltigBis.isNull() or (DomQualifikationTable.gueltigBis lessEq toDate)
            }
        }

        query.map { rowToDomQualifikation(it) }
    }

    override suspend fun create(domQualifikation: DomQualifikation): DomQualifikation = transaction {
        val now = Clock.System.now()
        DomQualifikationTable.insert {
            it[qualifikationId] = domQualifikation.qualifikationId
            it[personId] = domQualifikation.personId
            it[qualTypId] = domQualifikation.qualTypId
            it[bemerkung] = domQualifikation.bemerkung
            it[gueltigVon] = domQualifikation.gueltigVon
            it[gueltigBis] = domQualifikation.gueltigBis
            it[istAktiv] = domQualifikation.istAktiv
            it[createdAt] = domQualifikation.createdAt
            it[updatedAt] = now
        }
        domQualifikation.copy(updatedAt = now)
    }

    override suspend fun update(id: Uuid, domQualifikation: DomQualifikation): DomQualifikation? = transaction {
        val now = Clock.System.now()
        val updateCount = DomQualifikationTable.update({ DomQualifikationTable.qualifikationId eq id }) {
            it[personId] = domQualifikation.personId
            it[qualTypId] = domQualifikation.qualTypId
            it[bemerkung] = domQualifikation.bemerkung
            it[gueltigVon] = domQualifikation.gueltigVon
            it[gueltigBis] = domQualifikation.gueltigBis
            it[istAktiv] = domQualifikation.istAktiv
            it[updatedAt] = now
        }
        if (updateCount > 0) {
            domQualifikation.copy(qualifikationId = id, updatedAt = now)
        } else {
            null
        }
    }

    override suspend fun delete(id: Uuid): Boolean = transaction {
        DomQualifikationTable.deleteWhere { qualifikationId eq id } > 0
    }

    override suspend fun search(query: String): List<DomQualifikation> = transaction {
        DomQualifikationTable.select {
            DomQualifikationTable.bemerkung like "%$query%"
        }.map { rowToDomQualifikation(it) }
    }

    private fun rowToDomQualifikation(row: ResultRow): DomQualifikation {
        return DomQualifikation(
            qualifikationId = row[DomQualifikationTable.qualifikationId],
            personId = row[DomQualifikationTable.personId],
            qualTypId = row[DomQualifikationTable.qualTypId],
            bemerkung = row[DomQualifikationTable.bemerkung],
            gueltigVon = row[DomQualifikationTable.gueltigVon],
            gueltigBis = row[DomQualifikationTable.gueltigBis],
            istAktiv = row[DomQualifikationTable.istAktiv],
            createdAt = row[DomQualifikationTable.createdAt],
            updatedAt = row[DomQualifikationTable.updatedAt]
        )
    }
}

package at.mocode.masterdata.infrastructure.persistence

import at.mocode.core.domain.model.PlatzTypE
import at.mocode.masterdata.domain.model.Platz
import at.mocode.masterdata.domain.repository.PlatzRepository
import at.mocode.core.utils.database.DatabaseFactory
import com.benasher44.uuid.Uuid
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

/**
 * Implementierung des PlatzRepository für die Datenbankzugriffe.
 *
 * Diese Implementierung verwendet Exposed SQL für den Datenbankzugriff
 * und mappt zwischen der Platz Domain-Entität und der PlatzTable.
 */
class PlatzRepositoryImpl : PlatzRepository {

    /**
     * Konvertiert eine Datenbankzeile in ein Domain-Objekt.
     */
    private fun rowToPlatz(row: ResultRow): Platz {
        return Platz(
            id = row[PlatzTable.id],
            turnierId = row[PlatzTable.turnierId],
            name = row[PlatzTable.name],
            dimension = row[PlatzTable.dimension],
            boden = row[PlatzTable.boden],
            typ = PlatzTypE.valueOf(row[PlatzTable.typ]),
            istAktiv = row[PlatzTable.istAktiv],
            sortierReihenfolge = row[PlatzTable.sortierReihenfolge],
            createdAt = row[PlatzTable.createdAt].toInstant(TimeZone.UTC),
            updatedAt = row[PlatzTable.updatedAt].toInstant(TimeZone.UTC)
        )
    }

    override suspend fun findById(id: Uuid): Platz? = DatabaseFactory.dbQuery {
        PlatzTable.selectAll().where { PlatzTable.id eq id }
            .map(::rowToPlatz)
            .singleOrNull()
    }

    override suspend fun findByTournament(turnierId: Uuid, activeOnly: Boolean, orderBySortierung: Boolean): List<Platz> = DatabaseFactory.dbQuery {
        val query = PlatzTable.selectAll().where { PlatzTable.turnierId eq turnierId }

        if (activeOnly) {
            query.andWhere { PlatzTable.istAktiv eq true }
        }

        if (orderBySortierung) {
            query.orderBy(PlatzTable.sortierReihenfolge to SortOrder.ASC, PlatzTable.name to SortOrder.ASC)
        } else {
            query.orderBy(PlatzTable.name to SortOrder.ASC)
        }

        query.map(::rowToPlatz)
    }

    override suspend fun findByName(searchTerm: String, turnierId: Uuid?, limit: Int): List<Platz> = DatabaseFactory.dbQuery {
        val pattern = "%$searchTerm%"
        val query = PlatzTable.selectAll().where { PlatzTable.name like pattern }

        turnierId?.let {
            query.andWhere { PlatzTable.turnierId eq it }
        }

        query.limit(limit)
            .orderBy(PlatzTable.name to SortOrder.ASC)
            .map(::rowToPlatz)
    }

    override suspend fun findByType(typ: PlatzTypE, turnierId: Uuid?, activeOnly: Boolean): List<Platz> = DatabaseFactory.dbQuery {
        val query = PlatzTable.selectAll().where { PlatzTable.typ eq typ.name }

        turnierId?.let {
            query.andWhere { PlatzTable.turnierId eq it }
        }

        if (activeOnly) {
            query.andWhere { PlatzTable.istAktiv eq true }
        }

        query.orderBy(PlatzTable.name to SortOrder.ASC)
            .map(::rowToPlatz)
    }

    override suspend fun findByGroundType(boden: String, turnierId: Uuid?, activeOnly: Boolean): List<Platz> = DatabaseFactory.dbQuery {
        val query = PlatzTable.selectAll().where { PlatzTable.boden eq boden }

        turnierId?.let {
            query.andWhere { PlatzTable.turnierId eq it }
        }

        if (activeOnly) {
            query.andWhere { PlatzTable.istAktiv eq true }
        }

        query.orderBy(PlatzTable.name to SortOrder.ASC)
            .map(::rowToPlatz)
    }

    override suspend fun findByDimensions(dimension: String, turnierId: Uuid?, activeOnly: Boolean): List<Platz> = DatabaseFactory.dbQuery {
        val query = PlatzTable.selectAll().where { PlatzTable.dimension eq dimension }

        turnierId?.let {
            query.andWhere { PlatzTable.turnierId eq it }
        }

        if (activeOnly) {
            query.andWhere { PlatzTable.istAktiv eq true }
        }

        query.orderBy(PlatzTable.name to SortOrder.ASC)
            .map(::rowToPlatz)
    }

    override suspend fun findAllActive(orderBySortierung: Boolean): List<Platz> = DatabaseFactory.dbQuery {
        val query = PlatzTable.selectAll().where { PlatzTable.istAktiv eq true }

        if (orderBySortierung) {
            query.orderBy(PlatzTable.sortierReihenfolge to SortOrder.ASC, PlatzTable.name to SortOrder.ASC)
        } else {
            query.orderBy(PlatzTable.name to SortOrder.ASC)
        }

        query.map(::rowToPlatz)
    }

    override suspend fun findSuitableForDiscipline(
        requiredType: PlatzTypE,
        requiredDimensions: String?,
        turnierId: Uuid?
    ): List<Platz> = DatabaseFactory.dbQuery {
        val query = PlatzTable.selectAll().where {
            (PlatzTable.typ eq requiredType.name) and (PlatzTable.istAktiv eq true)
        }

        requiredDimensions?.let { dimensions ->
            query.andWhere { PlatzTable.dimension eq dimensions }
        }

        turnierId?.let {
            query.andWhere { PlatzTable.turnierId eq it }
        }

        query.orderBy(PlatzTable.sortierReihenfolge to SortOrder.ASC, PlatzTable.name to SortOrder.ASC)
            .map(::rowToPlatz)
    }

    override suspend fun save(platz: Platz): Platz = DatabaseFactory.dbQuery {
        val now = Clock.System.now()
        val existingPlatz = PlatzTable.selectAll().where { PlatzTable.id eq platz.id }.singleOrNull()

        if (existingPlatz == null) {
            // Insert a new venue
            PlatzTable.insert { stmt ->
                stmt[id] = platz.id
                stmt[turnierId] = platz.turnierId
                stmt[name] = platz.name
                stmt[dimension] = platz.dimension
                stmt[boden] = platz.boden
                stmt[typ] = platz.typ.name
                stmt[istAktiv] = platz.istAktiv
                stmt[sortierReihenfolge] = platz.sortierReihenfolge
                stmt[createdAt] = platz.createdAt.toLocalDateTime(TimeZone.UTC)
                stmt[updatedAt] = now.toLocalDateTime(TimeZone.UTC)
            }
        } else {
            // Update existing venue
            PlatzTable.update({ PlatzTable.id eq platz.id }) { stmt ->
                stmt[turnierId] = platz.turnierId
                stmt[name] = platz.name
                stmt[dimension] = platz.dimension
                stmt[boden] = platz.boden
                stmt[typ] = platz.typ.name
                stmt[istAktiv] = platz.istAktiv
                stmt[sortierReihenfolge] = platz.sortierReihenfolge
                stmt[updatedAt] = now.toLocalDateTime(TimeZone.UTC)
            }
        }

        platz.copy(updatedAt = now)
    }

    override suspend fun delete(id: Uuid): Boolean = DatabaseFactory.dbQuery {
        PlatzTable.deleteWhere { PlatzTable.id eq id } > 0
    }

    override suspend fun existsByNameAndTournament(name: String, turnierId: Uuid): Boolean = DatabaseFactory.dbQuery {
        PlatzTable.selectAll().where {
            (PlatzTable.name eq name) and (PlatzTable.turnierId eq turnierId)
        }.count() > 0
    }

    override suspend fun countActiveByTournament(turnierId: Uuid): Long = DatabaseFactory.dbQuery {
        PlatzTable.selectAll().where {
            (PlatzTable.turnierId eq turnierId) and (PlatzTable.istAktiv eq true)
        }.count()
    }

    override suspend fun countByTypeAndTournament(typ: PlatzTypE, turnierId: Uuid, activeOnly: Boolean): Long = DatabaseFactory.dbQuery {
        val query = PlatzTable.selectAll().where {
            (PlatzTable.typ eq typ.name) and (PlatzTable.turnierId eq turnierId)
        }

        if (activeOnly) {
            query.andWhere { PlatzTable.istAktiv eq true }
        }

        query.count()
    }

    override suspend fun findAvailableForTimeSlot(turnierId: Uuid, startTime: String?, endTime: String?): List<Platz> = DatabaseFactory.dbQuery {
        // For now, this returns all active venues for the tournament
        // This can be extended when venue scheduling functionality is implemented
        val query = PlatzTable.selectAll().where {
            (PlatzTable.turnierId eq turnierId) and (PlatzTable.istAktiv eq true)
        }

        // TODO: Add time slot availability logic when scheduling is implemented
        // This would involve joining with a scheduling/booking table to check availability

        query.orderBy(PlatzTable.sortierReihenfolge to SortOrder.ASC, PlatzTable.name to SortOrder.ASC)
            .map(::rowToPlatz)
    }
}

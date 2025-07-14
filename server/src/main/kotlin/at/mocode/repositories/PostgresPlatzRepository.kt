package at.mocode.repositories

import at.mocode.model.Platz
import at.mocode.tables.PlaetzeTable
import at.mocode.enums.PlatzTypE
import com.benasher44.uuid.Uuid
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class PostgresPlatzRepository : PlatzRepository {

    override suspend fun findAll(): List<Platz> = transaction {
        PlaetzeTable.selectAll().map { rowToPlatz(it) }
    }

    override suspend fun findById(id: Uuid): Platz? = transaction {
        PlaetzeTable.selectAll().where { PlaetzeTable.id eq id }
            .map { rowToPlatz(it) }
            .singleOrNull()
    }

    override suspend fun findByTurnierId(turnierId: Uuid): List<Platz> = transaction {
        PlaetzeTable.selectAll().where { PlaetzeTable.turnierId eq turnierId }
            .map { rowToPlatz(it) }
    }

    override suspend fun findByTyp(typ: PlatzTypE): List<Platz> = transaction {
        PlaetzeTable.selectAll().where { PlaetzeTable.typ eq typ }
            .map { rowToPlatz(it) }
    }

    override suspend fun create(platz: Platz): Platz = transaction {
        PlaetzeTable.insert {
            it[id] = platz.id
            it[turnierId] = platz.turnierId
            it[name] = platz.name
            it[dimension] = platz.dimension
            it[boden] = platz.boden
            it[typ] = platz.typ
        }
        platz
    }

    override suspend fun update(id: Uuid, platz: Platz): Platz? = transaction {
        val updateCount = PlaetzeTable.update({ PlaetzeTable.id eq id }) {
            it[turnierId] = platz.turnierId
            it[name] = platz.name
            it[dimension] = platz.dimension
            it[boden] = platz.boden
            it[typ] = platz.typ
        }
        if (updateCount > 0) {
            PlaetzeTable.selectAll().where { PlaetzeTable.id eq id }
                .map { rowToPlatz(it) }
                .singleOrNull()
        } else null
    }

    override suspend fun delete(id: Uuid): Boolean = transaction {
        PlaetzeTable.deleteWhere { PlaetzeTable.id eq id } > 0
    }

    override suspend fun search(query: String): List<Platz> = transaction {
        PlaetzeTable.selectAll().where {
            (PlaetzeTable.name.lowerCase() like "%${query.lowercase()}%") or
                (PlaetzeTable.dimension?.lowerCase()?.like("%${query.lowercase()}%") ?: Op.FALSE) or
                (PlaetzeTable.boden?.lowerCase()?.like("%${query.lowercase()}%") ?: Op.FALSE)
        }.map { rowToPlatz(it) }
    }

    private fun rowToPlatz(row: ResultRow): Platz {
        return Platz(
            id = row[PlaetzeTable.id],
            turnierId = row[PlaetzeTable.turnierId],
            name = row[PlaetzeTable.name],
            dimension = row[PlaetzeTable.dimension],
            boden = row[PlaetzeTable.boden],
            typ = row[PlaetzeTable.typ]
        )
    }
}

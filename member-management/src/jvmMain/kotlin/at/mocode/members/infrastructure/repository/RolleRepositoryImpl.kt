package at.mocode.members.infrastructure.repository

import at.mocode.enums.RolleE
import at.mocode.members.domain.model.DomRolle
import at.mocode.members.domain.repository.RolleRepository
import at.mocode.members.infrastructure.table.RolleTable
import at.mocode.shared.database.DatabaseFactory
import com.benasher44.uuid.Uuid
import kotlinx.datetime.Clock
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.TimeZone
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

/**
 * Implementierung des RolleRepository für die Datenbankzugriffe.
 */
class RolleRepositoryImpl : RolleRepository {

    /**
     * Konvertiert eine Datenbankzeile in ein Domain-Objekt.
     */
    private fun rowToDomRolle(row: ResultRow): DomRolle {
        return DomRolle(
            rolleId = row[RolleTable.id],
            rolleTyp = row[RolleTable.rolleTyp],
            name = row[RolleTable.name],
            beschreibung = row[RolleTable.beschreibung],
            istSystemRolle = row[RolleTable.istSystemRolle],
            istAktiv = row[RolleTable.istAktiv],
            createdAt = row[RolleTable.createdAt].toInstant(TimeZone.UTC),
            updatedAt = row[RolleTable.updatedAt].toInstant(TimeZone.UTC)
        )
    }

    override suspend fun save(rolle: DomRolle): DomRolle = DatabaseFactory.dbQuery {
        val now = Clock.System.now()
        val existingRolle = findById(rolle.rolleId)

        if (existingRolle == null) {
            // Insert new role
            RolleTable.insert { stmt ->
                stmt[RolleTable.id] = rolle.rolleId
                stmt[RolleTable.rolleTyp] = rolle.rolleTyp
                stmt[RolleTable.name] = rolle.name
                stmt[RolleTable.beschreibung] = rolle.beschreibung
                stmt[RolleTable.istSystemRolle] = rolle.istSystemRolle
                stmt[RolleTable.istAktiv] = rolle.istAktiv
                stmt[RolleTable.createdAt] = rolle.createdAt.toLocalDateTime(TimeZone.UTC)
                stmt[RolleTable.updatedAt] = now.toLocalDateTime(TimeZone.UTC)
            }
        } else {
            // Update existing role
            RolleTable.update({ RolleTable.id eq rolle.rolleId }) { stmt ->
                stmt[RolleTable.rolleTyp] = rolle.rolleTyp
                stmt[RolleTable.name] = rolle.name
                stmt[RolleTable.beschreibung] = rolle.beschreibung
                stmt[RolleTable.istSystemRolle] = rolle.istSystemRolle
                stmt[RolleTable.istAktiv] = rolle.istAktiv
                stmt[RolleTable.updatedAt] = now.toLocalDateTime(TimeZone.UTC)
            }
        }

        // Return updated object
        rolle.copy(updatedAt = now)
    }

    override suspend fun findById(rolleId: Uuid): DomRolle? = DatabaseFactory.dbQuery {
        RolleTable.select { RolleTable.id eq rolleId }
            .map(::rowToDomRolle)
            .singleOrNull()
    }

    override suspend fun findByTyp(rolleTyp: RolleE): DomRolle? = DatabaseFactory.dbQuery {
        RolleTable.select { RolleTable.rolleTyp eq rolleTyp }
            .map(::rowToDomRolle)
            .singleOrNull()
    }

    override suspend fun findByName(name: String): List<DomRolle> = DatabaseFactory.dbQuery {
        RolleTable.select { RolleTable.name like "%$name%" }
            .map(::rowToDomRolle)
    }

    override suspend fun findAllActive(): List<DomRolle> = DatabaseFactory.dbQuery {
        RolleTable.select { RolleTable.istAktiv eq true }
            .map(::rowToDomRolle)
    }

    override suspend fun findAll(): List<DomRolle> = DatabaseFactory.dbQuery {
        RolleTable.selectAll()
            .map(::rowToDomRolle)
    }

    override suspend fun deleteRolle(rolleId: Uuid): Boolean = DatabaseFactory.dbQuery {
        // Prüfen, ob es sich um eine Systemrolle handelt
        val rolle = findById(rolleId)
        if (rolle?.istSystemRolle == true) {
            return@dbQuery false
        }

        val rowsDeleted = RolleTable.deleteWhere { RolleTable.id eq rolleId }
        rowsDeleted > 0
    }

    override suspend fun deactivateRolle(rolleId: Uuid): Boolean = DatabaseFactory.dbQuery {
        val now = Clock.System.now()

        // Prüfen, ob es sich um eine Systemrolle handelt
        val rolle = findById(rolleId)
        if (rolle?.istSystemRolle == true) {
            return@dbQuery false
        }

        val rowsUpdated = RolleTable.update({ RolleTable.id eq rolleId }) { stmt ->
            stmt[RolleTable.istAktiv] = false
            stmt[RolleTable.updatedAt] = now.toLocalDateTime(TimeZone.UTC)
        }

        rowsUpdated > 0
    }

    override suspend fun existsByTyp(rolleTyp: RolleE): Boolean = DatabaseFactory.dbQuery {
        RolleTable.select { RolleTable.rolleTyp eq rolleTyp }
            .count() > 0
    }
}

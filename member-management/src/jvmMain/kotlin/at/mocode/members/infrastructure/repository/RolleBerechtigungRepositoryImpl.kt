package at.mocode.members.infrastructure.repository

import at.mocode.enums.BerechtigungE
import at.mocode.members.domain.model.DomBerechtigung
import at.mocode.members.domain.model.DomRolleBerechtigung
import at.mocode.members.domain.repository.RolleBerechtigungRepository
import at.mocode.members.infrastructure.table.BerechtigungTable
import at.mocode.members.infrastructure.table.RolleBerechtigungTable
import at.mocode.shared.database.DatabaseFactory
import com.benasher44.uuid.Uuid
import kotlinx.datetime.Clock
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.TimeZone
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

/**
 * Implementierung des RolleBerechtigungRepository für die Datenbankzugriffe.
 */
class RolleBerechtigungRepositoryImpl : RolleBerechtigungRepository {

    /**
     * Konvertiert eine Datenbankzeile in ein Domain-Objekt für Berechtigung.
     */
    private fun rowToDomBerechtigung(row: ResultRow): DomBerechtigung {
        return DomBerechtigung(
            berechtigungId = row[BerechtigungTable.id],
            berechtigungTyp = row[BerechtigungTable.berechtigungTyp],
            name = row[BerechtigungTable.name],
            beschreibung = row[BerechtigungTable.beschreibung],
            ressource = row[BerechtigungTable.ressource],
            aktion = row[BerechtigungTable.aktion],
            istAktiv = row[BerechtigungTable.istAktiv],
            istSystemBerechtigung = row[BerechtigungTable.istSystemBerechtigung],
            createdAt = row[BerechtigungTable.createdAt].toInstant(TimeZone.UTC),
            updatedAt = row[BerechtigungTable.updatedAt].toInstant(TimeZone.UTC)
        )
    }

    /**
     * Konvertiert eine Datenbankzeile in ein Domain-Objekt für RolleBerechtigung.
     */
    private fun rowToDomRolleBerechtigung(row: ResultRow): DomRolleBerechtigung {
        return DomRolleBerechtigung(
            rolleBerechtigungId = row[RolleBerechtigungTable.id],
            rolleId = row[RolleBerechtigungTable.rolleId],
            berechtigungId = row[RolleBerechtigungTable.berechtigungId],
            istAktiv = row[RolleBerechtigungTable.istAktiv],
            zugewiesenVon = row[RolleBerechtigungTable.zugewiesenVon],
            notizen = row[RolleBerechtigungTable.notizen],
            createdAt = row[RolleBerechtigungTable.createdAt].toInstant(TimeZone.UTC),
            updatedAt = row[RolleBerechtigungTable.updatedAt].toInstant(TimeZone.UTC)
        )
    }

    override suspend fun save(rolleBerechtigung: DomRolleBerechtigung): DomRolleBerechtigung = DatabaseFactory.dbQuery {
        val now = Clock.System.now()
        val updatedRolleBerechtigung = rolleBerechtigung.copy(updatedAt = now)

        // Check if this is an update (has existing ID) or insert (new record)
        val existingRecord = findById(rolleBerechtigung.rolleBerechtigungId)

        if (existingRecord != null) {
            // Update existing record
            RolleBerechtigungTable.update({ RolleBerechtigungTable.id eq rolleBerechtigung.rolleBerechtigungId }) { stmt ->
                stmt[RolleBerechtigungTable.rolleId] = updatedRolleBerechtigung.rolleId
                stmt[RolleBerechtigungTable.berechtigungId] = updatedRolleBerechtigung.berechtigungId
                stmt[RolleBerechtigungTable.istAktiv] = updatedRolleBerechtigung.istAktiv
                stmt[RolleBerechtigungTable.zugewiesenVon] = updatedRolleBerechtigung.zugewiesenVon
                stmt[RolleBerechtigungTable.notizen] = updatedRolleBerechtigung.notizen
                stmt[RolleBerechtigungTable.updatedAt] = updatedRolleBerechtigung.updatedAt.toLocalDateTime(TimeZone.UTC)
            }
            updatedRolleBerechtigung
        } else {
            // Insert new record
            val insertResult = RolleBerechtigungTable.insert { stmt ->
                stmt[RolleBerechtigungTable.id] = updatedRolleBerechtigung.rolleBerechtigungId
                stmt[RolleBerechtigungTable.rolleId] = updatedRolleBerechtigung.rolleId
                stmt[RolleBerechtigungTable.berechtigungId] = updatedRolleBerechtigung.berechtigungId
                stmt[RolleBerechtigungTable.istAktiv] = updatedRolleBerechtigung.istAktiv
                stmt[RolleBerechtigungTable.zugewiesenVon] = updatedRolleBerechtigung.zugewiesenVon
                stmt[RolleBerechtigungTable.notizen] = updatedRolleBerechtigung.notizen
                stmt[RolleBerechtigungTable.createdAt] = updatedRolleBerechtigung.createdAt.toLocalDateTime(TimeZone.UTC)
                stmt[RolleBerechtigungTable.updatedAt] = updatedRolleBerechtigung.updatedAt.toLocalDateTime(TimeZone.UTC)
            }

            val insertedId = insertResult[RolleBerechtigungTable.id]
            findById(insertedId)!!
        }
    }

    override suspend fun findById(rolleBerechtigungId: Uuid): DomRolleBerechtigung? = DatabaseFactory.dbQuery {
        RolleBerechtigungTable.select { RolleBerechtigungTable.id eq rolleBerechtigungId }
            .map(::rowToDomRolleBerechtigung)
            .singleOrNull()
    }

    override suspend fun findByRolleId(rolleId: Uuid, nurAktive: Boolean): List<DomRolleBerechtigung> = DatabaseFactory.dbQuery {
        val query = if (nurAktive) {
            RolleBerechtigungTable.select {
                (RolleBerechtigungTable.rolleId eq rolleId) and (RolleBerechtigungTable.istAktiv eq true)
            }
        } else {
            RolleBerechtigungTable.select { RolleBerechtigungTable.rolleId eq rolleId }
        }
        query.map(::rowToDomRolleBerechtigung)
    }

    override suspend fun findByBerechtigungId(berechtigungId: Uuid, nurAktive: Boolean): List<DomRolleBerechtigung> = DatabaseFactory.dbQuery {
        val query = if (nurAktive) {
            RolleBerechtigungTable.select {
                (RolleBerechtigungTable.berechtigungId eq berechtigungId) and (RolleBerechtigungTable.istAktiv eq true)
            }
        } else {
            RolleBerechtigungTable.select { RolleBerechtigungTable.berechtigungId eq berechtigungId }
        }
        query.map(::rowToDomRolleBerechtigung)
    }

    override suspend fun findByRolleAndBerechtigung(rolleId: Uuid, berechtigungId: Uuid): DomRolleBerechtigung? = DatabaseFactory.dbQuery {
        RolleBerechtigungTable.select {
            (RolleBerechtigungTable.rolleId eq rolleId) and (RolleBerechtigungTable.berechtigungId eq berechtigungId)
        }.map(::rowToDomRolleBerechtigung).singleOrNull()
    }

    override suspend fun findAllActive(): List<DomRolleBerechtigung> = DatabaseFactory.dbQuery {
        RolleBerechtigungTable.select { RolleBerechtigungTable.istAktiv eq true }
            .map(::rowToDomRolleBerechtigung)
    }

    override suspend fun findAll(): List<DomRolleBerechtigung> = DatabaseFactory.dbQuery {
        RolleBerechtigungTable.selectAll()
            .map(::rowToDomRolleBerechtigung)
    }

    override suspend fun deactivateRolleBerechtigung(rolleBerechtigungId: Uuid): Boolean = DatabaseFactory.dbQuery {
        val rowsUpdated = RolleBerechtigungTable.update({ RolleBerechtigungTable.id eq rolleBerechtigungId }) { stmt ->
            stmt[RolleBerechtigungTable.istAktiv] = false
            stmt[RolleBerechtigungTable.updatedAt] = Clock.System.now().toLocalDateTime(TimeZone.UTC)
        }
        rowsUpdated > 0
    }

    override suspend fun deleteRolleBerechtigung(rolleBerechtigungId: Uuid): Boolean = DatabaseFactory.dbQuery {
        val rowsDeleted = RolleBerechtigungTable.deleteWhere { RolleBerechtigungTable.id eq rolleBerechtigungId }
        rowsDeleted > 0
    }

    override suspend fun hasRolleBerechtigung(rolleId: Uuid, berechtigungId: Uuid): Boolean = DatabaseFactory.dbQuery {
        RolleBerechtigungTable.select {
            (RolleBerechtigungTable.rolleId eq rolleId) and
            (RolleBerechtigungTable.berechtigungId eq berechtigungId) and
            (RolleBerechtigungTable.istAktiv eq true)
        }.count() > 0
    }

    override suspend fun assignBerechtigungToRolle(rolleId: Uuid, berechtigungId: Uuid, zugewiesenVon: Uuid?): DomRolleBerechtigung = DatabaseFactory.dbQuery {
        // Check if assignment already exists
        val existing = findByRolleAndBerechtigung(rolleId, berechtigungId)
        if (existing != null) {
            // Relationship already exists, return it
            return@dbQuery existing
        }

        // Create new assignment
        val newAssignment = DomRolleBerechtigung(
            rolleId = rolleId,
            berechtigungId = berechtigungId,
            zugewiesenVon = zugewiesenVon
        )
        save(newAssignment)
    }

    override suspend fun revokeBerechtigungFromRolle(rolleId: Uuid, berechtigungId: Uuid): Boolean = DatabaseFactory.dbQuery {
        // Since we can't deactivate, we delete the relationship
        val rowsDeleted = RolleBerechtigungTable.deleteWhere {
            (RolleBerechtigungTable.rolleId eq rolleId) and (RolleBerechtigungTable.berechtigungId eq berechtigungId)
        }
        rowsDeleted > 0
    }
}

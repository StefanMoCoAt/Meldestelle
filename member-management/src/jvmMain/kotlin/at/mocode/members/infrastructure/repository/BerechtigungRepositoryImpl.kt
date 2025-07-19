package at.mocode.members.infrastructure.repository

import at.mocode.enums.BerechtigungE
import at.mocode.members.domain.model.DomBerechtigung
import at.mocode.members.domain.repository.BerechtigungRepository
import at.mocode.members.infrastructure.table.BerechtigungTable
import at.mocode.shared.database.DatabaseFactory
import com.benasher44.uuid.Uuid
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

/**
 * Implementierung des BerechtigungRepository für die Datenbankzugriffe.
 */
class BerechtigungRepositoryImpl : BerechtigungRepository {

    /**
     * Konvertiert eine Datenbankzeile in ein Domain-Objekt.
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

    override suspend fun save(berechtigung: DomBerechtigung): DomBerechtigung = DatabaseFactory.dbQuery {
        val now = Clock.System.now()
        val existingBerechtigung = findById(berechtigung.berechtigungId)

        if (existingBerechtigung == null) {
            // Insert new permission
            BerechtigungTable.insert { stmt ->
                stmt[BerechtigungTable.id] = berechtigung.berechtigungId
                stmt[BerechtigungTable.berechtigungTyp] = berechtigung.berechtigungTyp
                stmt[BerechtigungTable.name] = berechtigung.name
                stmt[BerechtigungTable.beschreibung] = berechtigung.beschreibung
                stmt[BerechtigungTable.ressource] = berechtigung.ressource
                stmt[BerechtigungTable.aktion] = berechtigung.aktion
                stmt[BerechtigungTable.istAktiv] = berechtigung.istAktiv
                stmt[BerechtigungTable.istSystemBerechtigung] = berechtigung.istSystemBerechtigung
                stmt[BerechtigungTable.createdAt] = berechtigung.createdAt.toLocalDateTime(TimeZone.UTC)
                stmt[BerechtigungTable.updatedAt] = now.toLocalDateTime(TimeZone.UTC)
            }
        } else {
            // Update existing permission
            BerechtigungTable.update({ BerechtigungTable.id eq berechtigung.berechtigungId }) { stmt ->
                stmt[BerechtigungTable.berechtigungTyp] = berechtigung.berechtigungTyp
                stmt[BerechtigungTable.name] = berechtigung.name
                stmt[BerechtigungTable.beschreibung] = berechtigung.beschreibung
                stmt[BerechtigungTable.ressource] = berechtigung.ressource
                stmt[BerechtigungTable.aktion] = berechtigung.aktion
                stmt[BerechtigungTable.istAktiv] = berechtigung.istAktiv
                stmt[BerechtigungTable.istSystemBerechtigung] = berechtigung.istSystemBerechtigung
                stmt[BerechtigungTable.updatedAt] = now.toLocalDateTime(TimeZone.UTC)
            }
        }

        // Return updated object
        berechtigung.copy(updatedAt = now)
    }

    override suspend fun findById(berechtigungId: Uuid): DomBerechtigung? = DatabaseFactory.dbQuery {
        BerechtigungTable.selectAll().where { BerechtigungTable.id eq berechtigungId }
            .map(::rowToDomBerechtigung)
            .singleOrNull()
    }

    override suspend fun findByTyp(berechtigungTyp: BerechtigungE): DomBerechtigung? = DatabaseFactory.dbQuery {
        BerechtigungTable.selectAll().where { BerechtigungTable.berechtigungTyp eq berechtigungTyp }
            .map(::rowToDomBerechtigung)
            .singleOrNull()
    }

    override suspend fun findByName(name: String): List<DomBerechtigung> = DatabaseFactory.dbQuery {
        BerechtigungTable.selectAll().where { BerechtigungTable.name like "%$name%" }
            .map(::rowToDomBerechtigung)
    }

    override suspend fun findByRessource(ressource: String): List<DomBerechtigung> = DatabaseFactory.dbQuery {
        BerechtigungTable.selectAll().where { BerechtigungTable.ressource eq ressource }
            .map(::rowToDomBerechtigung)
    }

    override suspend fun findByAktion(aktion: String): List<DomBerechtigung> = DatabaseFactory.dbQuery {
        BerechtigungTable.selectAll().where { BerechtigungTable.aktion eq aktion }
            .map(::rowToDomBerechtigung)
    }

    override suspend fun findAllActive(): List<DomBerechtigung> = DatabaseFactory.dbQuery {
        BerechtigungTable.selectAll().where { BerechtigungTable.istAktiv eq true }
            .map(::rowToDomBerechtigung)
    }

    override suspend fun findAll(): List<DomBerechtigung> = DatabaseFactory.dbQuery {
        BerechtigungTable.selectAll()
            .map(::rowToDomBerechtigung)
    }

    override suspend fun deactivateBerechtigung(berechtigungId: Uuid): Boolean = DatabaseFactory.dbQuery {
        val now = Clock.System.now()

        // Prüfen, ob es sich um eine Systemberechtigung handelt
        val berechtigung = findById(berechtigungId)
        if (berechtigung?.istSystemBerechtigung == true) {
            return@dbQuery false
        }

        val rowsUpdated = BerechtigungTable.update({ BerechtigungTable.id eq berechtigungId }) { stmt ->
            stmt[BerechtigungTable.istAktiv] = false
            stmt[BerechtigungTable.updatedAt] = now.toLocalDateTime(TimeZone.UTC)
        }

        rowsUpdated > 0
    }

    override suspend fun deleteBerechtigung(berechtigungId: Uuid): Boolean = DatabaseFactory.dbQuery {
        // Prüfen, ob es sich um eine Systemberechtigung handelt
        val berechtigung = findById(berechtigungId)
        if (berechtigung?.istSystemBerechtigung == true) {
            return@dbQuery false
        }

        val rowsDeleted = BerechtigungTable.deleteWhere { BerechtigungTable.id eq berechtigungId }
        rowsDeleted > 0
    }

    override suspend fun existsByTyp(berechtigungTyp: BerechtigungE): Boolean = DatabaseFactory.dbQuery {
        BerechtigungTable.selectAll().where { BerechtigungTable.berechtigungTyp eq berechtigungTyp }
            .count() > 0
    }
}

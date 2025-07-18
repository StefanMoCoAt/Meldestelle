package at.mocode.members.infrastructure.repository

import at.mocode.enums.BerechtigungE
import at.mocode.members.domain.model.DomBerechtigung
import at.mocode.members.domain.repository.BerechtigungRepository
import com.benasher44.uuid.Uuid
import kotlinx.datetime.Clock
import kotlinx.datetime.toKotlinInstant
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like

/**
 * Exposed-based implementation of BerechtigungRepository.
 *
 * This implementation provides data persistence for Berechtigung entities
 * using the Exposed SQL framework and PostgreSQL database.
 */
class BerechtigungRepositoryImpl : BerechtigungRepository {

    override suspend fun save(berechtigung: DomBerechtigung): DomBerechtigung {
        val now = Clock.System.now()
        val updatedBerechtigung = berechtigung.copy(updatedAt = now)

        BerechtigungTable.insertOrUpdate(BerechtigungTable.id) {
            it[id] = berechtigung.berechtigungId
            it[berechtigungTyp] = berechtigung.berechtigungTyp
            it[name] = berechtigung.name
            it[beschreibung] = berechtigung.beschreibung
            it[ressource] = berechtigung.ressource
            it[aktion] = berechtigung.aktion
            it[istAktiv] = berechtigung.istAktiv
            it[istSystemBerechtigung] = berechtigung.istSystemBerechtigung
            it[createdAt] = berechtigung.createdAt.toJavaInstant()
            it[updatedAt] = updatedBerechtigung.updatedAt.toJavaInstant()
        }

        return updatedBerechtigung
    }

    override suspend fun findById(berechtigungId: Uuid): DomBerechtigung? {
        return BerechtigungTable.select { BerechtigungTable.id eq berechtigungId }
            .map { rowToDomBerechtigung(it) }
            .singleOrNull()
    }

    override suspend fun findByTyp(berechtigungTyp: BerechtigungE): DomBerechtigung? {
        return BerechtigungTable.select { BerechtigungTable.berechtigungTyp eq berechtigungTyp }
            .map { rowToDomBerechtigung(it) }
            .singleOrNull()
    }

    override suspend fun findByName(name: String): List<DomBerechtigung> {
        val searchPattern = "%$name%"
        return BerechtigungTable.select { BerechtigungTable.name like searchPattern }
            .map { rowToDomBerechtigung(it) }
    }

    override suspend fun findByRessource(ressource: String): List<DomBerechtigung> {
        return BerechtigungTable.select { BerechtigungTable.ressource eq ressource }
            .map { rowToDomBerechtigung(it) }
    }

    override suspend fun findByAktion(aktion: String): List<DomBerechtigung> {
        return BerechtigungTable.select { BerechtigungTable.aktion eq aktion }
            .map { rowToDomBerechtigung(it) }
    }

    override suspend fun findAllActive(): List<DomBerechtigung> {
        return BerechtigungTable.select { BerechtigungTable.istAktiv eq true }
            .map { rowToDomBerechtigung(it) }
    }

    override suspend fun findAll(): List<DomBerechtigung> {
        return BerechtigungTable.selectAll()
            .map { rowToDomBerechtigung(it) }
    }

    override suspend fun deactivateBerechtigung(berechtigungId: Uuid): Boolean {
        val now = Clock.System.now()
        val updatedRows = BerechtigungTable.update({ BerechtigungTable.id eq berechtigungId }) {
            it[istAktiv] = false
            it[updatedAt] = now.toJavaInstant()
        }
        return updatedRows > 0
    }

    override suspend fun deleteBerechtigung(berechtigungId: Uuid): Boolean {
        // Only allow deletion of non-system permissions
        val berechtigung = findById(berechtigungId)
        if (berechtigung?.istSystemBerechtigung == true) {
            return false
        }

        val deletedRows = BerechtigungTable.deleteWhere { BerechtigungTable.id eq berechtigungId }
        return deletedRows > 0
    }

    override suspend fun existsByTyp(berechtigungTyp: BerechtigungE): Boolean {
        return BerechtigungTable.select { BerechtigungTable.berechtigungTyp eq berechtigungTyp }
            .count() > 0
    }

    /**
     * Converts a database row to a DomBerechtigung domain object.
     */
    private fun rowToDomBerechtigung(row: ResultRow): DomBerechtigung {
        return DomBerechtigung(
            berechtigungId = row[BerechtigungTable.id].value,
            berechtigungTyp = row[BerechtigungTable.berechtigungTyp],
            name = row[BerechtigungTable.name],
            beschreibung = row[BerechtigungTable.beschreibung],
            ressource = row[BerechtigungTable.ressource],
            aktion = row[BerechtigungTable.aktion],
            istAktiv = row[BerechtigungTable.istAktiv],
            istSystemBerechtigung = row[BerechtigungTable.istSystemBerechtigung],
            createdAt = row[BerechtigungTable.createdAt].toKotlinInstant(),
            updatedAt = row[BerechtigungTable.updatedAt].toKotlinInstant()
        )
    }
}

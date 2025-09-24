package at.mocode.events.infrastructure.persistence

import at.mocode.core.domain.model.SparteE
import at.mocode.events.domain.model.Veranstaltung
import at.mocode.events.domain.repository.VeranstaltungRepository
import at.mocode.core.utils.database.DatabaseFactory
import com.benasher44.uuid.Uuid
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.statements.UpdateBuilder

/**
 * Exposed-based implementation of VeranstaltungRepository.
 *
 * This implementation provides data persistence for Veranstaltung entities
 * using the Exposed SQL framework and PostgreSQL database.
 */
class VeranstaltungRepositoryImpl : VeranstaltungRepository {

    override suspend fun findById(id: Uuid): Veranstaltung? = DatabaseFactory.dbQuery {
        VeranstaltungTable.selectAll().where { VeranstaltungTable.id eq id }
            .map { rowToVeranstaltung(it) }
            .singleOrNull()
    }

    override suspend fun findByName(searchTerm: String, limit: Int): List<Veranstaltung> = DatabaseFactory.dbQuery {
        val searchPattern = "%$searchTerm%"
        VeranstaltungTable.selectAll().where { VeranstaltungTable.name like searchPattern }
            .orderBy(VeranstaltungTable.startDatum, SortOrder.DESC)
            .limit(limit)
            .map { rowToVeranstaltung(it) }
    }

    override suspend fun findByVeranstalterVereinId(vereinId: Uuid, activeOnly: Boolean): List<Veranstaltung> = DatabaseFactory.dbQuery {
        val query = VeranstaltungTable.selectAll().where { VeranstaltungTable.veranstalterVereinId eq vereinId }

        if (activeOnly) {
            query.andWhere { VeranstaltungTable.istAktiv eq true }
        } else {
            query
        }.orderBy(VeranstaltungTable.startDatum, SortOrder.DESC)
         .map { rowToVeranstaltung(it) }
    }

    override suspend fun findByDateRange(startDate: LocalDate, endDate: LocalDate, activeOnly: Boolean): List<Veranstaltung> = DatabaseFactory.dbQuery {
        val query = VeranstaltungTable.selectAll().where {
            (VeranstaltungTable.startDatum greaterEq startDate) and
                (VeranstaltungTable.endDatum lessEq endDate)
        }

        if (activeOnly) {
            query.andWhere { VeranstaltungTable.istAktiv eq true }
        } else {
            query
        }.orderBy(VeranstaltungTable.startDatum)
         .map { rowToVeranstaltung(it) }
    }

    override suspend fun findByStartDate(date: LocalDate, activeOnly: Boolean): List<Veranstaltung> = DatabaseFactory.dbQuery {
        val query = VeranstaltungTable.selectAll().where { VeranstaltungTable.startDatum eq date }

        if (activeOnly) {
            query.andWhere { VeranstaltungTable.istAktiv eq true }
        } else {
            query
        }.orderBy(VeranstaltungTable.name)
         .map { rowToVeranstaltung(it) }
    }

    override suspend fun findAllActive(limit: Int, offset: Int): List<Veranstaltung> = DatabaseFactory.dbQuery {
        VeranstaltungTable.selectAll().where { VeranstaltungTable.istAktiv eq true }
            .orderBy(VeranstaltungTable.startDatum, SortOrder.DESC)
            .limit(limit, offset.toLong())
            .map { rowToVeranstaltung(it) }
    }

    override suspend fun findPublicEvents(activeOnly: Boolean): List<Veranstaltung> = DatabaseFactory.dbQuery {
        val query = VeranstaltungTable.selectAll().where { VeranstaltungTable.istOeffentlich eq true }

        if (activeOnly) {
            query.andWhere { VeranstaltungTable.istAktiv eq true }
        } else {
            query
        }.orderBy(VeranstaltungTable.startDatum, SortOrder.DESC)
         .map { rowToVeranstaltung(it) }
    }

    override suspend fun save(veranstaltung: Veranstaltung): Veranstaltung = DatabaseFactory.dbQuery {
        val now = Clock.System.now()
        val updatedVeranstaltung = veranstaltung.copy(updatedAt = now)

        // Check if a record exists
        val existingRecord = VeranstaltungTable.selectAll()
            .where { VeranstaltungTable.id eq veranstaltung.veranstaltungId }
            .singleOrNull()

        if (existingRecord != null) {
            // Update existing record
            VeranstaltungTable.update({ VeranstaltungTable.id eq veranstaltung.veranstaltungId }) {
                veranstaltungToStatement(it, updatedVeranstaltung)
            }
            updatedVeranstaltung
        } else {
            // Insert a new record
            VeranstaltungTable.insert {
                it[id] = veranstaltung.veranstaltungId
                veranstaltungToStatement(it, updatedVeranstaltung)
            }
            updatedVeranstaltung
        }
    }

    override suspend fun delete(id: Uuid): Boolean = DatabaseFactory.dbQuery {
        val deletedRows = VeranstaltungTable.deleteWhere { VeranstaltungTable.id eq id }
        deletedRows > 0
    }

    override suspend fun countActive(): Long = DatabaseFactory.dbQuery {
        VeranstaltungTable.selectAll().where { VeranstaltungTable.istAktiv eq true }
            .count()
    }

    override suspend fun countByVeranstalterVereinId(vereinId: Uuid, activeOnly: Boolean): Long = DatabaseFactory.dbQuery {
        val query = VeranstaltungTable.selectAll().where { VeranstaltungTable.veranstalterVereinId eq vereinId }

        if (activeOnly) {
            query.andWhere { VeranstaltungTable.istAktiv eq true }
        } else {
            query
        }.count()
    }

    /**
     * Converts a database row to a Veranstaltung domain object.
     */
    private fun rowToVeranstaltung(row: ResultRow): Veranstaltung {
        // Parse sparten from JSON string
        val spartenJson = row[VeranstaltungTable.sparten]
        val sparten = if (spartenJson.isNotBlank()) {
            try {
                Json.decodeFromString<List<SparteE>>(spartenJson)
            } catch (_: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }

        return Veranstaltung(
            veranstaltungId = row[VeranstaltungTable.id].value,
            name = row[VeranstaltungTable.name],
            beschreibung = row[VeranstaltungTable.beschreibung],
            startDatum = row[VeranstaltungTable.startDatum],
            endDatum = row[VeranstaltungTable.endDatum],
            ort = row[VeranstaltungTable.ort],
            veranstalterVereinId = row[VeranstaltungTable.veranstalterVereinId],
            sparten = sparten,
            istAktiv = row[VeranstaltungTable.istAktiv],
            istOeffentlich = row[VeranstaltungTable.istOeffentlich],
            maxTeilnehmer = row[VeranstaltungTable.maxTeilnehmer],
            anmeldeschluss = row[VeranstaltungTable.anmeldeschluss],
            createdAt = row[VeranstaltungTable.createdAt],
            updatedAt = row[VeranstaltungTable.updatedAt]
        )
    }

    /**
     * Maps a Veranstaltung domain object to database statement values.
     */
    private fun veranstaltungToStatement(statement: UpdateBuilder<*>, veranstaltung: Veranstaltung) {
        statement[VeranstaltungTable.name] = veranstaltung.name
        statement[VeranstaltungTable.beschreibung] = veranstaltung.beschreibung
        statement[VeranstaltungTable.startDatum] = veranstaltung.startDatum
        statement[VeranstaltungTable.endDatum] = veranstaltung.endDatum
        statement[VeranstaltungTable.ort] = veranstaltung.ort
        statement[VeranstaltungTable.veranstalterVereinId] = veranstaltung.veranstalterVereinId
        statement[VeranstaltungTable.sparten] = Json.encodeToString(veranstaltung.sparten)
        statement[VeranstaltungTable.istAktiv] = veranstaltung.istAktiv
        statement[VeranstaltungTable.istOeffentlich] = veranstaltung.istOeffentlich
        statement[VeranstaltungTable.maxTeilnehmer] = veranstaltung.maxTeilnehmer
        statement[VeranstaltungTable.anmeldeschluss] = veranstaltung.anmeldeschluss
        statement[VeranstaltungTable.createdAt] = veranstaltung.createdAt
        statement[VeranstaltungTable.updatedAt] = veranstaltung.updatedAt
    }
}

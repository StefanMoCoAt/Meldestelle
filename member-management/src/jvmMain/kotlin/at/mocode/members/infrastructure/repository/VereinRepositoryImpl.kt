package at.mocode.members.infrastructure.repository

import at.mocode.members.domain.model.DomVerein
import at.mocode.members.domain.repository.VereinRepository
import at.mocode.members.infrastructure.repository.VereinTable
import at.mocode.shared.database.DatabaseFactory
import com.benasher44.uuid.Uuid
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

/**
 * Exposed-based implementation of VereinRepository.
 *
 * This implementation provides data persistence for Verein (Club/Association) entities
 * using the Exposed SQL framework and PostgreSQL database.
 */
class VereinRepositoryImpl : VereinRepository {

    override suspend fun findById(id: Uuid): DomVerein? = DatabaseFactory.dbQuery {
        VereinTable.select { VereinTable.id eq id }
            .map { rowToDomVerein(it) }
            .singleOrNull()
    }

    override suspend fun findByOepsVereinsNr(oepsVereinsNr: String): DomVerein? = DatabaseFactory.dbQuery {
        VereinTable.select { VereinTable.oepsVereinsNr eq oepsVereinsNr }
            .map { rowToDomVerein(it) }
            .singleOrNull()
    }

    override suspend fun findByName(searchTerm: String, limit: Int): List<DomVerein> = DatabaseFactory.dbQuery {
        val searchPattern = "%$searchTerm%"
        VereinTable.select {
            (VereinTable.name like searchPattern) or
                (VereinTable.kuerzel like searchPattern)
        }
        .limit(limit)
        .map { rowToDomVerein(it) }
    }

    override suspend fun findByBundeslandId(bundeslandId: Uuid): List<DomVerein> = DatabaseFactory.dbQuery {
        VereinTable.select { VereinTable.bundeslandId eq bundeslandId }
            .map { rowToDomVerein(it) }
    }

    override suspend fun findByLandId(landId: Uuid): List<DomVerein> = DatabaseFactory.dbQuery {
        VereinTable.select { VereinTable.landId eq landId }
            .map { rowToDomVerein(it) }
    }

    override suspend fun findAllActive(limit: Int, offset: Int): List<DomVerein> = DatabaseFactory.dbQuery {
        VereinTable.select { VereinTable.istAktiv eq true }
            .limit(limit, offset.toLong())
            .map { rowToDomVerein(it) }
    }

    override suspend fun findByLocation(searchTerm: String, limit: Int): List<DomVerein> = DatabaseFactory.dbQuery {
        val searchPattern = "%$searchTerm%"
        VereinTable.select {
            (VereinTable.ort like searchPattern) or
                (VereinTable.plz like searchPattern)
        }
        .limit(limit)
        .map { rowToDomVerein(it) }
    }

    override suspend fun save(verein: DomVerein): DomVerein = DatabaseFactory.dbQuery {
        val now = Clock.System.now()
        val existingVerein = findById(verein.vereinId)

        if (existingVerein == null) {
            // Insert new verein
            VereinTable.insert { stmt ->
                stmt[VereinTable.id] = verein.vereinId
                stmt[VereinTable.oepsVereinsNr] = verein.oepsVereinsNr
                stmt[VereinTable.name] = verein.name
                stmt[VereinTable.kuerzel] = verein.kuerzel
                stmt[VereinTable.adresseStrasse] = verein.adresseStrasse
                stmt[VereinTable.plz] = verein.plz
                stmt[VereinTable.ort] = verein.ort
                stmt[VereinTable.bundeslandId] = verein.bundeslandId
                stmt[VereinTable.landId] = verein.landId
                stmt[VereinTable.emailAllgemein] = verein.emailAllgemein
                stmt[VereinTable.telefonAllgemein] = verein.telefonAllgemein
                stmt[VereinTable.webseiteUrl] = verein.webseiteUrl
                stmt[VereinTable.datenQuelle] = verein.datenQuelle
                stmt[VereinTable.istAktiv] = verein.istAktiv
                stmt[VereinTable.notizenIntern] = verein.notizenIntern
                stmt[VereinTable.createdAt] = verein.createdAt.toLocalDateTime(TimeZone.UTC)
                stmt[VereinTable.updatedAt] = now.toLocalDateTime(TimeZone.UTC)
            }
        } else {
            // Update existing verein
            VereinTable.update({ VereinTable.id eq verein.vereinId }) { stmt ->
                stmt[VereinTable.oepsVereinsNr] = verein.oepsVereinsNr
                stmt[VereinTable.name] = verein.name
                stmt[VereinTable.kuerzel] = verein.kuerzel
                stmt[VereinTable.adresseStrasse] = verein.adresseStrasse
                stmt[VereinTable.plz] = verein.plz
                stmt[VereinTable.ort] = verein.ort
                stmt[VereinTable.bundeslandId] = verein.bundeslandId
                stmt[VereinTable.landId] = verein.landId
                stmt[VereinTable.emailAllgemein] = verein.emailAllgemein
                stmt[VereinTable.telefonAllgemein] = verein.telefonAllgemein
                stmt[VereinTable.webseiteUrl] = verein.webseiteUrl
                stmt[VereinTable.datenQuelle] = verein.datenQuelle
                stmt[VereinTable.istAktiv] = verein.istAktiv
                stmt[VereinTable.notizenIntern] = verein.notizenIntern
                stmt[VereinTable.updatedAt] = now.toLocalDateTime(TimeZone.UTC)
            }
        }

        verein.copy(updatedAt = now)
    }

    override suspend fun delete(id: Uuid): Boolean = DatabaseFactory.dbQuery {
        val deletedRows = VereinTable.deleteWhere { VereinTable.id eq id }
        deletedRows > 0
    }

    override suspend fun existsByOepsVereinsNr(oepsVereinsNr: String): Boolean = DatabaseFactory.dbQuery {
        VereinTable.select { VereinTable.oepsVereinsNr eq oepsVereinsNr }
            .count() > 0
    }

    override suspend fun countActive(): Long = DatabaseFactory.dbQuery {
        VereinTable.select { VereinTable.istAktiv eq true }
            .count()
    }

    override suspend fun countActiveByBundeslandId(bundeslandId: Uuid): Long = DatabaseFactory.dbQuery {
        VereinTable.select {
            (VereinTable.istAktiv eq true) and (VereinTable.bundeslandId eq bundeslandId)
        }.count()
    }

    /**
     * Converts a database row to a DomVerein domain object.
     */
    private fun rowToDomVerein(row: ResultRow): DomVerein {
        return DomVerein(
            vereinId = row[VereinTable.id].value,
            oepsVereinsNr = row[VereinTable.oepsVereinsNr],
            name = row[VereinTable.name],
            kuerzel = row[VereinTable.kuerzel],
            adresseStrasse = row[VereinTable.adresseStrasse],
            plz = row[VereinTable.plz],
            ort = row[VereinTable.ort],
            bundeslandId = row[VereinTable.bundeslandId],
            landId = row[VereinTable.landId],
            emailAllgemein = row[VereinTable.emailAllgemein],
            telefonAllgemein = row[VereinTable.telefonAllgemein],
            webseiteUrl = row[VereinTable.webseiteUrl],
            datenQuelle = row[VereinTable.datenQuelle],
            istAktiv = row[VereinTable.istAktiv],
            notizenIntern = row[VereinTable.notizenIntern],
            createdAt = row[VereinTable.createdAt].toInstant(TimeZone.UTC),
            updatedAt = row[VereinTable.updatedAt].toInstant(TimeZone.UTC)
        )
    }
}

package at.mocode.members.infrastructure.repository

// Import table definition and extension functions
import at.mocode.members.domain.model.DomVerein
import at.mocode.members.domain.repository.VereinRepository
import com.benasher44.uuid.Uuid
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

/**
 * Exposed-based implementation of VereinRepository.
 *
 * This implementation provides data persistence for Verein (Club/Association) entities
 * using the Exposed SQL framework and PostgreSQL database.
 */
class VereinRepositoryImpl : VereinRepository {

    override suspend fun findById(id: Uuid): DomVerein? {
        return VereinTable.selectAll().where { VereinTable.id eq id }
            .map { rowToDomVerein(it) }
            .singleOrNull()
    }

    override suspend fun findByOepsVereinsNr(oepsVereinsNr: String): DomVerein? {
        return VereinTable.selectAll().where { VereinTable.oepsVereinsNr eq oepsVereinsNr }
            .map { rowToDomVerein(it) }
            .singleOrNull()
    }

    override suspend fun findByName(searchTerm: String, limit: Int): List<DomVerein> {
        val searchPattern = "%$searchTerm%"
        return VereinTable.selectAll().where {
            (VereinTable.name like searchPattern) or
                (VereinTable.kuerzel like searchPattern)
        }
        .limit(limit)
        .map { rowToDomVerein(it) }
    }

    override suspend fun findByBundeslandId(bundeslandId: Uuid): List<DomVerein> {
        return VereinTable.selectAll().where { VereinTable.bundeslandId eq bundeslandId }
            .map { rowToDomVerein(it) }
    }

    override suspend fun findByLandId(landId: Uuid): List<DomVerein> {
        return VereinTable.selectAll().where { VereinTable.landId eq landId }
            .map { rowToDomVerein(it) }
    }

    override suspend fun findAllActive(limit: Int, offset: Int): List<DomVerein> {
        return VereinTable.selectAll().where { VereinTable.istAktiv eq true }
            .limit(limit, offset.toLong())
            .map { rowToDomVerein(it) }
    }

    override suspend fun findByLocation(searchTerm: String, limit: Int): List<DomVerein> {
        val searchPattern = "%$searchTerm%"
        return VereinTable.selectAll().where {
            (VereinTable.ort like searchPattern) or
                (VereinTable.plz like searchPattern)
        }
        .limit(limit)
        .map { rowToDomVerein(it) }
    }

    override suspend fun save(verein: DomVerein): DomVerein {
        val now = Clock.System.now()
        val updatedVerein = verein.copy(updatedAt = now)

        VereinTable.insertOrUpdate(VereinTable.id) {
            it[id] = verein.vereinId
            it[oepsVereinsNr] = verein.oepsVereinsNr
            it[name] = verein.name
            it[kuerzel] = verein.kuerzel
            it[adresseStrasse] = verein.adresseStrasse
            it[plz] = verein.plz
            it[ort] = verein.ort
            it[bundeslandId] = verein.bundeslandId
            it[landId] = verein.landId
            it[emailAllgemein] = verein.emailAllgemein
            it[telefonAllgemein] = verein.telefonAllgemein
            it[webseiteUrl] = verein.webseiteUrl
            it[datenQuelle] = verein.datenQuelle
            it[istAktiv] = verein.istAktiv
            it[notizenIntern] = verein.notizenIntern
            it[createdAt] = verein.createdAt.toLocalDateTime()
            it[updatedAt] = updatedVerein.updatedAt.toLocalDateTime()
        }

        return updatedVerein
    }

    override suspend fun delete(id: Uuid): Boolean {
        val deletedRows = VereinTable.deleteWhere { VereinTable.id eq id }
        return deletedRows > 0
    }

    override suspend fun existsByOepsVereinsNr(oepsVereinsNr: String): Boolean {
        return VereinTable.selectAll().where { VereinTable.oepsVereinsNr eq oepsVereinsNr }
            .count() > 0
    }

    override suspend fun countActive(): Long {
        return VereinTable.selectAll().where { VereinTable.istAktiv eq true }
            .count()
    }

    override suspend fun countActiveByBundeslandId(bundeslandId: Uuid): Long {
        return VereinTable.selectAll()
            .where { (VereinTable.istAktiv eq true) and (VereinTable.bundeslandId eq bundeslandId) }
        .count()
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
            createdAt = row[VereinTable.createdAt].toInstant(),
            updatedAt = row[VereinTable.updatedAt].toInstant()
        )
    }
}

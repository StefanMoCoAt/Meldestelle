package at.mocode.masterdata.infrastructure.persistence

import at.mocode.masterdata.domain.model.LandDefinition
import at.mocode.masterdata.domain.repository.LandRepository
import at.mocode.masterdata.infrastructure.persistence.LandTable
import at.mocode.core.utils.database.DatabaseFactory
import com.benasher44.uuid.Uuid
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

/**
 * Implementierung des LandRepository für die Datenbankzugriffe.
 *
 * Diese Implementierung verwendet Exposed SQL für den Datenbankzugriff
 * und mappt zwischen der LandDefinition Domain-Entität und der LandTable.
 */
class LandRepositoryImpl : LandRepository {

    /**
     * Konvertiert eine Datenbankzeile in ein Domain-Objekt.
     */
    private fun rowToLandDefinition(row: ResultRow): LandDefinition {
        return LandDefinition(
            landId = row[LandTable.id],
            isoAlpha2Code = row[LandTable.isoAlpha2Code],
            isoAlpha3Code = row[LandTable.isoAlpha3Code],
            isoNumerischerCode = row[LandTable.isoNumerischerCode],
            nameDeutsch = row[LandTable.nameDeutsch],
            nameEnglisch = row[LandTable.nameEnglisch],
            wappenUrl = row[LandTable.wappenUrl],
            istEuMitglied = row[LandTable.istEuMitglied],
            istEwrMitglied = row[LandTable.istEwrMitglied],
            istAktiv = row[LandTable.istAktiv],
            sortierReihenfolge = row[LandTable.sortierReihenfolge],
            createdAt = row[LandTable.createdAt].toInstant(TimeZone.UTC),
            updatedAt = row[LandTable.updatedAt].toInstant(TimeZone.UTC)
        )
    }

    override suspend fun findById(id: Uuid): LandDefinition? = DatabaseFactory.dbQuery {
        LandTable.selectAll().where { LandTable.id eq id }
            .map(::rowToLandDefinition)
            .singleOrNull()
    }

    override suspend fun findByIsoAlpha2Code(isoAlpha2Code: String): LandDefinition? = DatabaseFactory.dbQuery {
        LandTable.selectAll().where { LandTable.isoAlpha2Code eq isoAlpha2Code }
            .map(::rowToLandDefinition)
            .singleOrNull()
    }

    override suspend fun findByIsoAlpha3Code(isoAlpha3Code: String): LandDefinition? = DatabaseFactory.dbQuery {
        LandTable.selectAll().where { LandTable.isoAlpha3Code eq isoAlpha3Code }
            .map(::rowToLandDefinition)
            .singleOrNull()
    }

    override suspend fun findByName(searchTerm: String, limit: Int): List<LandDefinition> = DatabaseFactory.dbQuery {
        val pattern = "%$searchTerm%"
        LandTable.selectAll().where {
            (LandTable.nameDeutsch like pattern) or
            (LandTable.nameEnglisch like pattern)
        }
        .limit(limit)
        .map(::rowToLandDefinition)
    }

    override suspend fun findAllActive(orderBySortierung: Boolean): List<LandDefinition> = DatabaseFactory.dbQuery {
        val query = LandTable.selectAll().where { LandTable.istAktiv eq true }

        if (orderBySortierung) {
            query.orderBy(LandTable.sortierReihenfolge to SortOrder.ASC, LandTable.nameDeutsch to SortOrder.ASC)
        } else {
            query.orderBy(LandTable.nameDeutsch to SortOrder.ASC)
        }

        query.map(::rowToLandDefinition)
    }

    override suspend fun findEuMembers(): List<LandDefinition> = DatabaseFactory.dbQuery {
        LandTable.selectAll().where { (LandTable.istEuMitglied eq true) and (LandTable.istAktiv eq true) }
            .orderBy(LandTable.sortierReihenfolge to SortOrder.ASC, LandTable.nameDeutsch to SortOrder.ASC)
            .map(::rowToLandDefinition)
    }

    override suspend fun findEwrMembers(): List<LandDefinition> = DatabaseFactory.dbQuery {
        LandTable.selectAll().where { (LandTable.istEwrMitglied eq true) and (LandTable.istAktiv eq true) }
            .orderBy(LandTable.sortierReihenfolge to SortOrder.ASC, LandTable.nameDeutsch to SortOrder.ASC)
            .map(::rowToLandDefinition)
    }

    override suspend fun save(land: LandDefinition): LandDefinition = DatabaseFactory.dbQuery {
        val now = Clock.System.now()
        val existingLand = LandTable.selectAll().where { LandTable.id eq land.landId }.singleOrNull()

        if (existingLand == null) {
            // Insert a new country
            LandTable.insert { stmt ->
                stmt[id] = land.landId
                stmt[isoAlpha2Code] = land.isoAlpha2Code
                stmt[isoAlpha3Code] = land.isoAlpha3Code
                stmt[isoNumerischerCode] = land.isoNumerischerCode
                stmt[nameDeutsch] = land.nameDeutsch
                stmt[nameEnglisch] = land.nameEnglisch
                stmt[wappenUrl] = land.wappenUrl
                stmt[istEuMitglied] = land.istEuMitglied
                stmt[istEwrMitglied] = land.istEwrMitglied
                stmt[istAktiv] = land.istAktiv
                stmt[sortierReihenfolge] = land.sortierReihenfolge
                stmt[createdAt] = land.createdAt.toLocalDateTime(TimeZone.UTC)
                stmt[updatedAt] = now.toLocalDateTime(TimeZone.UTC)
            }
        } else {
            // Update existing country
            LandTable.update({ LandTable.id eq land.landId }) { stmt ->
                stmt[isoAlpha2Code] = land.isoAlpha2Code
                stmt[isoAlpha3Code] = land.isoAlpha3Code
                stmt[isoNumerischerCode] = land.isoNumerischerCode
                stmt[nameDeutsch] = land.nameDeutsch
                stmt[nameEnglisch] = land.nameEnglisch
                stmt[wappenUrl] = land.wappenUrl
                stmt[istEuMitglied] = land.istEuMitglied
                stmt[istEwrMitglied] = land.istEwrMitglied
                stmt[istAktiv] = land.istAktiv
                stmt[sortierReihenfolge] = land.sortierReihenfolge
                stmt[updatedAt] = now.toLocalDateTime(TimeZone.UTC)
            }
        }

        land.copy(updatedAt = now)
    }

    override suspend fun delete(id: Uuid): Boolean = DatabaseFactory.dbQuery {
        LandTable.deleteWhere { LandTable.id eq id } > 0
    }

    override suspend fun existsByIsoAlpha2Code(isoAlpha2Code: String): Boolean = DatabaseFactory.dbQuery {
        LandTable.selectAll().where { LandTable.isoAlpha2Code eq isoAlpha2Code }
            .count() > 0
    }

    override suspend fun existsByIsoAlpha3Code(isoAlpha3Code: String): Boolean = DatabaseFactory.dbQuery {
        LandTable.selectAll().where { LandTable.isoAlpha3Code eq isoAlpha3Code }
            .count() > 0
    }

    override suspend fun countActive(): Long = DatabaseFactory.dbQuery {
        LandTable.selectAll().where { LandTable.istAktiv eq true }.count()
    }
}

package at.mocode.masterdata.infrastructure.repository

import at.mocode.masterdata.domain.model.LandDefinition
import at.mocode.masterdata.domain.repository.LandRepository
import at.mocode.masterdata.infrastructure.table.LandTable
import at.mocode.shared.database.DatabaseFactory
import com.benasher44.uuid.Uuid
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

/**
 * Implementierung des LandRepository für die Datenbankzugriffe.
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
            nameDeutsch = row[LandTable.nameDe],
            nameEnglisch = row[LandTable.nameEn],
            istEuMitglied = row[LandTable.istEuMitglied],
            istEwrMitglied = row[LandTable.istEwrMitglied],
            sortierReihenfolge = row[LandTable.sortierReihenfolge],
            istAktiv = row[LandTable.istAktiv],
            createdAt = row[LandTable.erstelltAm].toInstant(TimeZone.UTC),
            updatedAt = row[LandTable.geaendertAm].toInstant(TimeZone.UTC)
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
        LandTable.selectAll().where { (LandTable.nameDe like pattern) or (LandTable.nameEn like pattern) }
        .limit(limit)
        .map(::rowToLandDefinition)
    }

    override suspend fun findAllActive(orderBySortierung: Boolean): List<LandDefinition> = DatabaseFactory.dbQuery {
        val query = LandTable.selectAll().where { LandTable.istAktiv eq true }

        if (orderBySortierung) {
            query.orderBy(LandTable.sortierReihenfolge to SortOrder.ASC, LandTable.nameDe to SortOrder.ASC)
        } else {
            query.orderBy(LandTable.nameDe to SortOrder.ASC)
        }

        query.map(::rowToLandDefinition)
    }

    override suspend fun findEuMembers(): List<LandDefinition> = DatabaseFactory.dbQuery {
        LandTable.selectAll().where { (LandTable.istEuMitglied eq true) and (LandTable.istAktiv eq true) }
            .orderBy(LandTable.sortierReihenfolge to SortOrder.ASC, LandTable.nameDe to SortOrder.ASC)
            .map(::rowToLandDefinition)
    }

    override suspend fun findEwrMembers(): List<LandDefinition> = DatabaseFactory.dbQuery {
        LandTable.selectAll().where { (LandTable.istEwrMitglied eq true) and (LandTable.istAktiv eq true) }
            .orderBy(LandTable.sortierReihenfolge to SortOrder.ASC, LandTable.nameDe to SortOrder.ASC)
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
                stmt[nameDe] = land.nameDeutsch
                stmt[nameEn] = land.nameEnglisch ?: ""
                stmt[istEuMitglied] = land.istEuMitglied ?: false
                stmt[istEwrMitglied] = land.istEwrMitglied ?: false
                stmt[sortierReihenfolge] = land.sortierReihenfolge ?: 999
                stmt[istAktiv] = land.istAktiv
                stmt[erstelltAm] = land.createdAt.toLocalDateTime(TimeZone.UTC)
                stmt[geaendertAm] = now.toLocalDateTime(TimeZone.UTC)
            }
        } else {
            // Update existing country
            LandTable.update({ LandTable.id eq land.landId }) { stmt ->
                stmt[isoAlpha2Code] = land.isoAlpha2Code
                stmt[isoAlpha3Code] = land.isoAlpha3Code
                stmt[nameDe] = land.nameDeutsch
                stmt[nameEn] = land.nameEnglisch ?: ""
                stmt[istEuMitglied] = land.istEuMitglied ?: false
                stmt[istEwrMitglied] = land.istEwrMitglied ?: false
                stmt[sortierReihenfolge] = land.sortierReihenfolge ?: 999
                stmt[istAktiv] = land.istAktiv
                stmt[geaendertAm] = now.toLocalDateTime(TimeZone.UTC)
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

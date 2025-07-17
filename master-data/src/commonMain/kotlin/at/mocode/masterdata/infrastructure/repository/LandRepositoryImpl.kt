package at.mocode.masterdata.infrastructure.repository

import at.mocode.masterdata.domain.model.LandDefinition
import at.mocode.masterdata.domain.repository.LandRepository
import com.benasher44.uuid.Uuid
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like

/**
 * PostgreSQL implementation of LandRepository using Exposed ORM.
 *
 * This implementation provides data access operations for country data,
 * mapping between the domain model (LandDefinition) and the database table (LandTable).
 */
class LandRepositoryImpl : LandRepository {

    override suspend fun findById(id: Uuid): LandDefinition? {
        return LandTable.select { LandTable.id eq id }
            .singleOrNull()
            ?.toLandDefinition()
    }

    override suspend fun findByIsoAlpha2Code(isoAlpha2Code: String): LandDefinition? {
        return LandTable.select { LandTable.isoAlpha2Code eq isoAlpha2Code }
            .singleOrNull()
            ?.toLandDefinition()
    }

    override suspend fun findByIsoAlpha3Code(isoAlpha3Code: String): LandDefinition? {
        return LandTable.select { LandTable.isoAlpha3Code eq isoAlpha3Code }
            .singleOrNull()
            ?.toLandDefinition()
    }

    override suspend fun findByName(searchTerm: String, limit: Int): List<LandDefinition> {
        val searchPattern = "%$searchTerm%"
        return LandTable.select {
            (LandTable.nameGerman like searchPattern) or
            (LandTable.nameEnglish like searchPattern) or
            (LandTable.nameLocal like searchPattern)
        }
        .orderBy(LandTable.sortierReihenfolge)
        .limit(limit)
        .map { it.toLandDefinition() }
    }

    override suspend fun findAllActive(orderBySortierung: Boolean): List<LandDefinition> {
        val query = LandTable.select { LandTable.isActive eq true }

        return if (orderBySortierung) {
            query.orderBy(LandTable.sortierReihenfolge, LandTable.nameGerman)
        } else {
            query.orderBy(LandTable.nameGerman)
        }.map { it.toLandDefinition() }
    }

    override suspend fun findEuMembers(): List<LandDefinition> {
        return LandTable.select {
            (LandTable.isActive eq true) and (LandTable.isEuMember eq true)
        }
        .orderBy(LandTable.sortierReihenfolge, LandTable.nameGerman)
        .map { it.toLandDefinition() }
    }

    override suspend fun findEwrMembers(): List<LandDefinition> {
        return LandTable.select {
            (LandTable.isActive eq true) and (LandTable.isEwrMember eq true)
        }
        .orderBy(LandTable.sortierReihenfolge, LandTable.nameGerman)
        .map { it.toLandDefinition() }
    }

    override suspend fun save(land: LandDefinition): LandDefinition {
        val now = Clock.System.now()

        // Check if record exists
        val existingRecord = LandTable.select { LandTable.id eq land.landId }.singleOrNull()

        return if (existingRecord != null) {
            // Update existing record
            LandTable.update({ LandTable.id eq land.landId }) {
                it[isoAlpha2Code] = land.isoAlpha2Code
                it[isoAlpha3Code] = land.isoAlpha3Code
                it[isoNumericCode] = land.isoNumerischerCode
                it[nameGerman] = land.nameDeutsch
                it[nameEnglish] = land.nameEnglisch
                it[nameLocal] = land.nameEnglisch // Using English as local fallback
                it[isActive] = land.istAktiv
                it[isEuMember] = land.istEuMitglied ?: false
                it[isEwrMember] = land.istEwrMitglied ?: false
                it[sortierReihenfolge] = land.sortierReihenfolge ?: 999
                it[flagIcon] = land.wappenUrl
                it[updatedAt] = now
                it[notes] = null // Could be extended later
            }
            land.copy(updatedAt = now)
        } else {
            // Insert new record
            LandTable.insert {
                it[id] = land.landId
                it[isoAlpha2Code] = land.isoAlpha2Code
                it[isoAlpha3Code] = land.isoAlpha3Code
                it[isoNumericCode] = land.isoNumerischerCode
                it[nameGerman] = land.nameDeutsch
                it[nameEnglish] = land.nameEnglisch
                it[nameLocal] = land.nameEnglisch // Using English as local fallback
                it[isActive] = land.istAktiv
                it[isEuMember] = land.istEuMitglied ?: false
                it[isEwrMember] = land.istEwrMitglied ?: false
                it[sortierReihenfolge] = land.sortierReihenfolge ?: 999
                it[flagIcon] = land.wappenUrl
                it[createdAt] = land.createdAt
                it[updatedAt] = now
                it[notes] = null
            }
            land.copy(updatedAt = now)
        }
    }

    override suspend fun delete(id: Uuid): Boolean {
        val deletedRows = LandTable.deleteWhere { LandTable.id eq id }
        return deletedRows > 0
    }

    override suspend fun existsByIsoAlpha2Code(isoAlpha2Code: String): Boolean {
        return LandTable.select { LandTable.isoAlpha2Code eq isoAlpha2Code }
            .count() > 0
    }

    override suspend fun existsByIsoAlpha3Code(isoAlpha3Code: String): Boolean {
        return LandTable.select { LandTable.isoAlpha3Code eq isoAlpha3Code }
            .count() > 0
    }

    override suspend fun countActive(): Long {
        return LandTable.select { LandTable.isActive eq true }.count()
    }

    /**
     * Extension function to convert a database ResultRow to a LandDefinition domain object.
     */
    private fun ResultRow.toLandDefinition(): LandDefinition {
        return LandDefinition(
            landId = this[LandTable.id].value,
            isoAlpha2Code = this[LandTable.isoAlpha2Code],
            isoAlpha3Code = this[LandTable.isoAlpha3Code],
            isoNumerischerCode = this[LandTable.isoNumericCode],
            nameDeutsch = this[LandTable.nameGerman],
            nameEnglisch = this[LandTable.nameEnglish],
            wappenUrl = this[LandTable.flagIcon],
            istEuMitglied = this[LandTable.isEuMember],
            istEwrMitglied = this[LandTable.isEwrMember],
            istAktiv = this[LandTable.isActive],
            sortierReihenfolge = this[LandTable.sortierReihenfolge],
            createdAt = this[LandTable.createdAt],
            updatedAt = this[LandTable.updatedAt]
        )
    }
}

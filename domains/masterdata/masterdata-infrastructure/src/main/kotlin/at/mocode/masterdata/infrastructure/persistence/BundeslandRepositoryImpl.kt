@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)
package at.mocode.masterdata.infrastructure.persistence

import at.mocode.masterdata.domain.model.BundeslandDefinition
import at.mocode.masterdata.domain.repository.BundeslandRepository
import at.mocode.core.utils.database.DatabaseFactory
import kotlin.uuid.Uuid
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

/**
 * Implementierung des BundeslandRepository für die Datenbankzugriffe.
 *
 * Diese Implementierung verwendet Exposed SQL für den Datenbankzugriff
 * und mappt zwischen der BundeslandDefinition Domain-Entität und der BundeslandTable.
 */
class BundeslandRepositoryImpl : BundeslandRepository {

    /**
     * Konvertiert eine Datenbankzeile in ein Domain-Objekt.
     */
    private fun rowToBundeslandDefinition(row: ResultRow): BundeslandDefinition {
        return BundeslandDefinition(
            bundeslandId = row[BundeslandTable.id],
            landId = row[BundeslandTable.landId],
            oepsCode = row[BundeslandTable.oepsCode],
            iso3166_2_Code = row[BundeslandTable.iso3166_2_Code],
            name = row[BundeslandTable.name],
            kuerzel = row[BundeslandTable.kuerzel],
            wappenUrl = row[BundeslandTable.wappenUrl],
            istAktiv = row[BundeslandTable.istAktiv],
            sortierReihenfolge = row[BundeslandTable.sortierReihenfolge],
            createdAt = row[BundeslandTable.createdAt].toInstant(TimeZone.UTC),
            updatedAt = row[BundeslandTable.updatedAt].toInstant(TimeZone.UTC)
        )
    }

    override suspend fun findById(id: Uuid): BundeslandDefinition? = DatabaseFactory.dbQuery {
        BundeslandTable.selectAll().where { BundeslandTable.id eq id }
            .map(::rowToBundeslandDefinition)
            .singleOrNull()
    }

    override suspend fun findByOepsCode(oepsCode: String, landId: Uuid): BundeslandDefinition? = DatabaseFactory.dbQuery {
        BundeslandTable.selectAll().where {
            (BundeslandTable.oepsCode eq oepsCode) and (BundeslandTable.landId eq landId)
        }
            .map(::rowToBundeslandDefinition)
            .singleOrNull()
    }

    override suspend fun findByIso3166_2_Code(iso3166_2_Code: String): BundeslandDefinition? = DatabaseFactory.dbQuery {
        BundeslandTable.selectAll().where { BundeslandTable.iso3166_2_Code eq iso3166_2_Code }
            .map(::rowToBundeslandDefinition)
            .singleOrNull()
    }

    override suspend fun findByCountry(landId: Uuid, activeOnly: Boolean, orderBySortierung: Boolean): List<BundeslandDefinition> = DatabaseFactory.dbQuery {
        val query = BundeslandTable.selectAll().where { BundeslandTable.landId eq landId }

        if (activeOnly) {
            query.andWhere { BundeslandTable.istAktiv eq true }
        }

        if (orderBySortierung) {
            query.orderBy(BundeslandTable.sortierReihenfolge to SortOrder.ASC, BundeslandTable.name to SortOrder.ASC)
        } else {
            query.orderBy(BundeslandTable.name to SortOrder.ASC)
        }

        query.map(::rowToBundeslandDefinition)
    }

    override suspend fun findByName(searchTerm: String, landId: Uuid?, limit: Int): List<BundeslandDefinition> = DatabaseFactory.dbQuery {
        val pattern = "%$searchTerm%"
        val query = BundeslandTable.selectAll().where { BundeslandTable.name like pattern }

        landId?.let {
            query.andWhere { BundeslandTable.landId eq it }
        }

        query.limit(limit).map(::rowToBundeslandDefinition)
    }

    override suspend fun findAllActive(orderBySortierung: Boolean): List<BundeslandDefinition> = DatabaseFactory.dbQuery {
        val query = BundeslandTable.selectAll().where { BundeslandTable.istAktiv eq true }

        if (orderBySortierung) {
            query.orderBy(BundeslandTable.sortierReihenfolge to SortOrder.ASC, BundeslandTable.name to SortOrder.ASC)
        } else {
            query.orderBy(BundeslandTable.name to SortOrder.ASC)
        }

        query.map(::rowToBundeslandDefinition)
    }

    override suspend fun save(bundesland: BundeslandDefinition): BundeslandDefinition = DatabaseFactory.dbQuery {
        val now = Clock.System.now()
        val existingBundesland = BundeslandTable.selectAll().where { BundeslandTable.id eq bundesland.bundeslandId }.singleOrNull()

        if (existingBundesland == null) {
            // Insert a new federal state
            BundeslandTable.insert { stmt ->
                stmt[id] = bundesland.bundeslandId
                stmt[landId] = bundesland.landId
                stmt[oepsCode] = bundesland.oepsCode
                stmt[iso3166_2_Code] = bundesland.iso3166_2_Code
                stmt[name] = bundesland.name
                stmt[kuerzel] = bundesland.kuerzel
                stmt[wappenUrl] = bundesland.wappenUrl
                stmt[istAktiv] = bundesland.istAktiv
                stmt[sortierReihenfolge] = bundesland.sortierReihenfolge
                stmt[createdAt] = bundesland.createdAt.toLocalDateTime(TimeZone.UTC)
                stmt[updatedAt] = now.toLocalDateTime(TimeZone.UTC)
            }
        } else {
            // Update existing federal state
            BundeslandTable.update({ BundeslandTable.id eq bundesland.bundeslandId }) { stmt ->
                stmt[landId] = bundesland.landId
                stmt[oepsCode] = bundesland.oepsCode
                stmt[iso3166_2_Code] = bundesland.iso3166_2_Code
                stmt[name] = bundesland.name
                stmt[kuerzel] = bundesland.kuerzel
                stmt[wappenUrl] = bundesland.wappenUrl
                stmt[istAktiv] = bundesland.istAktiv
                stmt[sortierReihenfolge] = bundesland.sortierReihenfolge
                stmt[updatedAt] = now.toLocalDateTime(TimeZone.UTC)
            }
        }

        bundesland.copy(updatedAt = now)
    }

    override suspend fun delete(id: Uuid): Boolean = DatabaseFactory.dbQuery {
        BundeslandTable.deleteWhere { BundeslandTable.id eq id } > 0
    }

    override suspend fun existsByOepsCode(oepsCode: String, landId: Uuid): Boolean = DatabaseFactory.dbQuery {
        BundeslandTable.selectAll().where {
            (BundeslandTable.oepsCode eq oepsCode) and (BundeslandTable.landId eq landId)
        }.count() > 0
    }

    override suspend fun existsByIso3166_2_Code(iso3166_2_Code: String): Boolean = DatabaseFactory.dbQuery {
        BundeslandTable.selectAll().where { BundeslandTable.iso3166_2_Code eq iso3166_2_Code }
            .count() > 0
    }

    override suspend fun countActiveByCountry(landId: Uuid): Long = DatabaseFactory.dbQuery {
        BundeslandTable.selectAll().where {
            (BundeslandTable.landId eq landId) and (BundeslandTable.istAktiv eq true)
        }.count()
    }
}

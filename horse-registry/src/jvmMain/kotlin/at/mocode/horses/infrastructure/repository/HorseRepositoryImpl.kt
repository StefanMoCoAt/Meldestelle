package at.mocode.horses.infrastructure.repository

import at.mocode.enums.PferdeGeschlechtE
import at.mocode.horses.domain.model.DomPferd
import at.mocode.horses.domain.repository.HorseRepository
import at.mocode.horses.infrastructure.repository.HorseTable
import at.mocode.shared.database.DatabaseFactory
import com.benasher44.uuid.Uuid
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.statements.UpdateBuilder

/**
 * PostgreSQL implementation of the HorseRepository using Exposed ORM.
 *
 * This implementation provides database operations for horse entities,
 * mapping between the domain model (DomPferd) and the database table (HorseTable).
 */
class HorseRepositoryImpl : HorseRepository {

    override suspend fun findById(id: Uuid): DomPferd? = DatabaseFactory.dbQuery {
        HorseTable.selectAll().where { HorseTable.id eq id }
            .map { rowToDomPferd(it) }
            .singleOrNull()
    }

    override suspend fun findByLebensnummer(lebensnummer: String): DomPferd? = DatabaseFactory.dbQuery {
        HorseTable.selectAll().where { HorseTable.lebensnummer eq lebensnummer }
            .map { rowToDomPferd(it) }
            .singleOrNull()
    }

    override suspend fun findByChipNummer(chipNummer: String): DomPferd? = DatabaseFactory.dbQuery {
        HorseTable.selectAll().where { HorseTable.chipNummer eq chipNummer }
            .map { rowToDomPferd(it) }
            .singleOrNull()
    }

    override suspend fun findByPassNummer(passNummer: String): DomPferd? = DatabaseFactory.dbQuery {
        HorseTable.selectAll().where { HorseTable.passNummer eq passNummer }
            .map { rowToDomPferd(it) }
            .singleOrNull()
    }

    override suspend fun findByOepsNummer(oepsNummer: String): DomPferd? = DatabaseFactory.dbQuery {
        HorseTable.selectAll().where { HorseTable.oepsNummer eq oepsNummer }
            .map { rowToDomPferd(it) }
            .singleOrNull()
    }

    override suspend fun findByFeiNummer(feiNummer: String): DomPferd? = DatabaseFactory.dbQuery {
        HorseTable.selectAll().where { HorseTable.feiNummer eq feiNummer }
            .map { rowToDomPferd(it) }
            .singleOrNull()
    }

    override suspend fun findByName(searchTerm: String, limit: Int): List<DomPferd> = DatabaseFactory.dbQuery {
        HorseTable.selectAll().where { HorseTable.pferdeName like "%$searchTerm%" }
            .orderBy(HorseTable.pferdeName to SortOrder.ASC)
            .limit(limit)
            .map { rowToDomPferd(it) }
    }

    override suspend fun findByOwnerId(ownerId: Uuid, activeOnly: Boolean): List<DomPferd> = DatabaseFactory.dbQuery {
        val query = HorseTable.selectAll().where { HorseTable.besitzerId eq ownerId }

        if (activeOnly) {
            query.andWhere { HorseTable.istAktiv eq true }
        } else {
            query
        }.orderBy(HorseTable.pferdeName to SortOrder.ASC)
         .map { rowToDomPferd(it) }
    }

    override suspend fun findByResponsiblePersonId(responsiblePersonId: Uuid, activeOnly: Boolean): List<DomPferd> = DatabaseFactory.dbQuery {
        val query = HorseTable.selectAll().where { HorseTable.verantwortlichePersonId eq responsiblePersonId }

        if (activeOnly) {
            query.andWhere { HorseTable.istAktiv eq true }
        } else {
            query
        }.orderBy(HorseTable.pferdeName to SortOrder.ASC)
         .map { rowToDomPferd(it) }
    }

    override suspend fun findByGeschlecht(geschlecht: PferdeGeschlechtE, activeOnly: Boolean, limit: Int): List<DomPferd> = DatabaseFactory.dbQuery {
        val query = HorseTable.selectAll().where { HorseTable.geschlecht eq geschlecht }

        if (activeOnly) {
            query.andWhere { HorseTable.istAktiv eq true }
        } else {
            query
        }.orderBy(HorseTable.pferdeName to SortOrder.ASC)
         .limit(limit)
         .map { rowToDomPferd(it) }
    }

    override suspend fun findByRasse(rasse: String, activeOnly: Boolean, limit: Int): List<DomPferd> = DatabaseFactory.dbQuery {
        val query = HorseTable.selectAll().where { HorseTable.rasse eq rasse }

        if (activeOnly) {
            query.andWhere { HorseTable.istAktiv eq true }
        } else {
            query
        }.orderBy(HorseTable.pferdeName to SortOrder.ASC)
         .limit(limit)
         .map { rowToDomPferd(it) }
    }

    override suspend fun findByBirthYear(birthYear: Int, activeOnly: Boolean): List<DomPferd> = DatabaseFactory.dbQuery {
        val query = HorseTable.selectAll().where {
            HorseTable.geburtsdatum.isNotNull() and
                (CustomFunction(
                    "EXTRACT",
                    IntegerColumnType(),
                    stringLiteral("YEAR FROM "),
                    HorseTable.geburtsdatum
                ) eq birthYear)
        }

        if (activeOnly) {
            query.andWhere { HorseTable.istAktiv eq true }
        } else {
            query
        }.orderBy(HorseTable.pferdeName to SortOrder.ASC)
         .map { rowToDomPferd(it) }
    }

    override suspend fun findByBirthYearRange(fromYear: Int, toYear: Int, activeOnly: Boolean): List<DomPferd> = DatabaseFactory.dbQuery {
        val query = HorseTable.selectAll().where {
            HorseTable.geburtsdatum.isNotNull() and
                (CustomFunction(
                    "EXTRACT",
                    IntegerColumnType(),
                    stringLiteral("YEAR FROM "),
                    HorseTable.geburtsdatum
                ) greaterEq fromYear) and
                (CustomFunction(
                    "EXTRACT",
                    IntegerColumnType(),
                    stringLiteral("YEAR FROM "),
                    HorseTable.geburtsdatum
                ) lessEq toYear)
        }

        if (activeOnly) {
            query.andWhere { HorseTable.istAktiv eq true }
        } else {
            query
        }.orderBy(HorseTable.geburtsdatum, SortOrder.DESC)
         .map { rowToDomPferd(it) }
    }

    override suspend fun findAllActive(limit: Int): List<DomPferd> = DatabaseFactory.dbQuery {
        HorseTable.selectAll().where { HorseTable.istAktiv eq true }
            .orderBy(HorseTable.pferdeName to SortOrder.ASC)
            .limit(limit)
            .map { rowToDomPferd(it) }
    }

    override suspend fun findOepsRegistered(activeOnly: Boolean): List<DomPferd> = DatabaseFactory.dbQuery {
        val query = HorseTable.selectAll().where { HorseTable.oepsNummer.isNotNull() }

        if (activeOnly) {
            query.andWhere { HorseTable.istAktiv eq true }
        } else {
            query
        }.orderBy(HorseTable.pferdeName to SortOrder.ASC)
         .map { rowToDomPferd(it) }
    }

    override suspend fun findFeiRegistered(activeOnly: Boolean): List<DomPferd> = DatabaseFactory.dbQuery {
        val query = HorseTable.selectAll().where { HorseTable.feiNummer.isNotNull() }

        if (activeOnly) {
            query.andWhere { HorseTable.istAktiv eq true }
        } else {
            query
        }.orderBy(HorseTable.pferdeName to SortOrder.ASC)
         .map { rowToDomPferd(it) }
    }

    override suspend fun save(horse: DomPferd): DomPferd = DatabaseFactory.dbQuery {
        val now = Clock.System.now()
        val existingHorse = findById(horse.pferdId)

        if (existingHorse != null) {
            // Update existing horse
            val updatedHorse = horse.copy(updatedAt = now)
            HorseTable.update({ HorseTable.id eq horse.pferdId }) {
                domPferdToStatement(it, updatedHorse)
            }
            updatedHorse
        } else {
            // Insert a new horse
            HorseTable.insert {
                it[id] = horse.pferdId
                domPferdToStatement(it, horse.copy(updatedAt = now))
            }
            horse.copy(updatedAt = now)
        }
    }

    override suspend fun delete(id: Uuid): Boolean = DatabaseFactory.dbQuery {
        val deletedRows = HorseTable.deleteWhere { HorseTable.id eq id }
        deletedRows > 0
    }

    override suspend fun existsByLebensnummer(lebensnummer: String): Boolean = DatabaseFactory.dbQuery {
        HorseTable.selectAll().where { HorseTable.lebensnummer eq lebensnummer }
            .count() > 0
    }

    override suspend fun existsByChipNummer(chipNummer: String): Boolean = DatabaseFactory.dbQuery {
        HorseTable.selectAll().where { HorseTable.chipNummer eq chipNummer }
            .count() > 0
    }

    override suspend fun existsByPassNummer(passNummer: String): Boolean = DatabaseFactory.dbQuery {
        HorseTable.selectAll().where { HorseTable.passNummer eq passNummer }
            .count() > 0
    }

    override suspend fun existsByOepsNummer(oepsNummer: String): Boolean = DatabaseFactory.dbQuery {
        HorseTable.selectAll().where { HorseTable.oepsNummer eq oepsNummer }
            .count() > 0
    }

    override suspend fun existsByFeiNummer(feiNummer: String): Boolean = DatabaseFactory.dbQuery {
        HorseTable.selectAll().where { HorseTable.feiNummer eq feiNummer }
            .count() > 0
    }

    override suspend fun countActive(): Long = DatabaseFactory.dbQuery {
        HorseTable.selectAll().where { HorseTable.istAktiv eq true }
            .count()
    }

    override suspend fun countByOwnerId(ownerId: Uuid, activeOnly: Boolean): Long = DatabaseFactory.dbQuery {
        val query = HorseTable.selectAll().where { HorseTable.besitzerId eq ownerId }

        if (activeOnly) {
            query.andWhere { HorseTable.istAktiv eq true }
        } else {
            query
        }.count()
    }

    /**
     * Maps a database row to a DomPferd domain object.
     */
    private fun rowToDomPferd(row: ResultRow): DomPferd {
        return DomPferd(
            pferdId = row[HorseTable.id].value,
            pferdeName = row[HorseTable.pferdeName],
            geschlecht = row[HorseTable.geschlecht],
            geburtsdatum = row[HorseTable.geburtsdatum],
            rasse = row[HorseTable.rasse],
            farbe = row[HorseTable.farbe],
            besitzerId = row[HorseTable.besitzerId],
            verantwortlichePersonId = row[HorseTable.verantwortlichePersonId],
            zuechterName = row[HorseTable.zuechterName],
            zuchtbuchNummer = row[HorseTable.zuchtbuchNummer],
            lebensnummer = row[HorseTable.lebensnummer],
            chipNummer = row[HorseTable.chipNummer],
            passNummer = row[HorseTable.passNummer],
            oepsNummer = row[HorseTable.oepsNummer],
            feiNummer = row[HorseTable.feiNummer],
            vaterName = row[HorseTable.vaterName],
            mutterName = row[HorseTable.mutterName],
            mutterVaterName = row[HorseTable.mutterVaterName],
            stockmass = row[HorseTable.stockmass],
            istAktiv = row[HorseTable.istAktiv],
            bemerkungen = row[HorseTable.bemerkungen],
            datenQuelle = row[HorseTable.datenQuelle],
            createdAt = row[HorseTable.createdAt],
            updatedAt = row[HorseTable.updatedAt]
        )
    }

    /**
     * Maps a DomPferd domain object to database statement values.
     */
    private fun domPferdToStatement(statement: UpdateBuilder<*>, horse: DomPferd) {
        statement[HorseTable.pferdeName] = horse.pferdeName
        statement[HorseTable.geschlecht] = horse.geschlecht
        statement[HorseTable.geburtsdatum] = horse.geburtsdatum
        statement[HorseTable.rasse] = horse.rasse
        statement[HorseTable.farbe] = horse.farbe
        statement[HorseTable.besitzerId] = horse.besitzerId
        statement[HorseTable.verantwortlichePersonId] = horse.verantwortlichePersonId
        statement[HorseTable.zuechterName] = horse.zuechterName
        statement[HorseTable.zuchtbuchNummer] = horse.zuchtbuchNummer
        statement[HorseTable.lebensnummer] = horse.lebensnummer
        statement[HorseTable.chipNummer] = horse.chipNummer
        statement[HorseTable.passNummer] = horse.passNummer
        statement[HorseTable.oepsNummer] = horse.oepsNummer
        statement[HorseTable.feiNummer] = horse.feiNummer
        statement[HorseTable.vaterName] = horse.vaterName
        statement[HorseTable.mutterName] = horse.mutterName
        statement[HorseTable.mutterVaterName] = horse.mutterVaterName
        statement[HorseTable.stockmass] = horse.stockmass
        statement[HorseTable.istAktiv] = horse.istAktiv
        statement[HorseTable.bemerkungen] = horse.bemerkungen
        statement[HorseTable.datenQuelle] = horse.datenQuelle
        statement[HorseTable.createdAt] = horse.createdAt
        statement[HorseTable.updatedAt] = horse.updatedAt
    }
}

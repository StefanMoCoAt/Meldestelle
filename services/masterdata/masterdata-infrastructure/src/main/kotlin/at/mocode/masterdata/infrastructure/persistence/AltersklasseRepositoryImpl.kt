@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)
package at.mocode.masterdata.infrastructure.persistence

import at.mocode.core.domain.model.SparteE
import at.mocode.masterdata.domain.model.AltersklasseDefinition
import at.mocode.masterdata.domain.repository.AltersklasseRepository
import at.mocode.core.utils.database.DatabaseFactory
import kotlin.uuid.Uuid
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

/**
 * Implementierung des AltersklasseRepository für die Datenbankzugriffe.
 *
 * Diese Implementierung verwendet Exposed SQL für den Datenbankzugriff
 * und mappt zwischen der AltersklasseDefinition Domain-Entität und der AltersklasseTable.
 */
class AltersklasseRepositoryImpl : AltersklasseRepository {

    /**
     * Konvertiert eine Datenbankzeile in ein Domain-Objekt.
     */
    private fun rowToAltersklasseDefinition(row: ResultRow): AltersklasseDefinition {
        return AltersklasseDefinition(
            altersklasseId = row[AltersklasseTable.id],
            altersklasseCode = row[AltersklasseTable.altersklasseCode],
            bezeichnung = row[AltersklasseTable.bezeichnung],
            minAlter = row[AltersklasseTable.minAlter],
            maxAlter = row[AltersklasseTable.maxAlter],
            stichtagRegelText = row[AltersklasseTable.stichtagRegelText],
            sparteFilter = row[AltersklasseTable.sparteFilter]?.let { SparteE.valueOf(it) },
            geschlechtFilter = row[AltersklasseTable.geschlechtFilter],
            oetoRegelReferenzId = row[AltersklasseTable.oetoRegelReferenzId],
            istAktiv = row[AltersklasseTable.istAktiv],
            createdAt = row[AltersklasseTable.createdAt].toInstant(TimeZone.UTC),
            updatedAt = row[AltersklasseTable.updatedAt].toInstant(TimeZone.UTC)
        )
    }

    override suspend fun findById(id: Uuid): AltersklasseDefinition? = DatabaseFactory.dbQuery {
        AltersklasseTable.selectAll().where { AltersklasseTable.id eq id }
            .map(::rowToAltersklasseDefinition)
            .singleOrNull()
    }

    override suspend fun findByCode(altersklasseCode: String): AltersklasseDefinition? = DatabaseFactory.dbQuery {
        AltersklasseTable.selectAll().where { AltersklasseTable.altersklasseCode eq altersklasseCode }
            .map(::rowToAltersklasseDefinition)
            .singleOrNull()
    }

    override suspend fun findByName(searchTerm: String, limit: Int): List<AltersklasseDefinition> = DatabaseFactory.dbQuery {
        val pattern = "%$searchTerm%"
        AltersklasseTable.selectAll().where { AltersklasseTable.bezeichnung like pattern }
            .limit(limit)
            .map(::rowToAltersklasseDefinition)
    }

    override suspend fun findAllActive(sparteFilter: SparteE?, geschlechtFilter: Char?): List<AltersklasseDefinition> = DatabaseFactory.dbQuery {
        val query = AltersklasseTable.selectAll().where { AltersklasseTable.istAktiv eq true }

        sparteFilter?.let { sparte ->
            query.andWhere {
                (AltersklasseTable.sparteFilter eq sparte.name) or (AltersklasseTable.sparteFilter.isNull())
            }
        }

        geschlechtFilter?.let { geschlecht ->
            query.andWhere {
                (AltersklasseTable.geschlechtFilter eq geschlecht) or (AltersklasseTable.geschlechtFilter.isNull())
            }
        }

        query.orderBy(AltersklasseTable.bezeichnung to SortOrder.ASC)
            .map(::rowToAltersklasseDefinition)
    }

    override suspend fun findApplicableForAge(age: Int, sparteFilter: SparteE?, geschlechtFilter: Char?): List<AltersklasseDefinition> = DatabaseFactory.dbQuery {
        val query = AltersklasseTable.selectAll().where { AltersklasseTable.istAktiv eq true }

        // Age range filter
        query.andWhere {
            (AltersklasseTable.minAlter.isNull() or (AltersklasseTable.minAlter lessEq age)) and
            (AltersklasseTable.maxAlter.isNull() or (AltersklasseTable.maxAlter greaterEq age))
        }

        sparteFilter?.let { sparte ->
            query.andWhere {
                (AltersklasseTable.sparteFilter eq sparte.name) or (AltersklasseTable.sparteFilter.isNull())
            }
        }

        geschlechtFilter?.let { geschlecht ->
            query.andWhere {
                (AltersklasseTable.geschlechtFilter eq geschlecht) or (AltersklasseTable.geschlechtFilter.isNull())
            }
        }

        query.orderBy(AltersklasseTable.bezeichnung to SortOrder.ASC)
            .map(::rowToAltersklasseDefinition)
    }

    override suspend fun findBySparte(sparte: SparteE, activeOnly: Boolean): List<AltersklasseDefinition> = DatabaseFactory.dbQuery {
        val query = AltersklasseTable.selectAll().where {
            (AltersklasseTable.sparteFilter eq sparte.name) or (AltersklasseTable.sparteFilter.isNull())
        }

        if (activeOnly) {
            query.andWhere { AltersklasseTable.istAktiv eq true }
        }

        query.orderBy(AltersklasseTable.bezeichnung to SortOrder.ASC)
            .map(::rowToAltersklasseDefinition)
    }

    override suspend fun findByGeschlecht(geschlecht: Char, activeOnly: Boolean): List<AltersklasseDefinition> = DatabaseFactory.dbQuery {
        val query = AltersklasseTable.selectAll().where {
            (AltersklasseTable.geschlechtFilter eq geschlecht) or (AltersklasseTable.geschlechtFilter.isNull())
        }

        if (activeOnly) {
            query.andWhere { AltersklasseTable.istAktiv eq true }
        }

        query.orderBy(AltersklasseTable.bezeichnung to SortOrder.ASC)
            .map(::rowToAltersklasseDefinition)
    }

    override suspend fun findByAgeRange(minAge: Int?, maxAge: Int?, activeOnly: Boolean): List<AltersklasseDefinition> = DatabaseFactory.dbQuery {
        val query = AltersklasseTable.selectAll()

        minAge?.let { min ->
            query.andWhere {
                (AltersklasseTable.maxAlter.isNull()) or (AltersklasseTable.maxAlter greaterEq min)
            }
        }

        maxAge?.let { max ->
            query.andWhere {
                (AltersklasseTable.minAlter.isNull()) or (AltersklasseTable.minAlter lessEq max)
            }
        }

        if (activeOnly) {
            query.andWhere { AltersklasseTable.istAktiv eq true }
        }

        query.orderBy(AltersklasseTable.bezeichnung to SortOrder.ASC)
            .map(::rowToAltersklasseDefinition)
    }

    override suspend fun findByOetoRegelReferenz(oetoRegelReferenzId: Uuid): List<AltersklasseDefinition> = DatabaseFactory.dbQuery {
        AltersklasseTable.selectAll().where { AltersklasseTable.oetoRegelReferenzId eq oetoRegelReferenzId }
            .orderBy(AltersklasseTable.bezeichnung to SortOrder.ASC)
            .map(::rowToAltersklasseDefinition)
    }

    override suspend fun save(altersklasse: AltersklasseDefinition): AltersklasseDefinition = DatabaseFactory.dbQuery {
        val now = Clock.System.now()
        val existingAltersklasse = AltersklasseTable.selectAll().where { AltersklasseTable.id eq altersklasse.altersklasseId }.singleOrNull()

        if (existingAltersklasse == null) {
            // Insert a new age class
            AltersklasseTable.insert { stmt ->
                stmt[id] = altersklasse.altersklasseId
                stmt[altersklasseCode] = altersklasse.altersklasseCode
                stmt[bezeichnung] = altersklasse.bezeichnung
                stmt[minAlter] = altersklasse.minAlter
                stmt[maxAlter] = altersklasse.maxAlter
                stmt[stichtagRegelText] = altersklasse.stichtagRegelText
                stmt[sparteFilter] = altersklasse.sparteFilter?.name
                stmt[geschlechtFilter] = altersklasse.geschlechtFilter
                stmt[oetoRegelReferenzId] = altersklasse.oetoRegelReferenzId
                stmt[istAktiv] = altersklasse.istAktiv
                stmt[createdAt] = altersklasse.createdAt.toLocalDateTime(TimeZone.UTC)
                stmt[updatedAt] = now.toLocalDateTime(TimeZone.UTC)
            }
        } else {
            // Update existing age class
            AltersklasseTable.update({ AltersklasseTable.id eq altersklasse.altersklasseId }) { stmt ->
                stmt[altersklasseCode] = altersklasse.altersklasseCode
                stmt[bezeichnung] = altersklasse.bezeichnung
                stmt[minAlter] = altersklasse.minAlter
                stmt[maxAlter] = altersklasse.maxAlter
                stmt[stichtagRegelText] = altersklasse.stichtagRegelText
                stmt[sparteFilter] = altersklasse.sparteFilter?.name
                stmt[geschlechtFilter] = altersklasse.geschlechtFilter
                stmt[oetoRegelReferenzId] = altersklasse.oetoRegelReferenzId
                stmt[istAktiv] = altersklasse.istAktiv
                stmt[updatedAt] = now.toLocalDateTime(TimeZone.UTC)
            }
        }

        altersklasse.copy(updatedAt = now)
    }

    override suspend fun delete(id: Uuid): Boolean = DatabaseFactory.dbQuery {
        AltersklasseTable.deleteWhere { AltersklasseTable.id eq id } > 0
    }

    override suspend fun existsByCode(altersklasseCode: String): Boolean = DatabaseFactory.dbQuery {
        AltersklasseTable.selectAll().where { AltersklasseTable.altersklasseCode eq altersklasseCode }
            .count() > 0
    }

    override suspend fun countActive(sparteFilter: SparteE?): Long = DatabaseFactory.dbQuery {
        val query = AltersklasseTable.selectAll().where { AltersklasseTable.istAktiv eq true }

        sparteFilter?.let { sparte ->
            query.andWhere {
                (AltersklasseTable.sparteFilter eq sparte.name) or (AltersklasseTable.sparteFilter.isNull())
            }
        }

        query.count()
    }

    override suspend fun isEligible(altersklasseId: Uuid, age: Int, geschlecht: Char): Boolean = DatabaseFactory.dbQuery {
        val altersklasse = AltersklasseTable.selectAll().where {
            (AltersklasseTable.id eq altersklasseId) and (AltersklasseTable.istAktiv eq true)
        }.singleOrNull()

        if (altersklasse == null) return@dbQuery false

        // Check age eligibility
        val minAlter = altersklasse[AltersklasseTable.minAlter]
        val maxAlter = altersklasse[AltersklasseTable.maxAlter]
        val ageEligible = (minAlter == null || age >= minAlter) && (maxAlter == null || age <= maxAlter)

        // Check gender eligibility
        val geschlechtFilter = altersklasse[AltersklasseTable.geschlechtFilter]
        val genderEligible = geschlechtFilter == null || geschlechtFilter == geschlecht

        ageEligible && genderEligible
    }
}

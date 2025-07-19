package at.mocode.members.infrastructure.repository

import at.mocode.members.domain.model.DomPersonRolle
import at.mocode.members.domain.repository.PersonRolleRepository
import at.mocode.members.infrastructure.table.PersonRolleTable
import at.mocode.shared.database.DatabaseFactory
import com.benasher44.uuid.Uuid
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

/**
 * Database implementation of PersonRolleRepository using PersonRolleTable.
 */
class PersonRolleRepositoryImpl : PersonRolleRepository {

    /**
     * Konvertiert eine Datenbankzeile in ein Domain-Objekt.
     */
    private fun rowToDomPersonRolle(row: ResultRow): DomPersonRolle {
        return DomPersonRolle(
            personRolleId = row[PersonRolleTable.id],
            personId = row[PersonRolleTable.personId],
            rolleId = row[PersonRolleTable.rolleId],
            vereinId = row[PersonRolleTable.vereinId],
            gueltigVon = row[PersonRolleTable.gueltigVon],
            gueltigBis = row[PersonRolleTable.gueltigBis],
            istAktiv = row[PersonRolleTable.istAktiv],
            zugewiesenVon = row[PersonRolleTable.zugewiesenVon],
            notizen = row[PersonRolleTable.notizen],
            createdAt = row[PersonRolleTable.createdAt],
            updatedAt = row[PersonRolleTable.updatedAt]
        )
    }

    override suspend fun save(personRolle: DomPersonRolle): DomPersonRolle = DatabaseFactory.dbQuery {
        val now = Clock.System.now()
        val existingPersonRolle = findById(personRolle.personRolleId)

        if (existingPersonRolle == null) {
            // Insert new person role
            PersonRolleTable.insert { stmt ->
                stmt[PersonRolleTable.id] = personRolle.personRolleId
                stmt[PersonRolleTable.personId] = personRolle.personId
                stmt[PersonRolleTable.rolleId] = personRolle.rolleId
                stmt[PersonRolleTable.vereinId] = personRolle.vereinId
                stmt[PersonRolleTable.gueltigVon] = personRolle.gueltigVon
                stmt[PersonRolleTable.gueltigBis] = personRolle.gueltigBis
                stmt[PersonRolleTable.istAktiv] = personRolle.istAktiv
                stmt[PersonRolleTable.zugewiesenVon] = personRolle.zugewiesenVon
                stmt[PersonRolleTable.notizen] = personRolle.notizen
                stmt[PersonRolleTable.createdAt] = personRolle.createdAt
                stmt[PersonRolleTable.updatedAt] = now
            }
        } else {
            // Update existing person role
            PersonRolleTable.update({ PersonRolleTable.id eq personRolle.personRolleId }) { stmt ->
                stmt[PersonRolleTable.personId] = personRolle.personId
                stmt[PersonRolleTable.rolleId] = personRolle.rolleId
                stmt[PersonRolleTable.vereinId] = personRolle.vereinId
                stmt[PersonRolleTable.gueltigVon] = personRolle.gueltigVon
                stmt[PersonRolleTable.gueltigBis] = personRolle.gueltigBis
                stmt[PersonRolleTable.istAktiv] = personRolle.istAktiv
                stmt[PersonRolleTable.zugewiesenVon] = personRolle.zugewiesenVon
                stmt[PersonRolleTable.notizen] = personRolle.notizen
                stmt[PersonRolleTable.updatedAt] = now
            }
        }

        personRolle.copy(updatedAt = now)
    }

    override suspend fun findById(personRolleId: Uuid): DomPersonRolle? = DatabaseFactory.dbQuery {
        PersonRolleTable.selectAll().where { PersonRolleTable.id eq personRolleId }
            .map(::rowToDomPersonRolle)
            .singleOrNull()
    }

    override suspend fun findByPersonId(personId: Uuid, nurAktive: Boolean): List<DomPersonRolle> = DatabaseFactory.dbQuery {
        val query = if (nurAktive) {
            PersonRolleTable.selectAll()
                .where { (PersonRolleTable.personId eq personId) and (PersonRolleTable.istAktiv eq true) }
        } else {
            PersonRolleTable.selectAll().where { PersonRolleTable.personId eq personId }
        }
        query.map(::rowToDomPersonRolle)
    }

    override suspend fun findByRolleId(rolleId: Uuid, nurAktive: Boolean): List<DomPersonRolle> = DatabaseFactory.dbQuery {
        val query = if (nurAktive) {
            PersonRolleTable.selectAll()
                .where { (PersonRolleTable.rolleId eq rolleId) and (PersonRolleTable.istAktiv eq true) }
        } else {
            PersonRolleTable.selectAll().where { PersonRolleTable.rolleId eq rolleId }
        }
        query.map(::rowToDomPersonRolle)
    }

    override suspend fun findByVereinId(vereinId: Uuid, nurAktive: Boolean): List<DomPersonRolle> = DatabaseFactory.dbQuery {
        val query = if (nurAktive) {
            PersonRolleTable.selectAll()
                .where { (PersonRolleTable.vereinId eq vereinId) and (PersonRolleTable.istAktiv eq true) }
        } else {
            PersonRolleTable.selectAll().where { PersonRolleTable.vereinId eq vereinId }
        }
        query.map(::rowToDomPersonRolle)
    }

    override suspend fun findByPersonAndRolle(personId: Uuid, rolleId: Uuid, vereinId: Uuid?): DomPersonRolle? = DatabaseFactory.dbQuery {
        val query = if (vereinId != null) {
            PersonRolleTable.selectAll().where {
                (PersonRolleTable.personId eq personId) and
                    (PersonRolleTable.rolleId eq rolleId) and
                    (PersonRolleTable.vereinId eq vereinId)
            }
        } else {
            PersonRolleTable.selectAll().where {
                (PersonRolleTable.personId eq personId) and
                    (PersonRolleTable.rolleId eq rolleId) and
                    PersonRolleTable.vereinId.isNull()
            }
        }
        query.map(::rowToDomPersonRolle).singleOrNull()
    }

    override suspend fun findValidAt(stichtag: LocalDate, nurAktive: Boolean): List<DomPersonRolle> = DatabaseFactory.dbQuery {
        val baseQuery = PersonRolleTable.selectAll().where {
            (PersonRolleTable.gueltigVon lessEq stichtag) and
                (PersonRolleTable.gueltigBis.isNull() or (PersonRolleTable.gueltigBis greaterEq stichtag))
        }

        val query = if (nurAktive) {
            baseQuery.andWhere { PersonRolleTable.istAktiv eq true }
        } else {
            baseQuery
        }

        query.map(::rowToDomPersonRolle)
    }

    override suspend fun findByPersonValidAt(personId: Uuid, stichtag: LocalDate, nurAktive: Boolean): List<DomPersonRolle> = DatabaseFactory.dbQuery {
        val baseQuery = PersonRolleTable.selectAll().where {
            (PersonRolleTable.personId eq personId) and
                (PersonRolleTable.gueltigVon lessEq stichtag) and
                (PersonRolleTable.gueltigBis.isNull() or (PersonRolleTable.gueltigBis greaterEq stichtag))
        }

        val query = if (nurAktive) {
            baseQuery.andWhere { PersonRolleTable.istAktiv eq true }
        } else {
            baseQuery
        }

        query.map(::rowToDomPersonRolle)
    }

    override suspend fun deactivatePersonRolle(personRolleId: Uuid): Boolean = DatabaseFactory.dbQuery {
        val now = Clock.System.now()
        val rowsUpdated = PersonRolleTable.update({ PersonRolleTable.id eq personRolleId }) { stmt ->
            stmt[PersonRolleTable.istAktiv] = false
            stmt[PersonRolleTable.updatedAt] = now
        }
        rowsUpdated > 0
    }

    override suspend fun deletePersonRolle(personRolleId: Uuid): Boolean = DatabaseFactory.dbQuery {
        val rowsDeleted = PersonRolleTable.deleteWhere { PersonRolleTable.id eq personRolleId }
        rowsDeleted > 0
    }

    override suspend fun hasPersonRolle(personId: Uuid, rolleId: Uuid, vereinId: Uuid?, stichtag: LocalDate?): Boolean = DatabaseFactory.dbQuery {
        val checkDate = stichtag ?: Clock.System.todayIn(TimeZone.currentSystemDefault())

        val baseQuery = PersonRolleTable.selectAll().where {
            (PersonRolleTable.personId eq personId) and
                (PersonRolleTable.rolleId eq rolleId) and
                (PersonRolleTable.istAktiv eq true) and
                (PersonRolleTable.gueltigVon lessEq checkDate) and
                (PersonRolleTable.gueltigBis.isNull() or (PersonRolleTable.gueltigBis greaterEq checkDate))
        }

        val query = if (vereinId != null) {
            baseQuery.andWhere { PersonRolleTable.vereinId eq vereinId }
        } else {
            baseQuery.andWhere { PersonRolleTable.vereinId.isNull() }
        }

        query.count() > 0
    }
}

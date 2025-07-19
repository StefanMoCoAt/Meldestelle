package at.mocode.members.infrastructure.repository

import at.mocode.members.domain.model.DomPerson
import at.mocode.members.domain.repository.PersonRepository
import at.mocode.shared.database.DatabaseFactory
import com.benasher44.uuid.Uuid
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

/**
 * Exposed-based implementation of PersonRepository.
 *
 * This implementation provides data persistence for Person entities
 * using the Exposed SQL framework and PostgreSQL database.
 */
class PersonRepositoryImpl : PersonRepository {

    override suspend fun findById(id: Uuid): DomPerson? = DatabaseFactory.dbQuery {
        PersonTable.selectAll().where { PersonTable.id eq id }
            .map { rowToDomPerson(it) }
            .singleOrNull()
    }

    override suspend fun findByOepsSatzNr(oepsSatzNr: String): DomPerson? = DatabaseFactory.dbQuery {
        PersonTable.selectAll().where { PersonTable.oepsSatzNr eq oepsSatzNr }
            .map { rowToDomPerson(it) }
            .singleOrNull()
    }

    override suspend fun findByStammVereinId(vereinId: Uuid): List<DomPerson> = DatabaseFactory.dbQuery {
        PersonTable.selectAll().where { PersonTable.stammVereinId eq vereinId }
            .map { rowToDomPerson(it) }
    }

    override suspend fun findByName(searchTerm: String, limit: Int): List<DomPerson> = DatabaseFactory.dbQuery {
        val searchPattern = "%$searchTerm%"
        PersonTable.selectAll().where {
            (PersonTable.nachname like searchPattern) or
                (PersonTable.vorname like searchPattern)
        }
        .limit(limit)
        .map { rowToDomPerson(it) }
    }

    override suspend fun findAllActive(limit: Int, offset: Int): List<DomPerson> = DatabaseFactory.dbQuery {
        PersonTable.selectAll().where { PersonTable.istAktiv eq true }
            .limit(limit, offset.toLong())
            .map { rowToDomPerson(it) }
    }

    override suspend fun save(person: DomPerson): DomPerson = DatabaseFactory.dbQuery {
        val now = Clock.System.now()
        val existingPerson = findById(person.personId)

        if (existingPerson == null) {
            // Insert a new person
            PersonTable.insert { stmt ->
                stmt[PersonTable.id] = person.personId
                stmt[PersonTable.oepsSatzNr] = person.oepsSatzNr
                stmt[PersonTable.nachname] = person.nachname
                stmt[PersonTable.vorname] = person.vorname
                stmt[PersonTable.titel] = person.titel
                stmt[PersonTable.geburtsdatum] = person.geburtsdatum
                stmt[PersonTable.geschlecht] = person.geschlechtE
                stmt[PersonTable.nationalitaetLandId] = person.nationalitaetLandId
                stmt[PersonTable.feiId] = person.feiId
                stmt[PersonTable.telefon] = person.telefon
                stmt[PersonTable.email] = person.email
                stmt[PersonTable.strasse] = person.strasse
                stmt[PersonTable.plz] = person.plz
                stmt[PersonTable.ort] = person.ort
                stmt[PersonTable.adresszusatzZusatzinfo] = person.adresszusatzZusatzinfo
                stmt[PersonTable.stammVereinId] = person.stammVereinId
                stmt[PersonTable.mitgliedsNummerBeiStammVerein] = person.mitgliedsNummerBeiStammVerein
                stmt[PersonTable.istGesperrt] = person.istGesperrt
                stmt[PersonTable.sperrGrund] = person.sperrGrund
                stmt[PersonTable.altersklasseOepsCodeRaw] = person.altersklasseOepsCodeRaw
                stmt[PersonTable.istJungerReiterOepsFlag] = person.istJungerReiterOepsFlag
                stmt[PersonTable.kaderStatusOepsRaw] = person.kaderStatusOepsRaw
                stmt[PersonTable.datenQuelle] = person.datenQuelle
                stmt[PersonTable.istAktiv] = person.istAktiv
                stmt[PersonTable.notizenIntern] = person.notizenIntern
                stmt[PersonTable.createdAt] = person.createdAt.toLocalDateTime(TimeZone.UTC)
                stmt[PersonTable.updatedAt] = now.toLocalDateTime(TimeZone.UTC)
            }
        } else {
            // Update existing person
            PersonTable.update({ PersonTable.id eq person.personId }) { stmt ->
                stmt[PersonTable.oepsSatzNr] = person.oepsSatzNr
                stmt[PersonTable.nachname] = person.nachname
                stmt[PersonTable.vorname] = person.vorname
                stmt[PersonTable.titel] = person.titel
                stmt[PersonTable.geburtsdatum] = person.geburtsdatum
                stmt[PersonTable.geschlecht] = person.geschlechtE
                stmt[PersonTable.nationalitaetLandId] = person.nationalitaetLandId
                stmt[PersonTable.feiId] = person.feiId
                stmt[PersonTable.telefon] = person.telefon
                stmt[PersonTable.email] = person.email
                stmt[PersonTable.strasse] = person.strasse
                stmt[PersonTable.plz] = person.plz
                stmt[PersonTable.ort] = person.ort
                stmt[PersonTable.adresszusatzZusatzinfo] = person.adresszusatzZusatzinfo
                stmt[PersonTable.stammVereinId] = person.stammVereinId
                stmt[PersonTable.mitgliedsNummerBeiStammVerein] = person.mitgliedsNummerBeiStammVerein
                stmt[PersonTable.istGesperrt] = person.istGesperrt
                stmt[PersonTable.sperrGrund] = person.sperrGrund
                stmt[PersonTable.altersklasseOepsCodeRaw] = person.altersklasseOepsCodeRaw
                stmt[PersonTable.istJungerReiterOepsFlag] = person.istJungerReiterOepsFlag
                stmt[PersonTable.kaderStatusOepsRaw] = person.kaderStatusOepsRaw
                stmt[PersonTable.datenQuelle] = person.datenQuelle
                stmt[PersonTable.istAktiv] = person.istAktiv
                stmt[PersonTable.notizenIntern] = person.notizenIntern
                stmt[PersonTable.updatedAt] = now.toLocalDateTime(TimeZone.UTC)
            }
        }

        person.copy(updatedAt = now)
    }

    override suspend fun delete(id: Uuid): Boolean = DatabaseFactory.dbQuery {
        val deletedRows = PersonTable.deleteWhere { PersonTable.id eq id }
        deletedRows > 0
    }

    override suspend fun existsByOepsSatzNr(oepsSatzNr: String): Boolean = DatabaseFactory.dbQuery {
        PersonTable.selectAll().where { PersonTable.oepsSatzNr eq oepsSatzNr }
            .count() > 0
    }

    override suspend fun countActive(): Long = DatabaseFactory.dbQuery {
        PersonTable.selectAll().where { PersonTable.istAktiv eq true }
            .count()
    }

    /**
     * Converts a database row to a DomPerson domain object.
     */
    private fun rowToDomPerson(row: ResultRow): DomPerson {
        return DomPerson(
            personId = row[PersonTable.id].value,
            oepsSatzNr = row[PersonTable.oepsSatzNr],
            nachname = row[PersonTable.nachname],
            vorname = row[PersonTable.vorname],
            titel = row[PersonTable.titel],
            geburtsdatum = row[PersonTable.geburtsdatum],
            geschlechtE = row[PersonTable.geschlecht],
            nationalitaetLandId = row[PersonTable.nationalitaetLandId],
            feiId = row[PersonTable.feiId],
            telefon = row[PersonTable.telefon],
            email = row[PersonTable.email],
            strasse = row[PersonTable.strasse],
            plz = row[PersonTable.plz],
            ort = row[PersonTable.ort],
            adresszusatzZusatzinfo = row[PersonTable.adresszusatzZusatzinfo],
            stammVereinId = row[PersonTable.stammVereinId],
            mitgliedsNummerBeiStammVerein = row[PersonTable.mitgliedsNummerBeiStammVerein],
            istGesperrt = row[PersonTable.istGesperrt],
            sperrGrund = row[PersonTable.sperrGrund],
            altersklasseOepsCodeRaw = row[PersonTable.altersklasseOepsCodeRaw],
            istJungerReiterOepsFlag = row[PersonTable.istJungerReiterOepsFlag],
            kaderStatusOepsRaw = row[PersonTable.kaderStatusOepsRaw],
            datenQuelle = row[PersonTable.datenQuelle],
            istAktiv = row[PersonTable.istAktiv],
            notizenIntern = row[PersonTable.notizenIntern],
            createdAt = row[PersonTable.createdAt].toInstant(TimeZone.UTC),
            updatedAt = row[PersonTable.updatedAt].toInstant(TimeZone.UTC)
        )
    }
}

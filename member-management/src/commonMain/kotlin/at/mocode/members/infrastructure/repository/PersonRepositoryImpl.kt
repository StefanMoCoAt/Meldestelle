package at.mocode.members.infrastructure.repository

import at.mocode.members.domain.model.DomPerson
import at.mocode.members.domain.repository.PersonRepository
import com.benasher44.uuid.Uuid
import kotlinx.datetime.Clock
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toKotlinLocalDate
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like

/**
 * Exposed-based implementation of PersonRepository.
 *
 * This implementation provides data persistence for Person entities
 * using the Exposed SQL framework and PostgreSQL database.
 */
class PersonRepositoryImpl : PersonRepository {

    override suspend fun findById(id: Uuid): DomPerson? {
        return PersonTable.select { PersonTable.id eq id }
            .map { rowToDomPerson(it) }
            .singleOrNull()
    }

    override suspend fun findByOepsSatzNr(oepsSatzNr: String): DomPerson? {
        return PersonTable.select { PersonTable.oepsSatzNr eq oepsSatzNr }
            .map { rowToDomPerson(it) }
            .singleOrNull()
    }

    override suspend fun findByStammVereinId(vereinId: Uuid): List<DomPerson> {
        return PersonTable.select { PersonTable.stammVereinId eq vereinId }
            .map { rowToDomPerson(it) }
    }

    override suspend fun findByName(searchTerm: String, limit: Int): List<DomPerson> {
        val searchPattern = "%$searchTerm%"
        return PersonTable.select {
            (PersonTable.nachname like searchPattern) or
            (PersonTable.vorname like searchPattern)
        }
        .limit(limit)
        .map { rowToDomPerson(it) }
    }

    override suspend fun findAllActive(limit: Int, offset: Int): List<DomPerson> {
        return PersonTable.select { PersonTable.istAktiv eq true }
            .limit(limit, offset.toLong())
            .map { rowToDomPerson(it) }
    }

    override suspend fun save(person: DomPerson): DomPerson {
        val now = Clock.System.now()
        val updatedPerson = person.copy(updatedAt = now)

        PersonTable.insertOrUpdate(PersonTable.id) {
            it[id] = person.personId
            it[oepsSatzNr] = person.oepsSatzNr
            it[nachname] = person.nachname
            it[vorname] = person.vorname
            it[titel] = person.titel
            it[geburtsdatum] = person.geburtsdatum
            it[geschlecht] = person.geschlechtE
            it[nationalitaetLandId] = person.nationalitaetLandId
            it[feiId] = person.feiId
            it[telefon] = person.telefon
            it[email] = person.email
            it[strasse] = person.strasse
            it[plz] = person.plz
            it[ort] = person.ort
            it[adresszusatzZusatzinfo] = person.adresszusatzZusatzinfo
            it[stammVereinId] = person.stammVereinId
            it[mitgliedsNummerBeiStammVerein] = person.mitgliedsNummerBeiStammVerein
            it[istGesperrt] = person.istGesperrt
            it[sperrGrund] = person.sperrGrund
            it[altersklasseOepsCodeRaw] = person.altersklasseOepsCodeRaw
            it[istJungerReiterOepsFlag] = person.istJungerReiterOepsFlag
            it[kaderStatusOepsRaw] = person.kaderStatusOepsRaw
            it[datenQuelle] = person.datenQuelle
            it[istAktiv] = person.istAktiv
            it[notizenIntern] = person.notizenIntern
            it[createdAt] = person.createdAt.toJavaInstant()
            it[updatedAt] = updatedPerson.updatedAt.toJavaInstant()
        }

        return updatedPerson
    }

    override suspend fun delete(id: Uuid): Boolean {
        val deletedRows = PersonTable.deleteWhere { PersonTable.id eq id }
        return deletedRows > 0
    }

    override suspend fun existsByOepsSatzNr(oepsSatzNr: String): Boolean {
        return PersonTable.select { PersonTable.oepsSatzNr eq oepsSatzNr }
            .count() > 0
    }

    override suspend fun countActive(): Long {
        return PersonTable.select { PersonTable.istAktiv eq true }
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
            createdAt = row[PersonTable.createdAt].toKotlinInstant(),
            updatedAt = row[PersonTable.updatedAt].toKotlinInstant()
        )
    }
}

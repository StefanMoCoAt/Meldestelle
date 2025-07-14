package at.mocode.repositories

import at.mocode.enums.FunktionaerRolleE
import at.mocode.stammdaten.Person
import at.mocode.tables.stammdaten.PersonenTable
import com.benasher44.uuid.Uuid
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class PostgresPersonRepository : PersonRepository {

    override suspend fun findAll(): List<Person> = transaction {
        PersonenTable.selectAll().map { rowToPerson(it) }
    }

    override suspend fun findById(id: Uuid): Person? = transaction {
        PersonenTable.selectAll().where { PersonenTable.id eq id }
            .map { rowToPerson(it) }
            .singleOrNull()
    }

    override suspend fun findByOepsSatzNr(oepsSatzNr: String): Person? = transaction {
        PersonenTable.selectAll().where { PersonenTable.oepsSatzNr eq oepsSatzNr }
            .map { rowToPerson(it) }
            .singleOrNull()
    }

    override suspend fun create(person: Person): Person = transaction {
        val now = Clock.System.now()
        PersonenTable.insert {
            it[id] = person.id
            it[oepsSatzNr] = person.oepsSatzNr
            it[nachname] = person.nachname
            it[vorname] = person.vorname
            it[titel] = person.titel
            it[geburtsdatum] = person.geburtsdatum
            it[geschlecht] = person.geschlechtE
            it[nationalitaet] = person.nationalitaet
            it[email] = person.email
            it[telefon] = person.telefon
            it[adresse] = person.adresse
            it[plz] = person.plz
            it[ort] = person.ort
            it[stammVereinId] = person.stammVereinId
            it[mitgliedsNummerIntern] = person.mitgliedsNummerIntern
            it[letzteZahlungJahr] = person.letzteZahlungJahr
            it[feiId] = person.feiId
            it[istGesperrt] = person.istGesperrt
            it[sperrGrund] = person.sperrGrund
            it[rollenCsv] = person.rollen.joinToString(",") { rolle -> rolle.name }
            it[qualifikationenRichterCsv] = person.qualifikationenRichter.joinToString(",")
            it[qualifikationenParcoursbauerCsv] = person.qualifikationenParcoursbauer.joinToString(",")
            it[istAktiv] = person.istAktiv
            it[createdAt] = now
            it[updatedAt] = now
        }
        person.copy(createdAt = now, updatedAt = now)
    }

    override suspend fun update(id: Uuid, person: Person): Person? = transaction {
        val updateCount = PersonenTable.update({ PersonenTable.id eq id }) {
            it[nachname] = person.nachname
            it[vorname] = person.vorname
            it[titel] = person.titel
            it[geburtsdatum] = person.geburtsdatum
            it[geschlecht] = person.geschlechtE
            it[nationalitaet] = person.nationalitaet
            it[email] = person.email
            it[telefon] = person.telefon
            it[adresse] = person.adresse
            it[plz] = person.plz
            it[ort] = person.ort
            it[stammVereinId] = person.stammVereinId
            it[mitgliedsNummerIntern] = person.mitgliedsNummerIntern
            it[letzteZahlungJahr] = person.letzteZahlungJahr
            it[feiId] = person.feiId
            it[istGesperrt] = person.istGesperrt
            it[sperrGrund] = person.sperrGrund
            it[rollenCsv] = person.rollen.joinToString(",") { rolle -> rolle.name }
            it[qualifikationenRichterCsv] = person.qualifikationenRichter.joinToString(",")
            it[qualifikationenParcoursbauerCsv] = person.qualifikationenParcoursbauer.joinToString(",")
            it[istAktiv] = person.istAktiv
            it[updatedAt] = Clock.System.now()
        }
        if (updateCount > 0) {
            PersonenTable.selectAll().where { PersonenTable.id eq id }
                .map { rowToPerson(it) }
                .singleOrNull()
        } else null
    }

    override suspend fun delete(id: Uuid): Boolean = transaction {
        PersonenTable.deleteWhere { PersonenTable.id eq id } > 0
    }

    override suspend fun findByVereinId(vereinId: Uuid): List<Person> = transaction {
        PersonenTable.selectAll().where { PersonenTable.stammVereinId eq vereinId }
            .map { rowToPerson(it) }
    }

    override suspend fun search(query: String): List<Person> = transaction {
        PersonenTable.selectAll().where {
            (PersonenTable.nachname.lowerCase() like "%${query.lowercase()}%") or
                (PersonenTable.vorname.lowerCase() like "%${query.lowercase()}%") or
                PersonenTable.email.lowerCase().like("%${query.lowercase()}%")
        }.map { rowToPerson(it) }
    }

    private fun rowToPerson(row: ResultRow): Person {
        return Person(
            id = row[PersonenTable.id],
            oepsSatzNr = row[PersonenTable.oepsSatzNr],
            nachname = row[PersonenTable.nachname],
            vorname = row[PersonenTable.vorname],
            titel = row[PersonenTable.titel],
            geburtsdatum = row[PersonenTable.geburtsdatum],
            geschlechtE = row[PersonenTable.geschlecht],
            nationalitaet = row[PersonenTable.nationalitaet],
            email = row[PersonenTable.email],
            telefon = row[PersonenTable.telefon],
            adresse = row[PersonenTable.adresse],
            plz = row[PersonenTable.plz],
            ort = row[PersonenTable.ort],
            stammVereinId = row[PersonenTable.stammVereinId],
            mitgliedsNummerIntern = row[PersonenTable.mitgliedsNummerIntern],
            letzteZahlungJahr = row[PersonenTable.letzteZahlungJahr],
            feiId = row[PersonenTable.feiId],
            istGesperrt = row[PersonenTable.istGesperrt],
            sperrGrund = row[PersonenTable.sperrGrund],
            rollen = parseRollen(row[PersonenTable.rollenCsv]),
            lizenzen = emptyList(), // TODO: Load from separate table if needed
            qualifikationenRichter = parseQualifikationen(row[PersonenTable.qualifikationenRichterCsv]),
            qualifikationenParcoursbauer = parseQualifikationen(row[PersonenTable.qualifikationenParcoursbauerCsv]),
            istAktiv = row[PersonenTable.istAktiv],
            createdAt = row[PersonenTable.createdAt],
            updatedAt = row[PersonenTable.updatedAt]
        )
    }

    private fun parseRollen(rollenCsv: String?): Set<FunktionaerRolleE> {
        return if (rollenCsv.isNullOrBlank()) {
            emptySet()
        } else {
            rollenCsv.split(",")
                .mapNotNull { roleName ->
                    try {
                        FunktionaerRolleE.valueOf(roleName.trim())
                    } catch (_: IllegalArgumentException) {
                        null
                    }
                }
                .toSet()
        }
    }

    private fun parseQualifikationen(qualifikationenCsv: String?): List<String> {
        return if (qualifikationenCsv.isNullOrBlank()) {
            emptyList()
        } else {
            qualifikationenCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        }
    }
}

package at.mocode.repositories

import at.mocode.stammdaten.Verein
import at.mocode.tables.stammdaten.VereineTable
import com.benasher44.uuid.Uuid
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class PostgresVereinRepository : VereinRepository {

    override suspend fun findAll(): List<Verein> = transaction {
        VereineTable.selectAll().map { rowToVerein(it) }
    }

    override suspend fun findById(id: Uuid): Verein? = transaction {
        VereineTable.selectAll().where { VereineTable.id eq id }
            .map { rowToVerein(it) }
            .singleOrNull()
    }

    override suspend fun findByOepsVereinsNr(oepsVereinsNr: String): Verein? = transaction {
        VereineTable.selectAll().where { VereineTable.oepsVereinsNr eq oepsVereinsNr }
            .map { rowToVerein(it) }
            .singleOrNull()
    }

    override suspend fun create(verein: Verein): Verein = transaction {
        val now = Clock.System.now()
        VereineTable.insert {
            it[id] = verein.id
            it[oepsVereinsNr] = verein.oepsVereinsNr
            it[name] = verein.name
            it[kuerzel] = verein.kuerzel
            it[bundesland] = verein.bundesland
            it[adresse] = verein.adresse
            it[plz] = verein.plz
            it[ort] = verein.ort
            it[email] = verein.email
            it[telefon] = verein.telefon
            it[webseite] = verein.webseite
            it[istAktiv] = verein.istAktiv
            it[createdAt] = now
            it[updatedAt] = now
        }
        verein.copy(createdAt = now, updatedAt = now)
    }

    override suspend fun update(id: Uuid, verein: Verein): Verein? = transaction {
        val updateCount = VereineTable.update({ VereineTable.id eq id }) {
            it[name] = verein.name
            it[kuerzel] = verein.kuerzel
            it[bundesland] = verein.bundesland
            it[adresse] = verein.adresse
            it[plz] = verein.plz
            it[ort] = verein.ort
            it[email] = verein.email
            it[telefon] = verein.telefon
            it[webseite] = verein.webseite
            it[istAktiv] = verein.istAktiv
            it[updatedAt] = Clock.System.now()
        }
        if (updateCount > 0) {
            VereineTable.selectAll().where { VereineTable.id eq id }
                .map { rowToVerein(it) }
                .singleOrNull()
        } else null
    }

    override suspend fun delete(id: Uuid): Boolean = transaction {
        VereineTable.deleteWhere { VereineTable.id eq id } > 0
    }

    override suspend fun findByBundesland(bundesland: String): List<Verein> = transaction {
        VereineTable.selectAll().where { VereineTable.bundesland eq bundesland }
            .map { rowToVerein(it) }
    }

    override suspend fun search(query: String): List<Verein> = transaction {
        VereineTable.selectAll().where {
            (VereineTable.name.lowerCase() like "%${query.lowercase()}%") or
                VereineTable.kuerzel.lowerCase().like("%${query.lowercase()}%") or
                VereineTable.ort.lowerCase().like("%${query.lowercase()}%")
        }.map { rowToVerein(it) }
    }

    private fun rowToVerein(row: ResultRow): Verein {
        return Verein(
            id = row[VereineTable.id],
            oepsVereinsNr = row[VereineTable.oepsVereinsNr],
            name = row[VereineTable.name],
            kuerzel = row[VereineTable.kuerzel],
            bundesland = row[VereineTable.bundesland],
            adresse = row[VereineTable.adresse],
            plz = row[VereineTable.plz],
            ort = row[VereineTable.ort],
            email = row[VereineTable.email],
            telefon = row[VereineTable.telefon],
            webseite = row[VereineTable.webseite],
            istAktiv = row[VereineTable.istAktiv],
            createdAt = row[VereineTable.createdAt],
            updatedAt = row[VereineTable.updatedAt]
        )
    }
}

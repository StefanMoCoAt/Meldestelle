package at.mocode.repositories

import at.mocode.model.Turnier
import at.mocode.tables.TurniereTable
import at.mocode.enums.NennungsArtE
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import kotlinx.datetime.Clock
import com.ionspin.kotlin.bignum.decimal.BigDecimal

class PostgresTurnierRepository : TurnierRepository {

    override suspend fun findAll(): List<Turnier> = transaction {
        TurniereTable.selectAll().map { rowToTurnier(it) }
    }

    override suspend fun findById(id: Uuid): Turnier? = transaction {
        TurniereTable.selectAll().where { TurniereTable.id eq id }
            .map { rowToTurnier(it) }
            .singleOrNull()
    }

    override suspend fun findByVeranstaltungId(veranstaltungId: Uuid): List<Turnier> = transaction {
        TurniereTable.selectAll().where { TurniereTable.veranstaltungId eq veranstaltungId }
            .map { rowToTurnier(it) }
    }

    override suspend fun findByOepsTurnierNr(oepsTurnierNr: String): Turnier? = transaction {
        TurniereTable.selectAll().where { TurniereTable.oepsTurnierNr eq oepsTurnierNr }
            .map { rowToTurnier(it) }
            .singleOrNull()
    }

    override suspend fun create(turnier: Turnier): Turnier = transaction {
        TurniereTable.insert {
            it[id] = turnier.id
            it[veranstaltungId] = turnier.veranstaltungId
            it[oepsTurnierNr] = turnier.oepsTurnierNr
            it[titel] = turnier.titel
            it[untertitel] = turnier.untertitel
            it[datumVon] = turnier.datumVon
            it[datumBis] = turnier.datumBis
            it[nennungsschluss] = turnier.nennungsschluss
            it[nennungsArtCsv] = turnier.nennungsArt.joinToString(",") { art -> art.name }
            it[nennungsHinweis] = turnier.nennungsHinweis
            it[eigenesNennsystemUrl] = turnier.eigenesNennsystemUrl
            it[nenngeld] = turnier.nenngeld?.toString()
            it[startgeldStandard] = turnier.startgeldStandard?.toString()
            it[turnierleiterId] = turnier.turnierleiterId
            it[turnierbeauftragterId] = turnier.turnierbeauftragterId
            it[richterIdsCsv] = turnier.richterIds.joinToString(",") { uuid -> uuid.toString() }
            it[parcoursbauerIdsCsv] = turnier.parcoursbauerIds.joinToString(",") { uuid -> uuid.toString() }
            it[parcoursAssistentIdsCsv] = turnier.parcoursAssistentIds.joinToString(",") { uuid -> uuid.toString() }
            it[tierarztInfos] = turnier.tierarztInfos
            it[hufschmiedInfo] = turnier.hufschmiedInfo
            it[meldestelleVerantwortlicherId] = turnier.meldestelleVerantwortlicherId
            it[meldestelleTelefon] = turnier.meldestelleTelefon
            it[meldestelleOeffnungszeiten] = turnier.meldestelleOeffnungszeiten
            it[ergebnislistenUrl] = turnier.ergebnislistenUrl
            it[createdAt] = turnier.createdAt
            it[updatedAt] = Clock.System.now()
        }
        turnier
    }

    override suspend fun update(id: Uuid, turnier: Turnier): Turnier? = transaction {
        val updateCount = TurniereTable.update({ TurniereTable.id eq id }) {
            it[veranstaltungId] = turnier.veranstaltungId
            it[oepsTurnierNr] = turnier.oepsTurnierNr
            it[titel] = turnier.titel
            it[untertitel] = turnier.untertitel
            it[datumVon] = turnier.datumVon
            it[datumBis] = turnier.datumBis
            it[nennungsschluss] = turnier.nennungsschluss
            it[nennungsArtCsv] = turnier.nennungsArt.joinToString(",") { art -> art.name }
            it[nennungsHinweis] = turnier.nennungsHinweis
            it[eigenesNennsystemUrl] = turnier.eigenesNennsystemUrl
            it[nenngeld] = turnier.nenngeld?.toString()
            it[startgeldStandard] = turnier.startgeldStandard?.toString()
            it[turnierleiterId] = turnier.turnierleiterId
            it[turnierbeauftragterId] = turnier.turnierbeauftragterId
            it[richterIdsCsv] = turnier.richterIds.joinToString(",") { uuid -> uuid.toString() }
            it[parcoursbauerIdsCsv] = turnier.parcoursbauerIds.joinToString(",") { uuid -> uuid.toString() }
            it[parcoursAssistentIdsCsv] = turnier.parcoursAssistentIds.joinToString(",") { uuid -> uuid.toString() }
            it[tierarztInfos] = turnier.tierarztInfos
            it[hufschmiedInfo] = turnier.hufschmiedInfo
            it[meldestelleVerantwortlicherId] = turnier.meldestelleVerantwortlicherId
            it[meldestelleTelefon] = turnier.meldestelleTelefon
            it[meldestelleOeffnungszeiten] = turnier.meldestelleOeffnungszeiten
            it[ergebnislistenUrl] = turnier.ergebnislistenUrl
            it[updatedAt] = Clock.System.now()
        }
        if (updateCount > 0) {
            TurniereTable.selectAll().where { TurniereTable.id eq id }
                .map { rowToTurnier(it) }
                .singleOrNull()
        } else null
    }

    override suspend fun delete(id: Uuid): Boolean = transaction {
        TurniereTable.deleteWhere { TurniereTable.id eq id } > 0
    }

    override suspend fun search(query: String): List<Turnier> = transaction {
        TurniereTable.selectAll().where {
            (TurniereTable.titel.lowerCase() like "%${query.lowercase()}%") or
                (TurniereTable.untertitel?.lowerCase()?.like("%${query.lowercase()}%") ?: Op.FALSE) or
                (TurniereTable.oepsTurnierNr.lowerCase() like "%${query.lowercase()}%")
        }.map { rowToTurnier(it) }
    }

    private fun rowToTurnier(row: ResultRow): Turnier {
        return Turnier(
            id = row[TurniereTable.id],
            veranstaltungId = row[TurniereTable.veranstaltungId],
            oepsTurnierNr = row[TurniereTable.oepsTurnierNr],
            titel = row[TurniereTable.titel],
            untertitel = row[TurniereTable.untertitel],
            datumVon = row[TurniereTable.datumVon],
            datumBis = row[TurniereTable.datumBis],
            nennungsschluss = row[TurniereTable.nennungsschluss],
            nennungsArt = parseNennungsArt(row[TurniereTable.nennungsArtCsv]),
            nennungsHinweis = row[TurniereTable.nennungsHinweis],
            eigenesNennsystemUrl = row[TurniereTable.eigenesNennsystemUrl],
            nenngeld = row[TurniereTable.nenngeld]?.let { BigDecimal.parseString(it) },
            startgeldStandard = row[TurniereTable.startgeldStandard]?.let { BigDecimal.parseString(it) },
            turnierleiterId = row[TurniereTable.turnierleiterId],
            turnierbeauftragterId = row[TurniereTable.turnierbeauftragterId],
            richterIds = parseUuidList(row[TurniereTable.richterIdsCsv]),
            parcoursbauerIds = parseUuidList(row[TurniereTable.parcoursbauerIdsCsv]),
            parcoursAssistentIds = parseUuidList(row[TurniereTable.parcoursAssistentIdsCsv]),
            tierarztInfos = row[TurniereTable.tierarztInfos],
            hufschmiedInfo = row[TurniereTable.hufschmiedInfo],
            meldestelleVerantwortlicherId = row[TurniereTable.meldestelleVerantwortlicherId],
            meldestelleTelefon = row[TurniereTable.meldestelleTelefon],
            meldestelleOeffnungszeiten = row[TurniereTable.meldestelleOeffnungszeiten],
            ergebnislistenUrl = row[TurniereTable.ergebnislistenUrl],
            createdAt = row[TurniereTable.createdAt],
            updatedAt = row[TurniereTable.updatedAt]
        )
    }

    private fun parseNennungsArt(csv: String?): List<NennungsArtE> {
        return if (csv.isNullOrBlank()) {
            emptyList()
        } else {
            csv.split(",").mapNotNull { artName ->
                try {
                    NennungsArtE.valueOf(artName.trim())
                } catch (e: IllegalArgumentException) {
                    null // Skip invalid enum values
                }
            }
        }
    }

    private fun parseUuidList(csv: String?): List<Uuid> {
        return if (csv.isNullOrBlank()) {
            emptyList()
        } else {
            csv.split(",").mapNotNull { uuidString ->
                try {
                    uuidFrom(uuidString.trim())
                } catch (e: IllegalArgumentException) {
                    null // Skip invalid UUIDs
                }
            }
        }
    }
}

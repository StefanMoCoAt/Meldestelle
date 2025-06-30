package at.mocode.model

import at.mocode.model.domaene.DomPferd
import at.mocode.tables.DomPferdTable
import com.benasher44.uuid.Uuid
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.transactions.transaction

class PostgresDomPferdRepository : DomPferdRepository {

    override suspend fun findAll(): List<DomPferd> = transaction {
        DomPferdTable.selectAll().map { rowToDomPferd(it) }
    }

    override suspend fun findById(id: Uuid): DomPferd? = transaction {
        DomPferdTable.select { DomPferdTable.pferdId eq id }
            .map { rowToDomPferd(it) }
            .singleOrNull()
    }

    override suspend fun findByOepsSatzNr(oepsSatzNr: String): DomPferd? = transaction {
        DomPferdTable.select { DomPferdTable.oepsSatzNrPferd eq oepsSatzNr }
            .map { rowToDomPferd(it) }
            .singleOrNull()
    }

    override suspend fun findByName(name: String): List<DomPferd> = transaction {
        DomPferdTable.select { DomPferdTable.name like "%$name%" }
            .map { rowToDomPferd(it) }
    }

    override suspend fun findByLebensnummer(lebensnummer: String): DomPferd? = transaction {
        DomPferdTable.select { DomPferdTable.lebensnummer eq lebensnummer }
            .map { rowToDomPferd(it) }
            .singleOrNull()
    }

    override suspend fun findByBesitzerId(besitzerId: Uuid): List<DomPferd> = transaction {
        DomPferdTable.select { DomPferdTable.besitzerPersonId eq besitzerId }
            .map { rowToDomPferd(it) }
    }

    override suspend fun findByVerantwortlichePersonId(personId: Uuid): List<DomPferd> = transaction {
        DomPferdTable.select { DomPferdTable.verantwortlichePersonId eq personId }
            .map { rowToDomPferd(it) }
    }

    override suspend fun findByHeimatVereinId(vereinId: Uuid): List<DomPferd> = transaction {
        DomPferdTable.select { DomPferdTable.heimatVereinId eq vereinId }
            .map { rowToDomPferd(it) }
    }

    override suspend fun findByRasse(rasse: String): List<DomPferd> = transaction {
        DomPferdTable.select { DomPferdTable.rasse like "%$rasse%" }
            .map { rowToDomPferd(it) }
    }

    override suspend fun findByGeburtsjahr(geburtsjahr: Int): List<DomPferd> = transaction {
        DomPferdTable.select { DomPferdTable.geburtsjahr eq geburtsjahr }
            .map { rowToDomPferd(it) }
    }

    override suspend fun findActiveHorses(): List<DomPferd> = transaction {
        DomPferdTable.select { DomPferdTable.istAktiv eq true }
            .map { rowToDomPferd(it) }
    }

    override suspend fun create(domPferd: DomPferd): DomPferd = transaction {
        val now = Clock.System.now()
        DomPferdTable.insert {
            it[pferdId] = domPferd.pferdId
            it[oepsSatzNrPferd] = domPferd.oepsSatzNrPferd
            it[oepsKopfNr] = domPferd.oepsKopfNr
            it[name] = domPferd.name
            it[lebensnummer] = domPferd.lebensnummer
            it[feiPassNr] = domPferd.feiPassNr
            it[geburtsjahr] = domPferd.geburtsjahr
            it[geschlecht] = domPferd.geschlecht
            it[farbe] = domPferd.farbe
            it[rasse] = domPferd.rasse
            it[abstammungVaterName] = domPferd.abstammungVaterName
            it[abstammungMutterName] = domPferd.abstammungMutterName
            it[abstammungMutterVaterName] = domPferd.abstammungMutterVaterName
            it[abstammungZusatzInfo] = domPferd.abstammungZusatzInfo
            it[besitzerPersonId] = domPferd.besitzerPersonId
            it[verantwortlichePersonId] = domPferd.verantwortlichePersonId
            it[heimatVereinId] = domPferd.heimatVereinId
            it[letzteZahlungPferdegebuehrJahrOeps] = domPferd.letzteZahlungPferdegebuehrJahrOeps
            it[stockmassCm] = domPferd.stockmassCm
            it[datenQuelle] = domPferd.datenQuelle
            it[istAktiv] = domPferd.istAktiv
            it[notizenIntern] = domPferd.notizenIntern
            it[createdAt] = domPferd.createdAt
            it[updatedAt] = now
        }
        domPferd.copy(updatedAt = now)
    }

    override suspend fun update(id: Uuid, domPferd: DomPferd): DomPferd? = transaction {
        val now = Clock.System.now()
        val updateCount = DomPferdTable.update({ DomPferdTable.pferdId eq id }) {
            it[oepsSatzNrPferd] = domPferd.oepsSatzNrPferd
            it[oepsKopfNr] = domPferd.oepsKopfNr
            it[name] = domPferd.name
            it[lebensnummer] = domPferd.lebensnummer
            it[feiPassNr] = domPferd.feiPassNr
            it[geburtsjahr] = domPferd.geburtsjahr
            it[geschlecht] = domPferd.geschlecht
            it[farbe] = domPferd.farbe
            it[rasse] = domPferd.rasse
            it[abstammungVaterName] = domPferd.abstammungVaterName
            it[abstammungMutterName] = domPferd.abstammungMutterName
            it[abstammungMutterVaterName] = domPferd.abstammungMutterVaterName
            it[abstammungZusatzInfo] = domPferd.abstammungZusatzInfo
            it[besitzerPersonId] = domPferd.besitzerPersonId
            it[verantwortlichePersonId] = domPferd.verantwortlichePersonId
            it[heimatVereinId] = domPferd.heimatVereinId
            it[letzteZahlungPferdegebuehrJahrOeps] = domPferd.letzteZahlungPferdegebuehrJahrOeps
            it[stockmassCm] = domPferd.stockmassCm
            it[datenQuelle] = domPferd.datenQuelle
            it[istAktiv] = domPferd.istAktiv
            it[notizenIntern] = domPferd.notizenIntern
            it[updatedAt] = now
        }
        if (updateCount > 0) {
            domPferd.copy(pferdId = id, updatedAt = now)
        } else {
            null
        }
    }

    override suspend fun delete(id: Uuid): Boolean = transaction {
        DomPferdTable.deleteWhere { pferdId eq id } > 0
    }

    override suspend fun search(query: String): List<DomPferd> = transaction {
        DomPferdTable.select {
            (DomPferdTable.name like "%$query%") or
            (DomPferdTable.lebensnummer like "%$query%") or
            (DomPferdTable.rasse like "%$query%") or
            (DomPferdTable.notizenIntern like "%$query%")
        }.map { rowToDomPferd(it) }
    }

    private fun rowToDomPferd(row: ResultRow): DomPferd {
        return DomPferd(
            pferdId = row[DomPferdTable.pferdId],
            oepsSatzNrPferd = row[DomPferdTable.oepsSatzNrPferd],
            oepsKopfNr = row[DomPferdTable.oepsKopfNr],
            name = row[DomPferdTable.name],
            lebensnummer = row[DomPferdTable.lebensnummer],
            feiPassNr = row[DomPferdTable.feiPassNr],
            geburtsjahr = row[DomPferdTable.geburtsjahr],
            geschlecht = row[DomPferdTable.geschlecht],
            farbe = row[DomPferdTable.farbe],
            rasse = row[DomPferdTable.rasse],
            abstammungVaterName = row[DomPferdTable.abstammungVaterName],
            abstammungMutterName = row[DomPferdTable.abstammungMutterName],
            abstammungMutterVaterName = row[DomPferdTable.abstammungMutterVaterName],
            abstammungZusatzInfo = row[DomPferdTable.abstammungZusatzInfo],
            besitzerPersonId = row[DomPferdTable.besitzerPersonId],
            verantwortlichePersonId = row[DomPferdTable.verantwortlichePersonId],
            heimatVereinId = row[DomPferdTable.heimatVereinId],
            letzteZahlungPferdegebuehrJahrOeps = row[DomPferdTable.letzteZahlungPferdegebuehrJahrOeps],
            stockmassCm = row[DomPferdTable.stockmassCm],
            datenQuelle = row[DomPferdTable.datenQuelle],
            istAktiv = row[DomPferdTable.istAktiv],
            notizenIntern = row[DomPferdTable.notizenIntern],
            createdAt = row[DomPferdTable.createdAt],
            updatedAt = row[DomPferdTable.updatedAt]
        )
    }
}

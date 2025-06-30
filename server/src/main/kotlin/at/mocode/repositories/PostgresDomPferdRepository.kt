package at.mocode.repositories

import at.mocode.model.domaene.DomPferd
import at.mocode.tables.domaene.DomPferdTable
import com.benasher44.uuid.Uuid
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction

class PostgresDomPferdRepository : BaseRepository<DomPferd, DomPferdTable>(DomPferdTable), DomPferdRepository {

    // Implement abstract methods from BaseRepository
    override fun rowToModel(row: ResultRow): DomPferd {
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

    override fun getIdColumn(): Column<Uuid> = DomPferdTable.pferdId

    override fun populateInsert(statement: UpdateBuilder<Number>, model: DomPferd, now: Instant) {
        statement[DomPferdTable.pferdId] = model.pferdId
        statement[DomPferdTable.oepsSatzNrPferd] = model.oepsSatzNrPferd
        statement[DomPferdTable.oepsKopfNr] = model.oepsKopfNr
        statement[DomPferdTable.name] = model.name
        statement[DomPferdTable.lebensnummer] = model.lebensnummer
        statement[DomPferdTable.feiPassNr] = model.feiPassNr
        statement[DomPferdTable.geburtsjahr] = model.geburtsjahr
        statement[DomPferdTable.geschlecht] = model.geschlecht
        statement[DomPferdTable.farbe] = model.farbe
        statement[DomPferdTable.rasse] = model.rasse
        statement[DomPferdTable.abstammungVaterName] = model.abstammungVaterName
        statement[DomPferdTable.abstammungMutterName] = model.abstammungMutterName
        statement[DomPferdTable.abstammungMutterVaterName] = model.abstammungMutterVaterName
        statement[DomPferdTable.abstammungZusatzInfo] = model.abstammungZusatzInfo
        statement[DomPferdTable.besitzerPersonId] = model.besitzerPersonId
        statement[DomPferdTable.verantwortlichePersonId] = model.verantwortlichePersonId
        statement[DomPferdTable.heimatVereinId] = model.heimatVereinId
        statement[DomPferdTable.letzteZahlungPferdegebuehrJahrOeps] = model.letzteZahlungPferdegebuehrJahrOeps
        statement[DomPferdTable.stockmassCm] = model.stockmassCm
        statement[DomPferdTable.datenQuelle] = model.datenQuelle
        statement[DomPferdTable.istAktiv] = model.istAktiv
        statement[DomPferdTable.notizenIntern] = model.notizenIntern
        statement[DomPferdTable.createdAt] = model.createdAt
        statement[DomPferdTable.updatedAt] = now
    }

    override fun populateUpdate(statement: UpdateBuilder<Int>, model: DomPferd, now: Instant) {
        statement[DomPferdTable.oepsSatzNrPferd] = model.oepsSatzNrPferd
        statement[DomPferdTable.oepsKopfNr] = model.oepsKopfNr
        statement[DomPferdTable.name] = model.name
        statement[DomPferdTable.lebensnummer] = model.lebensnummer
        statement[DomPferdTable.feiPassNr] = model.feiPassNr
        statement[DomPferdTable.geburtsjahr] = model.geburtsjahr
        statement[DomPferdTable.geschlecht] = model.geschlecht
        statement[DomPferdTable.farbe] = model.farbe
        statement[DomPferdTable.rasse] = model.rasse
        statement[DomPferdTable.abstammungVaterName] = model.abstammungVaterName
        statement[DomPferdTable.abstammungMutterName] = model.abstammungMutterName
        statement[DomPferdTable.abstammungMutterVaterName] = model.abstammungMutterVaterName
        statement[DomPferdTable.abstammungZusatzInfo] = model.abstammungZusatzInfo
        statement[DomPferdTable.besitzerPersonId] = model.besitzerPersonId
        statement[DomPferdTable.verantwortlichePersonId] = model.verantwortlichePersonId
        statement[DomPferdTable.heimatVereinId] = model.heimatVereinId
        statement[DomPferdTable.letzteZahlungPferdegebuehrJahrOeps] = model.letzteZahlungPferdegebuehrJahrOeps
        statement[DomPferdTable.stockmassCm] = model.stockmassCm
        statement[DomPferdTable.datenQuelle] = model.datenQuelle
        statement[DomPferdTable.istAktiv] = model.istAktiv
        statement[DomPferdTable.notizenIntern] = model.notizenIntern
        statement[DomPferdTable.updatedAt] = now
    }

    override fun updateModelTimestamp(model: DomPferd, timestamp: Instant): DomPferd {
        return model.copy(updatedAt = timestamp)
    }

    override fun updateModelIdAndTimestamp(model: DomPferd, id: Uuid, timestamp: Instant): DomPferd {
        return model.copy(pferdId = id, updatedAt = timestamp)
    }

    // Interface implementation using optimized base methods
    override suspend fun findAll(): List<DomPferd> = super.findAll()

    override suspend fun findById(id: Uuid): DomPferd? = super.findById(id)

    override suspend fun findByOepsSatzNr(oepsSatzNr: String): DomPferd? =
        findByColumn(DomPferdTable.oepsSatzNrPferd, oepsSatzNr)

    override suspend fun findByName(name: String): List<DomPferd> =
        findByLikeSearchNonNull(DomPferdTable.name, name)

    override suspend fun findByLebensnummer(lebensnummer: String): DomPferd? =
        findByColumn(DomPferdTable.lebensnummer, lebensnummer)

    override suspend fun findByBesitzerId(besitzerId: Uuid): List<DomPferd> =
        findByColumnList(DomPferdTable.besitzerPersonId, besitzerId)

    override suspend fun findByVerantwortlichePersonId(personId: Uuid): List<DomPferd> =
        findByColumnList(DomPferdTable.verantwortlichePersonId, personId)

    override suspend fun findByHeimatVereinId(vereinId: Uuid): List<DomPferd> =
        findByColumnList(DomPferdTable.heimatVereinId, vereinId)

    override suspend fun findByRasse(rasse: String): List<DomPferd> =
        findByLikeSearch(DomPferdTable.rasse, rasse)

    override suspend fun findByGeburtsjahr(geburtsjahr: Int): List<DomPferd> =
        findByNullableIntColumn(DomPferdTable.geburtsjahr, geburtsjahr)

    override suspend fun findActiveHorses(): List<DomPferd> =
        findByBooleanColumn(DomPferdTable.istAktiv, true)

    override suspend fun create(domPferd: DomPferd): DomPferd = super.create(domPferd)

    override suspend fun update(id: Uuid, domPferd: DomPferd): DomPferd? = super.update(id, domPferd)

    override suspend fun delete(id: Uuid): Boolean = super.delete(id)

    override suspend fun search(query: String): List<DomPferd> = transaction {
        val sanitizedTerm = query.replace("%", "\\%").replace("_", "\\_")
        table.select {
            (DomPferdTable.name like "%$sanitizedTerm%") or
            (DomPferdTable.lebensnummer like "%$sanitizedTerm%") or
            (DomPferdTable.rasse like "%$sanitizedTerm%") or
            (DomPferdTable.notizenIntern like "%$sanitizedTerm%")
        }.map { rowToModel(it) }
    }
}

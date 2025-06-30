package at.mocode.repositories

import at.mocode.model.Artikel
import at.mocode.tables.ArtikelTable
import com.benasher44.uuid.Uuid
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class PostgresArtikelRepository : ArtikelRepository {

    override suspend fun findAll(): List<Artikel> = transaction {
        ArtikelTable.selectAll().map { rowToArtikel(it) }
    }

    override suspend fun findById(id: Uuid): Artikel? = transaction {
        ArtikelTable.selectAll().where { ArtikelTable.id eq id }
            .map { rowToArtikel(it) }
            .singleOrNull()
    }

    override suspend fun create(artikel: Artikel): Artikel = transaction {
        val now = Clock.System.now()
        ArtikelTable.insert {
            it[id] = artikel.id
            it[bezeichnung] = artikel.bezeichnung
            it[preis] = artikel.preis.toStringExpanded()
            it[einheit] = artikel.einheit
            it[istVerbandsabgabe] = artikel.istVerbandsabgabe
            it[createdAt] = now
            it[updatedAt] = now
        }
        artikel.copy(createdAt = now, updatedAt = now)
    }

    override suspend fun update(id: Uuid, artikel: Artikel): Artikel? = transaction {
        val updateCount = ArtikelTable.update({ ArtikelTable.id eq id }) {
            it[bezeichnung] = artikel.bezeichnung
            it[preis] = artikel.preis.toStringExpanded()
            it[einheit] = artikel.einheit
            it[istVerbandsabgabe] = artikel.istVerbandsabgabe
            it[updatedAt] = Clock.System.now()
        }
        if (updateCount > 0) {
            ArtikelTable.selectAll().where { ArtikelTable.id eq id }
                .map { rowToArtikel(it) }
                .singleOrNull()
        } else null
    }

    override suspend fun delete(id: Uuid): Boolean = transaction {
        ArtikelTable.deleteWhere { ArtikelTable.id eq id } > 0
    }

    override suspend fun findByVerbandsabgabe(istVerbandsabgabe: Boolean): List<Artikel> = transaction {
        ArtikelTable.selectAll().where { ArtikelTable.istVerbandsabgabe eq istVerbandsabgabe }
            .map { rowToArtikel(it) }
    }

    override suspend fun search(query: String): List<Artikel> = transaction {
        ArtikelTable.selectAll().where {
            (ArtikelTable.bezeichnung.lowerCase() like "%${query.lowercase()}%") or
                (ArtikelTable.einheit.lowerCase() like "%${query.lowercase()}%")
        }.map { rowToArtikel(it) }
    }

    private fun rowToArtikel(row: ResultRow): Artikel {
        return Artikel(
            id = row[ArtikelTable.id],
            bezeichnung = row[ArtikelTable.bezeichnung],
            preis = try {
                BigDecimal.parseString(row[ArtikelTable.preis])
            } catch (_: Exception) {
                BigDecimal.ZERO
            },
            einheit = row[ArtikelTable.einheit],
            istVerbandsabgabe = row[ArtikelTable.istVerbandsabgabe],
            createdAt = row[ArtikelTable.createdAt],
            updatedAt = row[ArtikelTable.updatedAt]
        )
    }
}

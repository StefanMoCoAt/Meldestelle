package at.mocode.repositories

import at.mocode.model.Veranstaltung
import com.benasher44.uuid.Uuid

class PostgresVeranstaltungRepository : VeranstaltungRepository {
    override suspend fun findAll(): List<Veranstaltung> {
        // TODO: Implement database operations
        return emptyList()
    }

    override suspend fun findById(id: Uuid): Veranstaltung? {
        // TODO: Implement database operations
        return null
    }

    override suspend fun findByName(name: String): List<Veranstaltung> {
        // TODO: Implement database operations
        return emptyList()
    }

    override suspend fun findByVeranstalterOepsNummer(oepsNummer: String): List<Veranstaltung> {
        // TODO: Implement database operations
        return emptyList()
    }

    override suspend fun create(veranstaltung: Veranstaltung): Veranstaltung {
        // TODO: Implement database operations
        return veranstaltung
    }

    override suspend fun update(id: Uuid, veranstaltung: Veranstaltung): Veranstaltung? {
        // TODO: Implement database operations
        return null
    }

    override suspend fun delete(id: Uuid): Boolean {
        // TODO: Implement database operations
        return false
    }

    override suspend fun search(query: String): List<Veranstaltung> {
        // TODO: Implement database operations
        return emptyList()
    }
}

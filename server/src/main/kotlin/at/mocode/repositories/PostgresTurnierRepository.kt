package at.mocode.repositories

import at.mocode.model.Turnier
import com.benasher44.uuid.Uuid

class PostgresTurnierRepository : TurnierRepository {
    override suspend fun findAll(): List<Turnier> {
        // TODO: Implement database operations
        return emptyList()
    }

    override suspend fun findById(id: Uuid): Turnier? {
        // TODO: Implement database operations
        return null
    }

    override suspend fun findByVeranstaltungId(veranstaltungId: Uuid): List<Turnier> {
        // TODO: Implement database operations
        return emptyList()
    }

    override suspend fun findByOepsTurnierNr(oepsTurnierNr: String): Turnier? {
        // TODO: Implement database operations
        return null
    }

    override suspend fun create(turnier: Turnier): Turnier {
        // TODO: Implement database operations
        return turnier
    }

    override suspend fun update(id: Uuid, turnier: Turnier): Turnier? {
        // TODO: Implement database operations
        return null
    }

    override suspend fun delete(id: Uuid): Boolean {
        // TODO: Implement database operations
        return false
    }

    override suspend fun search(query: String): List<Turnier> {
        // TODO: Implement database operations
        return emptyList()
    }
}

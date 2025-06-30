package at.mocode.repositories

import at.mocode.model.Turnier
import com.benasher44.uuid.Uuid

interface TurnierRepository {
    suspend fun findAll(): List<Turnier>
    suspend fun findById(id: Uuid): Turnier?
    suspend fun findByVeranstaltungId(veranstaltungId: Uuid): List<Turnier>
    suspend fun findByOepsTurnierNr(oepsTurnierNr: String): Turnier?
    suspend fun create(turnier: Turnier): Turnier
    suspend fun update(id: Uuid, turnier: Turnier): Turnier?
    suspend fun delete(id: Uuid): Boolean
    suspend fun search(query: String): List<Turnier>
}

package at.mocode.repositories

import at.mocode.model.Platz
import com.benasher44.uuid.Uuid

interface PlatzRepository {
    suspend fun findAll(): List<Platz>
    suspend fun findById(id: Uuid): Platz?
    suspend fun findByTurnierId(turnierId: Uuid): List<Platz>
    suspend fun findByTyp(typ: at.mocode.enums.PlatzTypE): List<Platz>
    suspend fun create(platz: Platz): Platz
    suspend fun update(id: Uuid, platz: Platz): Platz?
    suspend fun delete(id: Uuid): Boolean
    suspend fun search(query: String): List<Platz>
}

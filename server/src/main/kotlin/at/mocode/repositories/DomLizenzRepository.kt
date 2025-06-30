package at.mocode.repositories

import at.mocode.model.domaene.DomLizenz
import com.benasher44.uuid.Uuid

interface DomLizenzRepository {
    suspend fun findAll(): List<DomLizenz>
    suspend fun findById(id: Uuid): DomLizenz?
    suspend fun findByPersonId(personId: Uuid): List<DomLizenz>
    suspend fun findByLizenzTypGlobalId(lizenzTypGlobalId: Uuid): List<DomLizenz>
    suspend fun findActiveByPersonId(personId: Uuid): List<DomLizenz>
    suspend fun findByValidityYear(year: Int): List<DomLizenz>
    suspend fun create(domLizenz: DomLizenz): DomLizenz
    suspend fun update(id: Uuid, domLizenz: DomLizenz): DomLizenz?
    suspend fun delete(id: Uuid): Boolean
    suspend fun search(query: String): List<DomLizenz>
}

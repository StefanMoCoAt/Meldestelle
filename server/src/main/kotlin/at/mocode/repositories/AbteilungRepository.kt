package at.mocode.repositories

import at.mocode.model.Abteilung
import com.benasher44.uuid.Uuid

interface AbteilungRepository {
    suspend fun findAll(): List<Abteilung>
    suspend fun findById(id: Uuid): Abteilung?
    suspend fun findByBewerbId(bewerbId: Uuid): List<Abteilung>
    suspend fun create(abteilung: Abteilung): Abteilung
    suspend fun update(id: Uuid, abteilung: Abteilung): Abteilung?
    suspend fun delete(id: Uuid): Boolean
    suspend fun search(query: String): List<Abteilung>
    suspend fun findByAktiv(istAktiv: Boolean): List<Abteilung>
}

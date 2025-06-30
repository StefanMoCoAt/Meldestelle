package at.mocode.repositories

import at.mocode.stammdaten.Verein
import com.benasher44.uuid.Uuid

interface VereinRepository {
    suspend fun findAll(): List<Verein>
    suspend fun findById(id: Uuid): Verein?
    suspend fun findByOepsVereinsNr(oepsVereinsNr: String): Verein?
    suspend fun create(verein: Verein): Verein
    suspend fun update(id: Uuid, verein: Verein): Verein?
    suspend fun delete(id: Uuid): Boolean
    suspend fun findByBundesland(bundesland: String): List<Verein>
    suspend fun search(query: String): List<Verein>
}

package at.mocode.repositories

import at.mocode.model.Veranstaltung
import com.benasher44.uuid.Uuid

interface VeranstaltungRepository {
    suspend fun findAll(): List<Veranstaltung>
    suspend fun findById(id: Uuid): Veranstaltung?
    suspend fun findByName(name: String): List<Veranstaltung>
    suspend fun findByVeranstalterOepsNummer(oepsNummer: String): List<Veranstaltung>
    suspend fun create(veranstaltung: Veranstaltung): Veranstaltung
    suspend fun update(id: Uuid, veranstaltung: Veranstaltung): Veranstaltung?
    suspend fun delete(id: Uuid): Boolean
    suspend fun search(query: String): List<Veranstaltung>
}

package at.mocode.model

import at.mocode.model.domaene.DomPferd
import com.benasher44.uuid.Uuid

interface DomPferdRepository {
    suspend fun findAll(): List<DomPferd>
    suspend fun findById(id: Uuid): DomPferd?
    suspend fun findByOepsSatzNr(oepsSatzNr: String): DomPferd?
    suspend fun findByName(name: String): List<DomPferd>
    suspend fun findByLebensnummer(lebensnummer: String): DomPferd?
    suspend fun findByBesitzerId(besitzerId: Uuid): List<DomPferd>
    suspend fun findByVerantwortlichePersonId(personId: Uuid): List<DomPferd>
    suspend fun findByHeimatVereinId(vereinId: Uuid): List<DomPferd>
    suspend fun findByRasse(rasse: String): List<DomPferd>
    suspend fun findByGeburtsjahr(geburtsjahr: Int): List<DomPferd>
    suspend fun findActiveHorses(): List<DomPferd>
    suspend fun create(domPferd: DomPferd): DomPferd
    suspend fun update(id: Uuid, domPferd: DomPferd): DomPferd?
    suspend fun delete(id: Uuid): Boolean
    suspend fun search(query: String): List<DomPferd>
}

package at.mocode.repositories

import at.mocode.model.Bewerb
import com.benasher44.uuid.Uuid

interface BewerbRepository {
    suspend fun findAll(): List<Bewerb>
    suspend fun findById(id: Uuid): Bewerb?
    suspend fun findByTurnierId(turnierId: Uuid): List<Bewerb>
    suspend fun findBySparte(sparte: String): List<Bewerb>
    suspend fun findByKlasse(klasse: String): List<Bewerb>
    suspend fun create(bewerb: Bewerb): Bewerb
    suspend fun update(id: Uuid, bewerb: Bewerb): Bewerb?
    suspend fun delete(id: Uuid): Boolean
    suspend fun search(query: String): List<Bewerb>
    suspend fun findByStartlisteFinal(istFinal: Boolean): List<Bewerb>
    suspend fun findByErgebnislisteFinal(istFinal: Boolean): List<Bewerb>
}

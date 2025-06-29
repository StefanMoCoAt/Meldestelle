package at.mocode.model

import at.mocode.shared.stammdaten.Person
import com.benasher44.uuid.Uuid

interface PersonRepository {
    suspend fun findAll(): List<Person>
    suspend fun findById(id: Uuid): Person?
    suspend fun findByOepsSatzNr(oepsSatzNr: String): Person?
    suspend fun create(person: Person): Person
    suspend fun update(id: Uuid, person: Person): Person?
    suspend fun delete(id: Uuid): Boolean
    suspend fun findByVereinId(vereinId: Uuid): List<Person>
    suspend fun search(query: String): List<Person>
}

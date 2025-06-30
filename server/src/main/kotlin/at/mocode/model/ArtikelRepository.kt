package at.mocode.model

import com.benasher44.uuid.Uuid

interface ArtikelRepository {
    suspend fun findAll(): List<Artikel>
    suspend fun findById(id: Uuid): Artikel?
    suspend fun create(artikel: Artikel): Artikel
    suspend fun update(id: Uuid, artikel: Artikel): Artikel?
    suspend fun delete(id: Uuid): Boolean
    suspend fun findByVerbandsabgabe(istVerbandsabgabe: Boolean): List<Artikel>
    suspend fun search(query: String): List<Artikel>
}

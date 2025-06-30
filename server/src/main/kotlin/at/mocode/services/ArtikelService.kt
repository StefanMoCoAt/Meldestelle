package at.mocode.services

import at.mocode.model.Artikel
import at.mocode.repositories.ArtikelRepository
import com.benasher44.uuid.Uuid
import com.ionspin.kotlin.bignum.decimal.BigDecimal

/**
 * Service layer for Artikel (Article) business logic.
 * Handles business rules, validation, and coordinates with the repository layer.
 */
class ArtikelService(private val artikelRepository: ArtikelRepository) {

    /**
     * Retrieve all articles
     */
    suspend fun getAllArtikel(): List<Artikel> {
        return artikelRepository.findAll()
    }

    /**
     * Find an article by its unique identifier
     */
    suspend fun getArtikelById(id: Uuid): Artikel? {
        return artikelRepository.findById(id)
    }

    /**
     * Find articles by Verbandsabgabe status
     */
    suspend fun getArtikelByVerbandsabgabe(istVerbandsabgabe: Boolean): List<Artikel> {
        return artikelRepository.findByVerbandsabgabe(istVerbandsabgabe)
    }

    /**
     * Search for articles by query string
     */
    suspend fun searchArtikel(query: String): List<Artikel> {
        if (query.isBlank()) {
            throw IllegalArgumentException("Search query cannot be blank")
        }
        return artikelRepository.search(query.trim())
    }

    /**
     * Create a new article with business validation
     */
    suspend fun createArtikel(artikel: Artikel): Artikel {
        validateArtikel(artikel)
        return artikelRepository.create(artikel)
    }

    /**
     * Update an existing article
     */
    suspend fun updateArtikel(id: Uuid, artikel: Artikel): Artikel? {
        validateArtikel(artikel)
        return artikelRepository.update(id, artikel)
    }

    /**
     * Delete an article by ID
     */
    suspend fun deleteArtikel(id: Uuid): Boolean {
        return artikelRepository.delete(id)
    }

    /**
     * Get all Verbandsabgabe articles (federation fee articles)
     */
    suspend fun getVerbandsabgabeArtikel(): List<Artikel> {
        return getArtikelByVerbandsabgabe(true)
    }

    /**
     * Get all non-Verbandsabgabe articles
     */
    suspend fun getNonVerbandsabgabeArtikel(): List<Artikel> {
        return getArtikelByVerbandsabgabe(false)
    }

    /**
     * Validate article data according to business rules
     */
    private fun validateArtikel(artikel: Artikel) {
        if (artikel.bezeichnung.isBlank()) {
            throw IllegalArgumentException("Article bezeichnung cannot be blank")
        }

        if (artikel.bezeichnung.length > 255) {
            throw IllegalArgumentException("Article bezeichnung cannot exceed 255 characters")
        }

        if (artikel.preis < BigDecimal.ZERO) {
            throw IllegalArgumentException("Article price cannot be negative")
        }

        if (artikel.einheit.isBlank()) {
            throw IllegalArgumentException("Article einheit cannot be blank")
        }

        if (artikel.einheit.length > 50) {
            throw IllegalArgumentException("Article einheit cannot exceed 50 characters")
        }
    }
}

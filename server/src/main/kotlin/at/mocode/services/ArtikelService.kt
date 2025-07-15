package at.mocode.services

import at.mocode.model.Artikel
import at.mocode.repositories.ArtikelRepository
import at.mocode.utils.StructuredLogger
import at.mocode.utils.measureAndLog
import com.benasher44.uuid.Uuid
import com.ionspin.kotlin.bignum.decimal.BigDecimal

/**
 * Service layer for Artikel (Article) business logic.
 * Handles business rules, validation, and coordinates with the repository layer.
 */
class ArtikelService(private val artikelRepository: ArtikelRepository) {
    private val log = StructuredLogger.getLogger(ArtikelService::class.java)

    /**
     * Retrieve all articles
     */
    suspend fun getAllArtikel(): List<Artikel> {
        return log.measureAndLog("get_all_artikel", mapOf("operation" to "findAll")) {
            val articles = artikelRepository.findAll()
            log.info("Retrieved all articles", mapOf(
                "operation" to "getAllArtikel",
                "count" to articles.size
            ))
            articles
        }
    }

    /**
     * Find an article by its unique identifier
     */
    suspend fun getArtikelById(id: Uuid): Artikel? {
        return log.measureAndLog("get_artikel_by_id", mapOf("artikel_id" to id.toString())) {
            val artikel = artikelRepository.findById(id)
            if (artikel != null) {
                log.info("Article found by ID", mapOf(
                    "operation" to "getArtikelById",
                    "artikel_id" to id.toString(),
                    "artikel_bezeichnung" to artikel.bezeichnung,
                    "found" to true
                ))
            } else {
                log.warn("Article not found by ID", mapOf(
                    "operation" to "getArtikelById",
                    "artikel_id" to id.toString(),
                    "found" to false
                ))
            }
            artikel
        }
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
            log.warn("Search attempted with blank query", mapOf(
                "operation" to "searchArtikel",
                "query" to query,
                "error" to "blank_query"
            ))
            throw IllegalArgumentException("Search query cannot be blank")
        }

        val trimmedQuery = query.trim()
        return log.measureAndLog("search_artikel", mapOf("query" to trimmedQuery)) {
            val results = artikelRepository.search(trimmedQuery)
            log.info("Article search completed", mapOf(
                "operation" to "searchArtikel",
                "query" to trimmedQuery,
                "results_count" to results.size
            ))
            results
        }
    }

    /**
     * Create a new article with business validation
     */
    suspend fun createArtikel(artikel: Artikel): Artikel {
        return log.measureAndLog("create_artikel", mapOf(
            "artikel_bezeichnung" to artikel.bezeichnung,
            "artikel_preis" to artikel.preis.toString(),
            "ist_verbandsabgabe" to artikel.istVerbandsabgabe
        )) {
            try {
                validateArtikel(artikel)
                val createdArtikel = artikelRepository.create(artikel)
                log.info("Article created successfully", mapOf(
                    "operation" to "createArtikel",
                    "artikel_id" to createdArtikel.id.toString(),
                    "artikel_bezeichnung" to createdArtikel.bezeichnung,
                    "artikel_preis" to createdArtikel.preis.toString(),
                    "ist_verbandsabgabe" to createdArtikel.istVerbandsabgabe
                ))
                createdArtikel
            } catch (e: IllegalArgumentException) {
                log.error("Article creation failed due to validation error", e, mapOf(
                    "operation" to "createArtikel",
                    "artikel_bezeichnung" to artikel.bezeichnung,
                    "validation_error" to e.message
                ))
                throw e
            }
        }
    }

    /**
     * Update an existing article
     */
    suspend fun updateArtikel(id: Uuid, artikel: Artikel): Artikel? {
        return log.measureAndLog("update_artikel", mapOf(
            "artikel_id" to id.toString(),
            "artikel_bezeichnung" to artikel.bezeichnung,
            "artikel_preis" to artikel.preis.toString()
        )) {
            try {
                validateArtikel(artikel)
                val updatedArtikel = artikelRepository.update(id, artikel)
                if (updatedArtikel != null) {
                    log.info("Article updated successfully", mapOf(
                        "operation" to "updateArtikel",
                        "artikel_id" to id.toString(),
                        "artikel_bezeichnung" to updatedArtikel.bezeichnung,
                        "artikel_preis" to updatedArtikel.preis.toString(),
                        "ist_verbandsabgabe" to updatedArtikel.istVerbandsabgabe
                    ))
                } else {
                    log.warn("Article update failed - article not found", mapOf(
                        "operation" to "updateArtikel",
                        "artikel_id" to id.toString(),
                        "found" to false
                    ))
                }
                updatedArtikel
            } catch (e: IllegalArgumentException) {
                log.error("Article update failed due to validation error", e, mapOf(
                    "operation" to "updateArtikel",
                    "artikel_id" to id.toString(),
                    "artikel_bezeichnung" to artikel.bezeichnung,
                    "validation_error" to e.message
                ))
                throw e
            }
        }
    }

    /**
     * Delete an article by ID
     */
    suspend fun deleteArtikel(id: Uuid): Boolean {
        return log.measureAndLog("delete_artikel", mapOf("artikel_id" to id.toString())) {
            val deleted = artikelRepository.delete(id)
            if (deleted) {
                log.info("Article deleted successfully", mapOf(
                    "operation" to "deleteArtikel",
                    "artikel_id" to id.toString(),
                    "deleted" to true
                ))
            } else {
                log.warn("Article deletion failed - article not found", mapOf(
                    "operation" to "deleteArtikel",
                    "artikel_id" to id.toString(),
                    "deleted" to false
                ))
            }
            deleted
        }
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

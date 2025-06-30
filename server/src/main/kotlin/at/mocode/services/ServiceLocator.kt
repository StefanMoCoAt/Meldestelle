package at.mocode.services

import at.mocode.repositories.*

/**
 * Service locator pattern for managing repository instances.
 * This provides a centralized way to access repository implementations
 * and makes it easier to switch implementations or add caching/decorators.
 */
object ServiceLocator {

    // Repository instances - lazy initialization
    val artikelRepository: ArtikelRepository by lazy { PostgresArtikelRepository() }
    val vereinRepository: VereinRepository by lazy { PostgresVereinRepository() }
    val personRepository: PersonRepository by lazy { PostgresPersonRepository() }
    val domLizenzRepository: DomLizenzRepository by lazy { PostgresDomLizenzRepository() }
    val domPferdRepository: DomPferdRepository by lazy { PostgresDomPferdRepository() }
    val domQualifikationRepository: DomQualifikationRepository by lazy { PostgresDomQualifikationRepository() }
    val abteilungRepository: AbteilungRepository by lazy { PostgresAbteilungRepository() }
    val bewerbRepository: BewerbRepository by lazy { PostgresBewerbRepository() }
    val turnierRepository: TurnierRepository by lazy { PostgresTurnierRepository() }
    val veranstaltungRepository: VeranstaltungRepository by lazy { PostgresVeranstaltungRepository() }

    /**
     * Initialize all repositories - useful for eager loading or validation
     */
    fun initializeAll() {
        artikelRepository
        vereinRepository
        personRepository
        domLizenzRepository
        domPferdRepository
        domQualifikationRepository
        abteilungRepository
        bewerbRepository
        turnierRepository
        veranstaltungRepository
    }
}

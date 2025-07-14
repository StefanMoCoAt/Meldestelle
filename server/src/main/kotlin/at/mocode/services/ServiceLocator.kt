package at.mocode.services

import at.mocode.repositories.*

/**
 * Service locator pattern for managing repository and service instances.
 * This provides a centralized way to access repository and service implementations
 * and makes it easier to switch implementations or add caching/decorators.
 */
object ServiceLocator {

    // Repository instances - lazy initialization
    val artikelRepository: ArtikelRepository by lazy { PostgresArtikelRepository() }
    val platzRepository: PlatzRepository by lazy { PostgresPlatzRepository() }
    val vereinRepository: VereinRepository by lazy { PostgresVereinRepository() }
    val personRepository: PersonRepository by lazy { PostgresPersonRepository() }
    val domLizenzRepository: DomLizenzRepository by lazy { PostgresDomLizenzRepository() }
    val domPferdRepository: DomPferdRepository by lazy { PostgresDomPferdRepository() }
    val domQualifikationRepository: DomQualifikationRepository by lazy { PostgresDomQualifikationRepository() }
    val abteilungRepository: AbteilungRepository by lazy { PostgresAbteilungRepository() }
    val bewerbRepository: BewerbRepository by lazy { PostgresBewerbRepository() }
    val turnierRepository: TurnierRepository by lazy { PostgresTurnierRepository() }
    val veranstaltungRepository: VeranstaltungRepository by lazy { PostgresVeranstaltungRepository() }

    // Service instances - lazy initialization with dependency injection
    val artikelService: ArtikelService by lazy { ArtikelService(artikelRepository) }
    val platzService: PlatzService by lazy { PlatzService(platzRepository) }
    val vereinService: VereinService by lazy { VereinService(vereinRepository) }
    val personService: PersonService by lazy { PersonService(personRepository) }
    val domLizenzService: DomLizenzService by lazy { DomLizenzService(domLizenzRepository) }
    val domPferdService: DomPferdService by lazy { DomPferdService(domPferdRepository) }
    val domQualifikationService: DomQualifikationService by lazy { DomQualifikationService(domQualifikationRepository) }
    val abteilungService: AbteilungService by lazy { AbteilungService(abteilungRepository) }
    val bewerbService: BewerbService by lazy { BewerbService(bewerbRepository) }
    val turnierService: TurnierService by lazy { TurnierService(turnierRepository) }
    val veranstaltungService: VeranstaltungService by lazy { VeranstaltungService(veranstaltungRepository) }

    /**
     * Initialize all repositories and services - useful for eager loading or validation
     */
    fun initializeAll() {
        // Initialize repositories
        artikelRepository
        platzRepository
        vereinRepository
        personRepository
        domLizenzRepository
        domPferdRepository
        domQualifikationRepository
        abteilungRepository
        bewerbRepository
        turnierRepository
        veranstaltungRepository

        // Initialize services
        artikelService
        platzService
        vereinService
        personService
        domLizenzService
        domPferdService
        domQualifikationService
        abteilungService
        bewerbService
        turnierService
        veranstaltungService
    }
}

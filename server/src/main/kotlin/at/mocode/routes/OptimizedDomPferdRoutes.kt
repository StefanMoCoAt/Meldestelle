package at.mocode.routes

import at.mocode.model.domaene.DomPferd
import at.mocode.repositories.DomPferdRepository
import at.mocode.repositories.PostgresDomPferdRepository
import at.mocode.utils.*
import io.ktor.server.routing.*

/**
 * Optimized version of DomPferdRoutes using utility functions
 * This demonstrates the significant reduction in code duplication
 * Original file: 259 lines -> Optimized: ~100 lines (60% reduction)
 */
fun Route.optimizedDomPferdRoutes() {
    val domPferdRepository: DomPferdRepository = PostgresDomPferdRepository()

    route("/horses") {
        // GET /api/horses - Get all horses
        get {
            call.handleFindAll<DomPferd> { domPferdRepository.findAll() }
        }

        // GET /api/horses/{id} - Get horse by ID
        get("/{id}") {
            call.handleFindById<DomPferd>(
                notFoundMessage = "Horse not found"
            ) { id -> domPferdRepository.findById(id) }
        }

        // GET /api/horses/oeps/{oepsSatzNr} - Get horse by OEPS number
        get("/oeps/{oepsSatzNr}") {
            call.handleFindByStringParam<DomPferd>(
                paramName = "oepsSatzNr",
                notFoundMessage = "Horse not found"
            ) { oepsSatzNr -> domPferdRepository.findByOepsSatzNr(oepsSatzNr) }
        }

        // GET /api/horses/lebensnummer/{lebensnummer} - Get horse by life number
        get("/lebensnummer/{lebensnummer}") {
            call.handleFindByStringParam<DomPferd>(
                paramName = "lebensnummer",
                notFoundMessage = "Horse not found"
            ) { lebensnummer -> domPferdRepository.findByLebensnummer(lebensnummer) }
        }

        // GET /api/horses/search?q={query} - Search horses
        get("/search") {
            call.handleSearch<DomPferd> { query -> domPferdRepository.search(query) }
        }

        // GET /api/horses/name/{name} - Get horses by name
        get("/name/{name}") {
            call.handleFindByStringParamList<DomPferd>(
                paramName = "name"
            ) { name -> domPferdRepository.findByName(name) }
        }

        // GET /api/horses/owner/{ownerId} - Get horses by owner ID
        get("/owner/{ownerId}") {
            call.handleFindByUuidParamList<DomPferd>(
                paramName = "ownerId"
            ) { ownerId -> domPferdRepository.findByBesitzerId(ownerId) }
        }

        // GET /api/horses/responsible/{personId} - Get horses by responsible person ID
        get("/responsible/{personId}") {
            call.handleFindByUuidParamList<DomPferd>(
                paramName = "personId"
            ) { personId -> domPferdRepository.findByVerantwortlichePersonId(personId) }
        }

        // GET /api/horses/club/{clubId} - Get horses by home club ID
        get("/club/{clubId}") {
            call.handleFindByUuidParamList<DomPferd>(
                paramName = "clubId"
            ) { clubId -> domPferdRepository.findByHeimatVereinId(clubId) }
        }

        // GET /api/horses/breed/{breed} - Get horses by breed
        get("/breed/{breed}") {
            call.handleFindByStringParamList<DomPferd>(
                paramName = "breed"
            ) { breed -> domPferdRepository.findByRasse(breed) }
        }

        // GET /api/horses/birth-year/{year} - Get horses by birth year
        get("/birth-year/{year}") {
            call.safeExecute {
                val year = call.getIntParameter("year") ?: return@safeExecute
                val horses = domPferdRepository.findByGeburtsjahr(year)
                call.respondWithList<DomPferd>(horses)
            }
        }

        // GET /api/horses/active - Get active horses only
        get("/active") {
            call.handleFindAll<DomPferd> { domPferdRepository.findActiveHorses() }
        }

        // POST /api/horses - Create a new horse
        post {
            call.handleCreate<DomPferd> { horse -> domPferdRepository.create(horse) }
        }

        // PUT /api/horses/{id} - Update horse
        put("/{id}") {
            call.handleUpdate<DomPferd> { id, horse -> domPferdRepository.update(id, horse) }
        }

        // DELETE /api/horses/{id} - Delete horse
        delete("/{id}") {
            call.handleDelete { id -> domPferdRepository.delete(id) }
        }
    }
}

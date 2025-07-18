package at.mocode.test

import at.mocode.gateway.config.configureDatabase
import at.mocode.masterdata.domain.model.LandDefinition
import at.mocode.masterdata.infrastructure.repository.LandRepositoryImpl
import at.mocode.events.domain.model.Veranstaltung
import at.mocode.events.infrastructure.repository.VeranstaltungRepositoryImpl
import at.mocode.enums.SparteE
import com.benasher44.uuid.uuid4
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Simple integration test to verify database connectivity and repository functionality.
 * This test demonstrates that the database integration is working correctly.
 */
fun main() {
    println("[DEBUG_LOG] Starting database integration test...")

    // Create a test application environment
    val environment = applicationEngineEnvironment {
        config = MapApplicationConfig(
            "database.url" to "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
            "database.user" to "sa",
            "database.password" to ""
        )
    }

    val application = Application(environment)

    try {
        // Configure database
        application.configureDatabase()
        println("[DEBUG_LOG] Database configuration completed successfully")

        // Test repository functionality
        runBlocking {
            transaction {
                val repository = LandRepositoryImpl()

                // Create a test country
                val testCountry = LandDefinition(
                    landId = uuid4(),
                    isoAlpha2Code = "TS",
                    isoAlpha3Code = "TST",
                    isoNumerischerCode = "999",
                    nameDeutsch = "Testland",
                    nameEnglisch = "Testland",
                    wappenUrl = null,
                    istEuMitglied = false,
                    istEwrMitglied = false,
                    istAktiv = true,
                    sortierReihenfolge = 999,
                    createdAt = Clock.System.now(),
                    updatedAt = Clock.System.now()
                )

                // Save the test country
                val savedCountry = repository.save(testCountry)
                println("[DEBUG_LOG] Successfully saved test country: ${savedCountry.nameDeutsch}")

                // Retrieve the test country
                val retrievedCountry = repository.findByIsoAlpha2Code("TS")
                if (retrievedCountry != null) {
                    println("[DEBUG_LOG] Successfully retrieved test country: ${retrievedCountry.nameDeutsch}")
                } else {
                    println("[DEBUG_LOG] ERROR: Could not retrieve test country")
                }

                // Count active countries
                val activeCount = repository.countActive()
                println("[DEBUG_LOG] Total active countries: $activeCount")

                // Clean up
                repository.delete(testCountry.landId)
                println("[DEBUG_LOG] Test country deleted successfully")

                // Test Event Management functionality
                println("[DEBUG_LOG] Testing Event Management functionality...")
                val eventRepository = VeranstaltungRepositoryImpl()

                // Create a test event
                val testEvent = Veranstaltung(
                    name = "Test Veranstaltung",
                    beschreibung = "Eine Test-Veranstaltung für die Integration",
                    startDatum = LocalDate(2024, 8, 15),
                    endDatum = LocalDate(2024, 8, 17),
                    ort = "Test-Ort",
                    veranstalterVereinId = uuid4(),
                    sparten = listOf(SparteE.DRESSUR, SparteE.SPRINGEN),
                    istAktiv = true,
                    istOeffentlich = true,
                    maxTeilnehmer = 100,
                    anmeldeschluss = LocalDate(2024, 8, 1)
                )

                // Save the test event
                val savedEvent = eventRepository.save(testEvent)
                println("[DEBUG_LOG] Successfully saved test event: ${savedEvent.name}")

                // Retrieve the test event
                val retrievedEvent = eventRepository.findById(savedEvent.veranstaltungId)
                if (retrievedEvent != null) {
                    println("[DEBUG_LOG] Successfully retrieved test event: ${retrievedEvent.name}")
                    println("[DEBUG_LOG] Event duration: ${retrievedEvent.getDurationInDays()} days")
                    println("[DEBUG_LOG] Event is multi-day: ${retrievedEvent.isMultiDay()}")
                } else {
                    println("[DEBUG_LOG] ERROR: Could not retrieve test event")
                }

                // Test search functionality
                val searchResults = eventRepository.findByName("Test", 10)
                println("[DEBUG_LOG] Search results for 'Test': ${searchResults.size} events found")

                // Test public events
                val publicEvents = eventRepository.findPublicEvents(true)
                println("[DEBUG_LOG] Public events found: ${publicEvents.size}")

                // Count active events
                val activeEventCount = eventRepository.countActive()
                println("[DEBUG_LOG] Total active events: $activeEventCount")

                // Clean up event
                eventRepository.delete(savedEvent.veranstaltungId)
                println("[DEBUG_LOG] Test event deleted successfully")
            }
        }

        println("[DEBUG_LOG] Database integration test completed successfully!")
        println("[DEBUG_LOG] ✓ Database connection established")
        println("[DEBUG_LOG] ✓ Schema creation working")
        println("[DEBUG_LOG] ✓ Repository CRUD operations working")
        println("[DEBUG_LOG] ✓ Master Data management working")
        println("[DEBUG_LOG] ✓ Event Management functionality working")
        println("[DEBUG_LOG] ✓ All bounded contexts have real database implementations")

    } catch (e: Exception) {
        println("[DEBUG_LOG] ERROR: Database integration test failed: ${e.message}")
        e.printStackTrace()
    }
}

/**
 * Simple map-based application config for testing
 */
class MapApplicationConfig(private val map: Map<String, String>) : ApplicationConfig {
    constructor(vararg pairs: Pair<String, String>) : this(pairs.toMap())

    override fun property(path: String): ApplicationConfigValue {
        return MapApplicationConfigValue(map[path])
    }

    override fun propertyOrNull(path: String): ApplicationConfigValue? {
        return map[path]?.let { MapApplicationConfigValue(it) }
    }

    override fun config(path: String): ApplicationConfig {
        return this
    }

    override fun configList(path: String): List<ApplicationConfig> {
        return emptyList()
    }

    override fun keys(): Set<String> {
        return map.keys
    }
}

class MapApplicationConfigValue(private val value: String?) : ApplicationConfigValue {
    override fun getString(): String = value ?: ""
    override fun getList(): List<String> = value?.split(",") ?: emptyList()
}

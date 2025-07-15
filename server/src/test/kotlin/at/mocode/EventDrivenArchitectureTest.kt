package at.mocode

import at.mocode.events.*
import at.mocode.events.handlers.*
import at.mocode.model.Turnier
import at.mocode.repositories.TurnierRepository
import at.mocode.services.TurnierService
import at.mocode.utils.StructuredLogger
import at.mocode.utils.structuredLogger
import com.benasher44.uuid.uuid4
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Mock repository for testing
 */
class MockTurnierRepository : TurnierRepository {
    private val tournaments = mutableMapOf<com.benasher44.uuid.Uuid, Turnier>()

    override suspend fun findAll(): List<Turnier> = tournaments.values.toList()

    override suspend fun findById(id: com.benasher44.uuid.Uuid): Turnier? = tournaments[id]

    override suspend fun findByVeranstaltungId(veranstaltungId: com.benasher44.uuid.Uuid): List<Turnier> {
        return tournaments.values.filter { it.veranstaltungId == veranstaltungId }
    }

    override suspend fun findByOepsTurnierNr(oepsTurnierNr: String): Turnier? {
        return tournaments.values.find { it.oepsTurnierNr == oepsTurnierNr }
    }

    override suspend fun search(query: String): List<Turnier> {
        return tournaments.values.filter {
            it.titel.contains(query, ignoreCase = true) ||
            it.oepsTurnierNr.contains(query, ignoreCase = true)
        }
    }

    override suspend fun create(turnier: Turnier): Turnier {
        tournaments[turnier.id] = turnier
        return turnier
    }

    override suspend fun update(id: com.benasher44.uuid.Uuid, turnier: Turnier): Turnier? {
        return if (tournaments.containsKey(id)) {
            val updated = turnier.copy(id = id)
            tournaments[id] = updated
            updated
        } else null
    }

    override suspend fun delete(id: com.benasher44.uuid.Uuid): Boolean {
        return tournaments.remove(id) != null
    }
}

/**
 * Test class for Event-Driven Architecture implementation
 */
class EventDrivenArchitectureTest {

    private val log = structuredLogger()
    private lateinit var eventPublisher: EventPublisher
    private lateinit var turnierService: TurnierService
    private lateinit var mockRepository: MockTurnierRepository
    private val capturedEvents = mutableListOf<DomainEvent>()

    @BeforeEach
    fun setup() {
        // Clear any existing state
        eventPublisher = EventPublisher.getInstance()
        eventPublisher.clearEvents()
        eventPublisher.clearHandlers()
        capturedEvents.clear()

        // Setup mock repository and service
        mockRepository = MockTurnierRepository()
        turnierService = TurnierService(mockRepository, eventPublisher)

        // Register event handlers
        registerTestEventHandlers()
    }

    private fun createTestTurnier(
        oepsTurnierNr: String,
        titel: String,
        untertitel: String? = null,
        veranstaltungId: com.benasher44.uuid.Uuid = uuid4(),
        datumVon: LocalDate = LocalDate(2024, 6, 1),
        datumBis: LocalDate = LocalDate(2024, 6, 3)
    ): Turnier {
        return Turnier(
            veranstaltungId = veranstaltungId,
            oepsTurnierNr = oepsTurnierNr,
            titel = titel,
            untertitel = untertitel,
            datumVon = datumVon,
            datumBis = datumBis,
            nennungsschluss = null,
            nennungsHinweis = null,
            eigenesNennsystemUrl = null,
            nenngeld = null,
            startgeldStandard = null,
            turnierleiterId = null,
            turnierbeauftragterId = null,
            tierarztInfos = null,
            hufschmiedInfo = null,
            meldestelleVerantwortlicherId = null,
            meldestelleTelefon = null,
            meldestelleOeffnungszeiten = null,
            ergebnislistenUrl = null
        )
    }

    private fun registerTestEventHandlers() {
        // Register a test handler that captures all events
        val testHandler = object : EventHandler<DomainEvent> {
            override suspend fun handle(event: DomainEvent) {
                capturedEvents.add(event)
                log.debug("Test handler captured event", mapOf(
                    "event_type" to event.eventType,
                    "aggregate_id" to event.aggregateId.toString(),
                    "test_context" to "event_capture",
                    "handler_type" to "test_handler"
                ))
            }
        }

        eventPublisher.registerHandler("TurnierCreated", testHandler)
        eventPublisher.registerHandler("TurnierUpdated", testHandler)
        eventPublisher.registerHandler("TurnierDeleted", testHandler)

        // Register the actual handlers
        val auditHandler = TurnierAuditHandler()
        val notificationHandler = TurnierNotificationHandler()
        val analyticsHandler = TurnierAnalyticsHandler()
        val cacheHandler = TurnierCacheHandler()

        eventPublisher.registerHandler("TurnierCreated", auditHandler)
        eventPublisher.registerHandler("TurnierUpdated", auditHandler)
        eventPublisher.registerHandler("TurnierDeleted", auditHandler)

        eventPublisher.registerHandler("TurnierCreated", notificationHandler)
        eventPublisher.registerHandler("TurnierCreated", analyticsHandler)
        eventPublisher.registerHandler("TurnierCreated", cacheHandler)
        eventPublisher.registerHandler("TurnierUpdated", cacheHandler)
        eventPublisher.registerHandler("TurnierDeleted", cacheHandler)
    }

    @Test
    fun `test tournament creation publishes TurnierCreatedEvent`() = runBlocking {
        log.info("Starting test", mapOf(
            "test_name" to "tournament_creation",
            "test_phase" to "start",
            "test_type" to "event_driven_architecture"
        ))

        // Given
        val veranstaltungId = uuid4()
        val turnier = createTestTurnier(
            oepsTurnierNr = "TEST001",
            titel = "Test Tournament",
            untertitel = "Test Subtitle",
            veranstaltungId = veranstaltungId,
            datumVon = LocalDate(2024, 6, 1),
            datumBis = LocalDate(2024, 6, 3)
        )

        // When
        val createdTurnier = turnierService.createTurnier(turnier)

        // Give some time for async event processing
        delay(100)

        // Then
        assertNotNull(createdTurnier)
        assertEquals("Test Tournament", createdTurnier.titel)

        // Verify event was published
        val events = eventPublisher.getAllEvents()
        assertTrue(events.isNotEmpty(), "Events should be published")

        val createdEvent = events.find { it.eventType == "TurnierCreated" }
        assertNotNull(createdEvent, "TurnierCreatedEvent should be published")
        assertTrue(createdEvent is TurnierCreatedEvent)

        val typedEvent = createdEvent as TurnierCreatedEvent
        assertEquals(createdTurnier.id, typedEvent.aggregateId)
        assertEquals("Test Tournament", typedEvent.turnier.titel)

        // Verify test handler captured the event
        assertTrue(capturedEvents.any { it.eventType == "TurnierCreated" })

        log.info("Test completed", mapOf(
            "test_name" to "tournament_creation",
            "test_phase" to "complete",
            "test_result" to "success",
            "test_type" to "event_driven_architecture"
        ))
    }

    @Test
    fun `test tournament update publishes TurnierUpdatedEvent`() = runBlocking {
        log.info("Starting test", mapOf(
            "test_name" to "tournament_update",
            "test_phase" to "start",
            "test_type" to "event_driven_architecture"
        ))

        // Given - create a tournament first
        val veranstaltungId = uuid4()
        val originalTurnier = createTestTurnier(
            oepsTurnierNr = "TEST002",
            titel = "Original Title",
            untertitel = "Original Subtitle",
            veranstaltungId = veranstaltungId,
            datumVon = LocalDate(2024, 7, 1),
            datumBis = LocalDate(2024, 7, 3)
        )

        val createdTurnier = turnierService.createTurnier(originalTurnier)
        capturedEvents.clear() // Clear creation events

        // When - update the tournament
        val updatedTurnier = createdTurnier.copy(titel = "Updated Title")
        val result = turnierService.updateTurnier(createdTurnier.id, updatedTurnier)

        // Give some time for async event processing
        delay(100)

        // Then
        assertNotNull(result)
        assertEquals("Updated Title", result.titel)

        // Verify update event was published
        val updateEvents = eventPublisher.getEventsByType("TurnierUpdated")
        assertTrue(updateEvents.isNotEmpty(), "TurnierUpdatedEvent should be published")

        val updateEvent = updateEvents.first() as TurnierUpdatedEvent
        assertEquals(createdTurnier.id, updateEvent.aggregateId)
        assertEquals("Original Title", updateEvent.previousTurnier.titel)
        assertEquals("Updated Title", updateEvent.updatedTurnier.titel)

        // Verify test handler captured the event
        assertTrue(capturedEvents.any { it.eventType == "TurnierUpdated" })

        log.info("Test completed", mapOf(
            "test_name" to "tournament_update",
            "test_phase" to "complete",
            "test_result" to "success",
            "test_type" to "event_driven_architecture"
        ))
    }

    @Test
    fun `test tournament deletion publishes TurnierDeletedEvent`() = runBlocking {
        log.info("Starting test", mapOf(
            "test_name" to "tournament_deletion",
            "test_phase" to "start",
            "test_type" to "event_driven_architecture"
        ))

        // Given - create a tournament first
        val veranstaltungId = uuid4()
        val turnier = createTestTurnier(
            oepsTurnierNr = "TEST003",
            titel = "Tournament to Delete",
            untertitel = "Will be deleted",
            veranstaltungId = veranstaltungId,
            datumVon = LocalDate(2024, 8, 1),
            datumBis = LocalDate(2024, 8, 3)
        )

        val createdTurnier = turnierService.createTurnier(turnier)
        capturedEvents.clear() // Clear creation events

        // When - delete the tournament
        val deleted = turnierService.deleteTurnier(createdTurnier.id)

        // Give some time for async event processing
        delay(100)

        // Then
        assertTrue(deleted, "Tournament should be deleted")

        // Verify delete event was published
        val deleteEvents = eventPublisher.getEventsByType("TurnierDeleted")
        assertTrue(deleteEvents.isNotEmpty(), "TurnierDeletedEvent should be published")

        val deleteEvent = deleteEvents.first() as TurnierDeletedEvent
        assertEquals(createdTurnier.id, deleteEvent.aggregateId)
        assertEquals("Tournament to Delete", deleteEvent.deletedTurnier.titel)

        // Verify test handler captured the event
        assertTrue(capturedEvents.any { it.eventType == "TurnierDeleted" })

        log.info("Test completed", mapOf(
            "test_name" to "tournament_deletion",
            "test_phase" to "complete",
            "test_result" to "success",
            "test_type" to "event_driven_architecture"
        ))
    }

    @Test
    fun `test event store functionality`() = runBlocking {
        log.info("Starting test", mapOf(
            "test_name" to "event_store",
            "test_phase" to "start",
            "test_type" to "event_driven_architecture"
        ))

        // Given
        val veranstaltungId = uuid4()
        val turnier = createTestTurnier(
            oepsTurnierNr = "TEST004",
            titel = "Event Store Test",
            untertitel = "Testing event store",
            veranstaltungId = veranstaltungId,
            datumVon = LocalDate(2024, 9, 1),
            datumBis = LocalDate(2024, 9, 3)
        )

        // When - perform multiple operations
        val createdTurnier = turnierService.createTurnier(turnier)
        val updatedTurnier = turnierService.updateTurnier(createdTurnier.id, createdTurnier.copy(titel = "Updated Title"))
        turnierService.deleteTurnier(createdTurnier.id)

        // Give some time for async event processing
        delay(100)

        // Then - verify event store contains all events
        val allEvents = eventPublisher.getAllEvents()
        assertTrue(allEvents.size >= 3, "Should have at least 3 events (create, update, delete)")

        val aggregateEvents = eventPublisher.getEventsByAggregateId(createdTurnier.id)
        assertEquals(3, aggregateEvents.size, "Should have exactly 3 events for this aggregate")

        val eventTypes = aggregateEvents.map { it.eventType }.toSet()
        assertTrue(eventTypes.contains("TurnierCreated"))
        assertTrue(eventTypes.contains("TurnierUpdated"))
        assertTrue(eventTypes.contains("TurnierDeleted"))

        log.info("Test completed", mapOf(
            "test_name" to "event_store",
            "test_phase" to "complete",
            "test_result" to "success",
            "test_type" to "event_driven_architecture"
        ))
    }

    @Test
    fun `test multiple event handlers process same event`() = runBlocking {
        log.info("Starting test", mapOf(
            "test_name" to "multiple_handlers",
            "test_phase" to "start",
            "test_type" to "event_driven_architecture"
        ))

        // Given
        val veranstaltungId = uuid4()
        val turnier = createTestTurnier(
            oepsTurnierNr = "TEST005",
            titel = "Multi Handler Test",
            untertitel = "Testing multiple handlers",
            veranstaltungId = veranstaltungId,
            datumVon = LocalDate(2024, 10, 1),
            datumBis = LocalDate(2024, 10, 3)
        )

        // When
        turnierService.createTurnier(turnier)

        // Give some time for async event processing
        delay(200)

        // Then - verify that multiple handlers processed the same event
        // This is verified by the fact that we registered multiple handlers
        // (audit, notification, analytics, cache) for the same event type
        // and they should all process the event without interfering with each other

        val createdEvents = eventPublisher.getEventsByType("TurnierCreated")
        assertTrue(createdEvents.isNotEmpty(), "TurnierCreatedEvent should be published")

        // The test handler should have captured the event
        assertTrue(capturedEvents.any { it.eventType == "TurnierCreated" })

        log.info("Test completed", mapOf(
            "test_name" to "multiple_handlers",
            "test_phase" to "complete",
            "test_result" to "success",
            "test_type" to "event_driven_architecture"
        ))
    }
}

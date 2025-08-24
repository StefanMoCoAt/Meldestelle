package at.mocode.core.domain

import at.mocode.core.domain.event.BaseDomainEvent
import at.mocode.core.domain.model.*
import com.benasher44.uuid.uuid4
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(kotlin.time.ExperimentalTime::class)
class BaseDomainEventTest {

    @kotlinx.serialization.Serializable
    data class TestEvent(
        val name: String,
        // Delegiert an BaseDomainEvent
        private val base: BaseDomainEvent
    ) : BaseDomainEvent(
        aggregateId = base.aggregateId,
        eventType = base.eventType,
        version = base.version,
        eventId = base.eventId,
        timestamp = base.timestamp,
        correlationId = base.correlationId,
        causationId = base.causationId
    )

    @Test
    fun `secondary constructor generates id and timestamp`() {
        val aggId = AggregateId(uuid4())
        val ev = object : BaseDomainEvent(
            aggregateId = aggId,
            eventType = EventType("TestEvent"),
            version = EventVersion(1)
        ) {}

        assertNotNull(ev.eventId)
        assertNotNull(ev.timestamp)
        assertEquals(aggId, ev.aggregateId)
        assertEquals(EventType("TestEvent"), ev.eventType)
        assertEquals(EventVersion(1), ev.version)
    }

    @Test
    fun `primary constructor uses provided id and timestamp`() {
        val aggId = AggregateId(uuid4())
        val eid = EventId(uuid4())
        val ts = kotlin.time.Instant.parse("2025-01-01T00:00:00Z")
        val base = object : BaseDomainEvent(
            aggregateId = aggId,
            eventType = EventType("TestEvent"),
            version = EventVersion(2),
            eventId = eid,
            timestamp = ts,
            correlationId = CorrelationId(uuid4()),
            causationId = CausationId(uuid4())
        ) {}

        assertEquals(eid, base.eventId)
        assertEquals(ts, base.timestamp)
        assertEquals(EventVersion(2), base.version)
    }
}

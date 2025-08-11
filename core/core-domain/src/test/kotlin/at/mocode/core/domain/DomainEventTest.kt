package at.mocode.core.domain

import at.mocode.core.domain.event.BaseDomainEvent
import at.mocode.core.domain.model.*
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class DomainEventTest {

    /**
     * Eine konkrete Implementierung eines Domänen-Events zu Testzwecken.
     * Repräsentiert das Ereignis, dass eine Test-Entität erstellt wurde.
     *
     * @param aggregateId Die ID der Entität, auf die sich das Event bezieht.
     * @param version Die Versionsnummer des Events für dieses Aggregat.
     * @param testPayload Ein zusätzliches Datenfeld, das für den Test relevant ist.
     */
    @Serializable
    data class TestEvent(
        @Transient
        override val aggregateId: AggregateId = AggregateId(uuid4()),
        @Transient
        override val version: EventVersion = EventVersion(1L),
        val testPayload: String = "Test"
    ) : BaseDomainEvent(
        aggregateId = aggregateId,
        eventType = EventType("TestEventOccurred"), // Ein klar definierter Event-Typ
        version = version
    )

    @Test
    fun `BaseDomainEvent should auto-generate eventId and timestamp upon creation`() {
        // Arrange
        val aggregateId = AggregateId(uuid4())
        val version = EventVersion(1L)

        // Act
        val event = TestEvent(aggregateId, version)

        // Assert
        assertNotNull(event.eventId, "eventId should be automatically generated and not null")
        assertNotNull(event.timestamp, "timestamp should be automatically generated and not null")
        assertEquals(aggregateId, event.aggregateId, "aggregateId should be set correctly")
        assertEquals(version, event.version, "version should be set correctly")
        assertEquals(EventType("TestEventOccurred"), event.eventType, "eventType should be set correctly")
    }
}

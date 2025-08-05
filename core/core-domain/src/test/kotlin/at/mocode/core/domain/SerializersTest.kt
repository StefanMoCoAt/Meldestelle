package at.mocode.core.domain

import at.mocode.core.domain.serialization.KotlinInstantSerializer
import at.mocode.core.domain.serialization.KotlinLocalDateSerializer
import at.mocode.core.domain.serialization.KotlinLocalDateTimeSerializer
import at.mocode.core.domain.serialization.KotlinLocalTimeSerializer
import at.mocode.core.domain.serialization.UuidSerializer
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.time.Clock
import kotlin.time.Instant

class SerializersTest {

    private val json = Json // Standard-Json-Konfiguration für die Tests

    // Hilfsklasse, um die Serializer im Kontext von kotlinx.serialization zu testen
    @Serializable
    data class TestContainer(
        @Serializable(with = UuidSerializer::class) val uuid: Uuid,
        @Serializable(with = KotlinInstantSerializer::class) val instant: Instant,
        @Serializable(with = KotlinLocalDateSerializer::class) val localDate: LocalDate,
        @Serializable(with = KotlinLocalDateTimeSerializer::class) val localDateTime: LocalDateTime,
        @Serializable(with = KotlinLocalTimeSerializer::class) val localTime: LocalTime
    )

    @Test
    fun `all custom serializers should correctly serialize and deserialize`() {
        // Arrange
        val originalObject = TestContainer(
            uuid = uuid4(),
            instant = Clock.System.now(),
            localDate = LocalDate(2025, 8, 5),
            localDateTime = LocalDateTime(2025, 8, 5, 12, 30, 0),
            localTime = LocalTime(12, 30, 0)
        )

        // Act: Serialize
        val jsonString = json.encodeToString(TestContainer.serializer(), originalObject)

        // Assert: Serialization
        // Wir prüfen, ob die serialisierten Werte einfache Strings sind, wie erwartet.
        assertTrue(
            jsonString.contains("\"uuid\":\"${originalObject.uuid}\""),
            "Serialized JSON should contain the UUID as a string"
        )
        assertTrue(
            jsonString.contains("\"instant\":\"${originalObject.instant}\""),
            "Serialized JSON should contain the Instant as a string"
        )
        assertTrue(
            jsonString.contains("\"localDate\":\"2025-08-05\""),
            "Serialized JSON should contain the LocalDate as a string"
        )
        assertTrue(
            jsonString.contains("\"localDateTime\":\"2025-08-05T12:30\""),
            "Serialized JSON should contain the LocalDateTime as a string"
        )
        assertTrue(
            jsonString.contains("\"localTime\":\"12:30\""),
            "Serialized JSON should contain the LocalTime as a string"
        )

        // Act: Deserialize
        val deserializedObject = json.decodeFromString(TestContainer.serializer(), jsonString)

        // Assert: Deserialization
        assertEquals(originalObject, deserializedObject, "Deserialized object should be equal to the original object")
    }
}

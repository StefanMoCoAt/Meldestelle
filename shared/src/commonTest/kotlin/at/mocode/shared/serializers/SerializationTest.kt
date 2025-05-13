package at.mocode.shared.serializers

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.benasher44.uuid.uuidFrom
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SerializationTest {

    @Test
    fun testBigDecimalSerializer() {
        // Test regular values
        testBigDecimalSerialization(BigDecimal.fromInt(0))
        testBigDecimalSerialization(BigDecimal.fromInt(42))
        testBigDecimalSerialization(BigDecimal.fromInt(-42))
        testBigDecimalSerialization(BigDecimal.parseString("123.456"))
        testBigDecimalSerialization(BigDecimal.parseString("-123.456"))

        // Test edge cases
        testBigDecimalSerialization(BigDecimal.fromInt(Int.MAX_VALUE))
        testBigDecimalSerialization(BigDecimal.fromInt(Int.MIN_VALUE))
        testBigDecimalSerialization(BigDecimal.parseString("9999999999999999999.9999999999"))
        testBigDecimalSerialization(BigDecimal.parseString("-9999999999999999999.9999999999"))
    }

    private fun testBigDecimalSerialization(value: BigDecimal) {
        @Serializable
        data class TestClass(
            @Serializable(with = BigDecimalSerializer::class)
            val value: BigDecimal
        )

        val testObject = TestClass(value)
        val json = Json { prettyPrint = true }
        val jsonString = json.encodeToString(testObject)

        // Verify serialization
        assertTrue(jsonString.contains("\"value\""))
        assertTrue(jsonString.contains(value.toStringExpanded()))

        // Verify deserialization
        val deserializedObject = json.decodeFromString<TestClass>(jsonString)
        assertEquals(value, deserializedObject.value)
    }

    @Test
    fun testUuidSerializer() {
        // Test regular UUIDs
        testUuidSerialization(uuid4())
        testUuidSerialization(uuidFrom("00000000-0000-0000-0000-000000000000"))
        testUuidSerialization(uuidFrom("ffffffff-ffff-ffff-ffff-ffffffffffff"))

        // Test specific UUID formats
        testUuidSerialization(uuidFrom("12345678-1234-5678-1234-567812345678"))
    }

    private fun testUuidSerialization(value: Uuid) {
        @Serializable
        data class TestClass(
            @Serializable(with = UuidSerializer::class)
            val id: Uuid
        )

        val testObject = TestClass(value)
        val json = Json { prettyPrint = true }
        val jsonString = json.encodeToString(testObject)

        // Verify serialization
        assertTrue(jsonString.contains("\"id\""))
        assertTrue(jsonString.contains(value.toString()))

        // Verify deserialization
        val deserializedObject = json.decodeFromString<TestClass>(jsonString)
        assertEquals(value, deserializedObject.id)
    }

    @Test
    fun testKotlinInstantSerializer() {
        // Test current time
        testInstantSerialization(Clock.System.now())

        // Test specific instants
        testInstantSerialization(Instant.parse("2023-01-01T00:00:00Z"))
        testInstantSerialization(Instant.parse("1970-01-01T00:00:00Z"))
        testInstantSerialization(Instant.parse("2099-12-31T23:59:59.999Z"))

        // Test with different time zones
        testInstantSerialization(Instant.parse("2023-06-15T12:30:45.123+02:00"))
    }

    private fun testInstantSerialization(value: Instant) {
        @Serializable
        data class TestClass(
            @Serializable(with = KotlinInstantSerializer::class)
            val timestamp: Instant
        )

        val testObject = TestClass(value)
        val json = Json { prettyPrint = true }
        val jsonString = json.encodeToString(testObject)

        // Verify serialization
        assertTrue(jsonString.contains("\"timestamp\""))
        assertTrue(jsonString.contains(value.toString()))

        // Verify deserialization
        val deserializedObject = json.decodeFromString<TestClass>(jsonString)
        assertEquals(value, deserializedObject.timestamp)
    }

    @Test
    fun testKotlinLocalDateSerializer() {
        // Test regular dates
        testLocalDateSerialization(LocalDate(2023, 1, 1))
        testLocalDateSerialization(LocalDate(2000, 2, 29)) // Leap year
        testLocalDateSerialization(LocalDate(1970, 1, 1))

        // Test edge cases
        testLocalDateSerialization(LocalDate(1, 1, 1))
        testLocalDateSerialization(LocalDate(9999, 12, 31))
    }

    private fun testLocalDateSerialization(value: LocalDate) {
        @Serializable
        data class TestClass(
            @Serializable(with = KotlinLocalDateSerializer::class)
            val date: LocalDate
        )

        val testObject = TestClass(value)
        val json = Json { prettyPrint = true }
        val jsonString = json.encodeToString(testObject)

        // Verify serialization
        assertTrue(jsonString.contains("\"date\""))
        assertTrue(jsonString.contains(value.toString()))

        // Verify deserialization
        val deserializedObject = json.decodeFromString<TestClass>(jsonString)
        assertEquals(value, deserializedObject.date)
    }

    @Test
    fun testKotlinLocalDateTimeSerializer() {
        // Test regular date-times
        testLocalDateTimeSerialization(LocalDateTime(2023, 1, 1, 12, 0, 0))
        testLocalDateTimeSerialization(LocalDateTime(2000, 2, 29, 23, 59, 59)) // Leap year
        testLocalDateTimeSerialization(LocalDateTime(1970, 1, 1, 0, 0, 0))

        // Test with nanoseconds
        testLocalDateTimeSerialization(LocalDateTime(2023, 6, 15, 12, 30, 45, 123456789))

        // Test edge cases
        testLocalDateTimeSerialization(LocalDateTime(1, 1, 1, 0, 0, 0))
        testLocalDateTimeSerialization(LocalDateTime(9999, 12, 31, 23, 59, 59, 999999999))
    }

    private fun testLocalDateTimeSerialization(value: LocalDateTime) {
        @Serializable
        data class TestClass(
            @Serializable(with = KotlinLocalDateTimeSerializer::class)
            val dateTime: LocalDateTime
        )

        val testObject = TestClass(value)
        val json = Json { prettyPrint = true }
        val jsonString = json.encodeToString(testObject)

        // Verify serialization
        assertTrue(jsonString.contains("\"dateTime\""))
        assertTrue(jsonString.contains(value.toString()))

        // Verify deserialization
        val deserializedObject = json.decodeFromString<TestClass>(jsonString)
        assertEquals(value, deserializedObject.dateTime)
    }

    @Test
    fun testAllSerializersInSingleObject() {
        @Serializable
        data class ComplexObject(
            @Serializable(with = UuidSerializer::class)
            val id: Uuid,
            @Serializable(with = BigDecimalSerializer::class)
            val amount: BigDecimal,
            @Serializable(with = KotlinInstantSerializer::class)
            val createdAt: Instant,
            @Serializable(with = KotlinLocalDateSerializer::class)
            val date: LocalDate,
            @Serializable(with = KotlinLocalDateTimeSerializer::class)
            val dateTime: LocalDateTime
        )

        val obj = ComplexObject(
            id = uuid4(),
            amount = BigDecimal.parseString("123.456"),
            createdAt = Clock.System.now(),
            date = LocalDate(2023, 1, 1),
            dateTime = LocalDateTime(2023, 1, 1, 12, 0, 0)
        )

        val json = Json { prettyPrint = true }
        val jsonString = json.encodeToString(obj)

        // Verify serialization contains all fields
        assertTrue(jsonString.contains("\"id\""))
        assertTrue(jsonString.contains("\"amount\""))
        assertTrue(jsonString.contains("\"createdAt\""))
        assertTrue(jsonString.contains("\"date\""))
        assertTrue(jsonString.contains("\"dateTime\""))

        // Verify deserialization
        val deserializedObj = json.decodeFromString<ComplexObject>(jsonString)
        assertEquals(obj.id, deserializedObj.id)
        assertEquals(obj.amount, deserializedObj.amount)
        assertEquals(obj.createdAt, deserializedObj.createdAt)
        assertEquals(obj.date, deserializedObj.date)
        assertEquals(obj.dateTime, deserializedObj.dateTime)
    }
}

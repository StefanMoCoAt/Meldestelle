package at.mocode.horses.service.integration

import at.mocode.horses.application.usecase.TransactionalCreateHorseUseCase
import at.mocode.horses.domain.repository.HorseRepository
import at.mocode.core.domain.model.PferdeGeschlechtE
import com.benasher44.uuid.uuid4
import kotlinx.coroutines.*
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.assertTrue
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Integration tests to verify that transaction context issues with coroutines are resolved.
 *
 * This test class verifies that the transactional use cases properly handle
 * concurrent operations and maintain data consistency.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = [
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.jpa.hibernate.ddl-auto=create-drop"
])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TransactionalContextTest {

    @Autowired
    private lateinit var horseRepository: HorseRepository

    @Autowired
    private lateinit var transactionalCreateHorseUseCase: TransactionalCreateHorseUseCase

    @BeforeEach
    fun setUp() {
        runBlocking {
            // Clean up any existing test data
            // Note: This is a simplified cleanup - in a real scenario you'd have proper cleanup
        }
    }

    @Test
    fun `should handle race condition properly with transaction boundaries`(): Unit = runBlocking {
        println("[DEBUG_LOG] Starting transactional race condition test")

        val lebensnummer = "TRANS-RACE-001"
        val chipNummer = "TRANS-CHIP-001"

        // Create two identical horse creation requests
        val ownerId = uuid4()
        val request1 = TransactionalCreateHorseUseCase.CreateHorseRequest(
            pferdeName = "Transactional Race Horse 1",
            geschlecht = PferdeGeschlechtE.WALLACH,
            geburtsdatum = LocalDate(2020, 1, 1),
            lebensnummer = lebensnummer,
            chipNummer = chipNummer,
            besitzerId = ownerId
        )

        val request2 = TransactionalCreateHorseUseCase.CreateHorseRequest(
            pferdeName = "Transactional Race Horse 2",
            geschlecht = PferdeGeschlechtE.STUTE,
            geburtsdatum = LocalDate(2020, 1, 2),
            lebensnummer = lebensnummer, // Same lebensnummer - should cause conflict
            chipNummer = chipNummer,     // Same chipNummer - should cause conflict
            besitzerId = ownerId
        )

        println("[DEBUG_LOG] Created requests with duplicate identifiers")

        // Launch two concurrent coroutines to create horses using transactional use case
        val results = listOf(
            async {
                println("[DEBUG_LOG] Starting transactional creation 1")
                transactionalCreateHorseUseCase.execute(request1)
            },
            async {
                println("[DEBUG_LOG] Starting transactional creation 2")
                transactionalCreateHorseUseCase.execute(request2)
            }
        ).awaitAll()

        println("[DEBUG_LOG] Both transactional operations completed")
        println("[DEBUG_LOG] Result 1 success: ${results[0].success}")
        println("[DEBUG_LOG] Result 2 success: ${results[1].success}")

        // With proper transaction boundaries, exactly one should succeed
        val successCount = results.count { it.success }
        val failureCount = results.count { !it.success }

        println("[DEBUG_LOG] Success count: $successCount, Failure count: $failureCount")

        // Verify that exactly one operation succeeded and one failed
        assertEquals(1, successCount, "Exactly one operation should succeed with proper transactions")
        assertEquals(1, failureCount, "Exactly one operation should fail with proper transactions")

        // Check what actually got saved in the database
        val savedByLebensnummer = horseRepository.findByLebensnummer(lebensnummer)
        val savedByChipNummer = horseRepository.findByChipNummer(chipNummer)

        println("[DEBUG_LOG] Found by lebensnummer: ${savedByLebensnummer?.pferdeName}")
        println("[DEBUG_LOG] Found by chipNummer: ${savedByChipNummer?.pferdeName}")

        // Verify that exactly one horse was saved
        assertNotNull(savedByLebensnummer, "One horse should be saved with the lebensnummer")
        assertNotNull(savedByChipNummer, "One horse should be saved with the chipNummer")
        assertEquals(savedByLebensnummer?.pferdId, savedByChipNummer?.pferdId, "Both queries should return the same horse")

        // Verify that the failed operation returned proper error
        val failedResult = results.find { !it.success }
        assertNotNull(failedResult, "There should be one failed result")
        assertEquals("UNIQUENESS_ERROR", failedResult?.error?.code, "Failed operation should return uniqueness error")

        println("[DEBUG_LOG] Transactional test completed successfully - race condition properly handled")
    }

    @Test
    fun `should maintain transaction consistency on validation failure`(): Unit = runBlocking {
        println("[DEBUG_LOG] Starting transaction consistency test")

        // Create a request with invalid data that will fail validation
        val request = TransactionalCreateHorseUseCase.CreateHorseRequest(
            pferdeName = "", // Empty name should fail validation
            geschlecht = PferdeGeschlechtE.HENGST,
            lebensnummer = "VALIDATION-TEST-001",
            stockmass = 300, // Invalid height should fail validation
            besitzerId = uuid4() // Add owner to pass basic validation
        )

        println("[DEBUG_LOG] Executing transactional create with invalid data")
        val result = transactionalCreateHorseUseCase.execute(request)

        println("[DEBUG_LOG] Creation result: success=${result.success}")

        // Verify that the operation failed due to validation
        assertTrue(!result.success, "Operation should fail due to validation errors")
        assertEquals("VALIDATION_ERROR", result.error?.code, "Should return validation error")

        // Verify that no horse was saved in the database
        val savedHorse = horseRepository.findByLebensnummer("VALIDATION-TEST-001")
        assertTrue(savedHorse == null, "No horse should be saved when validation fails")

        println("[DEBUG_LOG] Transaction consistency test completed - no data saved on validation failure")
    }

    @Test
    fun `should successfully create horse with valid data in transaction`(): Unit = runBlocking {
        println("[DEBUG_LOG] Starting successful transactional creation test")

        val request = TransactionalCreateHorseUseCase.CreateHorseRequest(
            pferdeName = "Successful Transaction Horse",
            geschlecht = PferdeGeschlechtE.STUTE,
            geburtsdatum = LocalDate(2021, 6, 15),
            lebensnummer = "SUCCESS-TEST-001",
            chipNummer = "SUCCESS-CHIP-001",
            rasse = "Warmblut",
            stockmass = 165,
            besitzerId = uuid4() // Add required owner
        )

        println("[DEBUG_LOG] Executing transactional create with valid data")
        val result = transactionalCreateHorseUseCase.execute(request)

        println("[DEBUG_LOG] Creation result: success=${result.success}")

        // Verify that the operation succeeded
        assertTrue(result.success, "Operation should succeed with valid data")
        assertNotNull(result.data, "Result should contain the created horse")
        assertEquals("Successful Transaction Horse", result.data?.pferdeName, "Horse name should match")

        // Verify that the horse was saved in the database
        val savedHorse = horseRepository.findByLebensnummer("SUCCESS-TEST-001")
        assertNotNull(savedHorse, "Horse should be saved in database")
        assertEquals("Successful Transaction Horse", savedHorse.pferdeName, "Saved horse name should match")
        assertEquals("SUCCESS-CHIP-001", savedHorse.chipNummer, "Saved horse chip number should match")

        println("[DEBUG_LOG] Successful transactional creation test completed")
    }
}

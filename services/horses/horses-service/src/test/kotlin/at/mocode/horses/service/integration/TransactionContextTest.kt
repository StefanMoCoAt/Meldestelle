package at.mocode.horses.service.integration

import at.mocode.horses.domain.model.DomPferd
import at.mocode.horses.domain.repository.HorseRepository
import at.mocode.core.domain.model.PferdeGeschlechtE
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
 * Integration tests to demonstrate and verify transaction context issues with coroutines.
 *
 * This test class reproduces the race condition that can occur when multiple
 * coroutines perform database operations without proper transaction boundaries.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = [
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.jpa.hibernate.ddl-auto=create-drop"
])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TransactionContextTest {

    @Autowired
    private lateinit var horseRepository: HorseRepository

    @BeforeEach
    fun setUp() {
        runBlocking {
            // Clean up any existing test data
            // Note: This is a simplified cleanup - in a real scenario you'd have proper cleanup
        }
    }

    @Test
    fun `should demonstrate race condition without transaction boundaries`(): Unit = runBlocking {
        println("[DEBUG_LOG] Starting race condition test")

        val lebensnummer = "TEST-RACE-001"
        val chipNummer = "CHIP-RACE-001"

        // Create two horses with the same identifiers
        val horse1 = DomPferd(
            pferdeName = "Race Horse 1",
            geschlecht = PferdeGeschlechtE.WALLACH,
            geburtsdatum = LocalDate(2020, 1, 1),
            lebensnummer = lebensnummer,
            chipNummer = chipNummer,
            istAktiv = true
        )

        val horse2 = DomPferd(
            pferdeName = "Race Horse 2",
            geschlecht = PferdeGeschlechtE.STUTE,
            geburtsdatum = LocalDate(2020, 1, 2),
            lebensnummer = lebensnummer, // Same lebensnummer - should cause conflict
            chipNummer = chipNummer,     // Same chipNummer - should cause conflict
            istAktiv = true
        )

        println("[DEBUG_LOG] Created horses with duplicate identifiers")

        // Simulate the use case logic: check uniqueness then save
        // This mimics what CreateHorseUseCase.execute() does without transactions
        suspend fun createHorseWithChecks(horse: DomPferd): Boolean {
            return try {
                // Check uniqueness constraints (like in checkUniquenessConstraints)
                val existsByLebensnummer = horse.lebensnummer?.let {
                    horseRepository.existsByLebensnummer(it)
                } ?: false

                val existsByChipNummer = horse.chipNummer?.let {
                    horseRepository.existsByChipNummer(it)
                } ?: false

                println("[DEBUG_LOG] ${horse.pferdeName}: existsByLebensnummer=$existsByLebensnummer, existsByChipNummer=$existsByChipNummer")

                if (existsByLebensnummer || existsByChipNummer) {
                    println("[DEBUG_LOG] ${horse.pferdeName}: Uniqueness check failed")
                    false
                } else {
                    // Save the horse (like in the use case)
                    horseRepository.save(horse)
                    println("[DEBUG_LOG] ${horse.pferdeName}: Saved successfully")
                    true
                }
            } catch (e: Exception) {
                println("[DEBUG_LOG] ${horse.pferdeName}: Exception during creation: ${e.message}")
                false
            }
        }

        // Launch two concurrent coroutines to create horses
        val results = listOf(
            async {
                println("[DEBUG_LOG] Starting creation 1")
                createHorseWithChecks(horse1)
            },
            async {
                println("[DEBUG_LOG] Starting creation 2")
                createHorseWithChecks(horse2)
            }
        ).awaitAll()

        println("[DEBUG_LOG] Both operations completed")
        println("[DEBUG_LOG] Result 1 success: ${results[0]}")
        println("[DEBUG_LOG] Result 2 success: ${results[1]}")

        // In a properly transactional system, exactly one should succeed
        val successCount = results.count { it }
        val failureCount = results.count { !it }

        println("[DEBUG_LOG] Success count: $successCount, Failure count: $failureCount")

        // Check what actually got saved in the database
        val savedByLebensnummer = horseRepository.findByLebensnummer(lebensnummer)
        val savedByChipNummer = horseRepository.findByChipNummer(chipNummer)

        println("[DEBUG_LOG] Found by lebensnummer: ${savedByLebensnummer?.pferdeName}")
        println("[DEBUG_LOG] Found by chipNummer: ${savedByChipNummer?.pferdeName}")

        // This test demonstrates the issue - without transactions, both operations might succeed
        // due to race conditions, or the behavior might be unpredictable
        // The fix should ensure exactly one succeeds and one fails with a proper error
        assertTrue(successCount >= 1, "At least one operation should succeed")
    }

    @Test
    fun `should demonstrate transaction context propagation issue`(): Unit = runBlocking {
        println("[DEBUG_LOG] Starting transaction context propagation test")

        // This test will show that without @Transactional, each repository call
        // runs in its own transaction context, which can lead to inconsistencies

        val horse = DomPferd(
            pferdeName = "Transaction Test Horse",
            geschlecht = PferdeGeschlechtE.HENGST,
            lebensnummer = "TRANS-TEST-001",
            istAktiv = true
        )

        println("[DEBUG_LOG] Creating horse with repository operations")

        // Simulate multiple repository operations that should be atomic
        val existsCheck = horseRepository.existsByLebensnummer("TRANS-TEST-001")
        println("[DEBUG_LOG] Exists check result: $existsCheck")

        if (!existsCheck) {
            val savedHorse = horseRepository.save(horse)
            println("[DEBUG_LOG] Horse saved successfully: ${savedHorse.pferdeName}")
            assertNotNull(savedHorse)
            assertEquals("Transaction Test Horse", savedHorse.pferdeName)
        }

        // The issue is that without @Transactional, if an exception occurs after
        // the uniqueness checks but before/during save, the database state
        // might be inconsistent
        val finalCheck = horseRepository.findByLebensnummer("TRANS-TEST-001")
        assertNotNull(finalCheck, "Horse should be saved in database")
    }
}

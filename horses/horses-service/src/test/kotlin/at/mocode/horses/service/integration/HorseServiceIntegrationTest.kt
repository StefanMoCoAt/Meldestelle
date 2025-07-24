package at.mocode.horses.service.integration

import at.mocode.horses.domain.model.DomPferd
import at.mocode.horses.domain.repository.HorseRepository
import at.mocode.infrastructure.messaging.client.EventPublisher
import at.mocode.core.domain.model.PferdeGeschlechtE
import kotlinx.datetime.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration tests for the Horses Service.
 *
 * These tests verify the complete functionality including:
 * - Repository operations
 * - Database persistence
 * - Domain model validation
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = [
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.kafka.bootstrap-servers=localhost:9092"
])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HorseServiceIntegrationTest {

    @Autowired
    private lateinit var horseRepository: HorseRepository

    @MockBean
    private lateinit var eventPublisher: EventPublisher

    @BeforeEach
    fun setUp() = runBlocking {
        // Clean up database before each test
        println("[DEBUG_LOG] Setting up horse test - cleaning database")
    }

    @Test
    fun `should create horse successfully`() = runBlocking {
        println("[DEBUG_LOG] Testing horse creation")

        // Given
        val horse = DomPferd(
            pferdeName = "Thunder",
            geschlecht = PferdeGeschlechtE.WALLACH,
            geburtsdatum = LocalDate(2020, 5, 15),
            rasse = "Warmblut",
            farbe = "Braun",
            lebensnummer = "AT123456789",
            chipNummer = "123456789012345",
            stockmass = 165,
            istAktiv = true
        )

        // When
        val savedHorse = horseRepository.save(horse)

        // Then
        assertNotNull(savedHorse)
        assertEquals("Thunder", savedHorse.pferdeName)
        assertEquals(PferdeGeschlechtE.WALLACH, savedHorse.geschlecht)
        assertEquals("AT123456789", savedHorse.lebensnummer)
        assertEquals("123456789012345", savedHorse.chipNummer)
        assertEquals("Warmblut", savedHorse.rasse)
        assertTrue(savedHorse.istAktiv)

        println("[DEBUG_LOG] Horse created successfully with ID: ${savedHorse.pferdId}")
    }

    @Test
    fun `should find horse by lebensnummer`() = runBlocking {
        println("[DEBUG_LOG] Testing find horse by lebensnummer")

        // Given
        val horse = DomPferd(
            pferdeName = "Lightning",
            geschlecht = PferdeGeschlechtE.STUTE,
            geburtsdatum = LocalDate(2019, 3, 10),
            rasse = "Vollblut",
            farbe = "Schimmel",
            lebensnummer = "AT987654321",
            chipNummer = "987654321098765",
            stockmass = 160,
            istAktiv = true
        )
        horseRepository.save(horse)

        // When
        val foundHorse = horseRepository.findByLebensnummer("AT987654321")

        // Then
        assertNotNull(foundHorse)
        assertEquals("Lightning", foundHorse.pferdeName)
        assertEquals("AT987654321", foundHorse.lebensnummer)
        assertEquals(PferdeGeschlechtE.STUTE, foundHorse.geschlecht)
        assertEquals("Vollblut", foundHorse.rasse)

        println("[DEBUG_LOG] Horse found by lebensnummer: ${foundHorse.pferdId}")
    }

    @Test
    fun `should find horse by chip number`() = runBlocking {
        println("[DEBUG_LOG] Testing find horse by chip number")

        // Given
        val horse = DomPferd(
            pferdeName = "Storm",
            geschlecht = PferdeGeschlechtE.HENGST,
            geburtsdatum = LocalDate(2021, 8, 20),
            rasse = "Haflinger",
            farbe = "Fuchs",
            lebensnummer = "AT555666777",
            chipNummer = "555666777888999",
            stockmass = 150,
            istAktiv = true
        )
        horseRepository.save(horse)

        // When
        val foundHorse = horseRepository.findByChipNummer("555666777888999")

        // Then
        assertNotNull(foundHorse)
        assertEquals("Storm", foundHorse.pferdeName)
        assertEquals("555666777888999", foundHorse.chipNummer)
        assertEquals(PferdeGeschlechtE.HENGST, foundHorse.geschlecht)
        assertEquals("Haflinger", foundHorse.rasse)

        println("[DEBUG_LOG] Horse found by chip number: ${foundHorse.pferdId}")
    }

    @Test
    fun `should find horses by gender`() = runBlocking {
        println("[DEBUG_LOG] Testing find horses by gender")

        // Given
        val stallion = DomPferd(
            pferdeName = "Stallion Horse",
            geschlecht = PferdeGeschlechtE.HENGST,
            geburtsdatum = LocalDate(2018, 4, 12),
            rasse = "Warmblut",
            farbe = "Braun",
            lebensnummer = "AT111222333",
            chipNummer = "111222333444555",
            stockmass = 170,
            istAktiv = true
        )

        val mare = DomPferd(
            pferdeName = "Mare Horse",
            geschlecht = PferdeGeschlechtE.STUTE,
            geburtsdatum = LocalDate(2017, 6, 8),
            rasse = "Vollblut",
            farbe = "Rappe",
            lebensnummer = "AT444555666",
            chipNummer = "444555666777888",
            stockmass = 165,
            istAktiv = true
        )

        horseRepository.save(stallion)
        horseRepository.save(mare)

        // When
        val stallions = horseRepository.findByGeschlecht(PferdeGeschlechtE.HENGST, true, 10)

        // Then
        assertTrue(stallions.isNotEmpty(), "Should find at least one stallion")
        assertTrue(stallions.any { it.pferdeName == "Stallion Horse" }, "Should contain the stallion horse")
        assertTrue(stallions.all { it.geschlecht == PferdeGeschlechtE.HENGST }, "All returned horses should be stallions")

        println("[DEBUG_LOG] Found ${stallions.size} stallions")
    }

    @Test
    fun `should find horses by breed`() = runBlocking {
        println("[DEBUG_LOG] Testing find horses by breed")

        // Given
        val warmblutHorse = DomPferd(
            pferdeName = "Warmblut Horse",
            geschlecht = PferdeGeschlechtE.WALLACH,
            geburtsdatum = LocalDate(2019, 9, 15),
            rasse = "Warmblut",
            farbe = "Braun",
            lebensnummer = "AT333444555",
            chipNummer = "333444555666777",
            stockmass = 168,
            istAktiv = true
        )
        horseRepository.save(warmblutHorse)

        // When
        val warmblutHorses = horseRepository.findByRasse("Warmblut", true, 10)

        // Then
        assertTrue(warmblutHorses.isNotEmpty(), "Should find at least one Warmblut horse")
        assertTrue(warmblutHorses.any { it.pferdeName == "Warmblut Horse" }, "Should contain the Warmblut horse")
        assertTrue(warmblutHorses.all { it.rasse == "Warmblut" }, "All returned horses should be Warmblut")

        println("[DEBUG_LOG] Found ${warmblutHorses.size} Warmblut horses")
    }

    @Test
    fun `should find OEPS registered horses`() = runBlocking {
        println("[DEBUG_LOG] Testing find OEPS registered horses")

        // Given
        val oepsHorse = DomPferd(
            pferdeName = "OEPS Horse",
            geschlecht = PferdeGeschlechtE.WALLACH,
            geburtsdatum = LocalDate(2018, 7, 22),
            rasse = "Warmblut",
            farbe = "Braun",
            lebensnummer = "AT777888999",
            chipNummer = "777888999000111",
            oepsNummer = "OEPS123456",
            stockmass = 170,
            istAktiv = true
        )

        val nonOepsHorse = DomPferd(
            pferdeName = "Non-OEPS Horse",
            geschlecht = PferdeGeschlechtE.STUTE,
            geburtsdatum = LocalDate(2017, 11, 5),
            rasse = "Vollblut",
            farbe = "Rappe",
            lebensnummer = "AT000111222",
            chipNummer = "000111222333444",
            stockmass = 165,
            istAktiv = true
        )

        horseRepository.save(oepsHorse)
        horseRepository.save(nonOepsHorse)

        // When
        val oepsHorses = horseRepository.findOepsRegistered(true)

        // Then
        assertTrue(oepsHorses.isNotEmpty(), "Should find at least one OEPS registered horse")
        assertTrue(oepsHorses.any { it.pferdeName == "OEPS Horse" }, "Should contain the OEPS registered horse")
        assertTrue(oepsHorses.all { !it.oepsNummer.isNullOrBlank() }, "All returned horses should have OEPS numbers")

        println("[DEBUG_LOG] Found ${oepsHorses.size} OEPS registered horses")
    }

    @Test
    fun `should find FEI registered horses`() = runBlocking {
        println("[DEBUG_LOG] Testing find FEI registered horses")

        // Given
        val feiHorse = DomPferd(
            pferdeName = "FEI Horse",
            geschlecht = PferdeGeschlechtE.HENGST,
            geburtsdatum = LocalDate(2016, 2, 14),
            rasse = "Warmblut",
            farbe = "Schimmel",
            lebensnummer = "AT999000111",
            chipNummer = "999000111222333",
            feiNummer = "FEI789012",
            stockmass = 175,
            istAktiv = true
        )
        horseRepository.save(feiHorse)

        // When
        val feiHorses = horseRepository.findFeiRegistered(true)

        // Then
        assertTrue(feiHorses.isNotEmpty(), "Should find at least one FEI registered horse")
        assertTrue(feiHorses.any { it.pferdeName == "FEI Horse" }, "Should contain the FEI registered horse")
        assertTrue(feiHorses.all { !it.feiNummer.isNullOrBlank() }, "All returned horses should have FEI numbers")

        println("[DEBUG_LOG] Found ${feiHorses.size} FEI registered horses")
    }

    @Test
    fun `should validate duplicate lebensnummer`() = runBlocking {
        println("[DEBUG_LOG] Testing duplicate lebensnummer validation")

        // Given
        val horse = DomPferd(
            pferdeName = "First Horse",
            geschlecht = PferdeGeschlechtE.WALLACH,
            geburtsdatum = LocalDate(2019, 1, 1),
            rasse = "Warmblut",
            farbe = "Braun",
            lebensnummer = "AT123123123",
            chipNummer = "123123123456789",
            stockmass = 165,
            istAktiv = true
        )
        horseRepository.save(horse)

        // When
        val exists = horseRepository.existsByLebensnummer("AT123123123")

        // Then
        assertTrue(exists, "Should detect existing lebensnummer")

        println("[DEBUG_LOG] Duplicate lebensnummer validation passed")
    }

    @Test
    fun `should validate duplicate chip number`() = runBlocking {
        println("[DEBUG_LOG] Testing duplicate chip number validation")

        // Given
        val horse = DomPferd(
            pferdeName = "Chip Test Horse",
            geschlecht = PferdeGeschlechtE.STUTE,
            geburtsdatum = LocalDate(2020, 12, 25),
            rasse = "Haflinger",
            farbe = "Fuchs",
            lebensnummer = "AT456456456",
            chipNummer = "456456456789012",
            stockmass = 148,
            istAktiv = true
        )
        horseRepository.save(horse)

        // When
        val exists = horseRepository.existsByChipNummer("456456456789012")

        // Then
        assertTrue(exists, "Should detect existing chip number")

        println("[DEBUG_LOG] Duplicate chip number validation passed")
    }
}

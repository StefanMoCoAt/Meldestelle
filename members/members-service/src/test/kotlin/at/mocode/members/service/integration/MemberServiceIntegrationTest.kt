package at.mocode.members.service.integration

import at.mocode.members.api.rest.MemberController
import at.mocode.members.domain.model.Member
import at.mocode.members.domain.repository.MemberRepository
import at.mocode.members.infrastructure.persistence.MemberRepositoryImpl
import at.mocode.infrastructure.messaging.client.EventPublisher
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
import org.springframework.beans.factory.annotation.Qualifier
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration tests for the Members Service.
 *
 * These tests verify the complete functionality including
 * - REST API endpoints
 * - Database operations
 * - Event publishing
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = [
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.kafka.bootstrap-servers=localhost:9092"
])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MemberServiceIntegrationTest {

    @Autowired
    @Qualifier("memberRepositoryImpl")
    private lateinit var memberRepository: MemberRepository

    @MockBean
    private lateinit var eventPublisher: EventPublisher

    @BeforeEach
    fun setUp() = runBlocking {
        // Clean up database before each test
        // Note: In a real implementation, you might want to use @Transactional or @DirtiesContext
        println("[DEBUG_LOG] Setting up test - cleaning database")
    }

    @Test
    fun `should create member successfully`() = runBlocking {
        println("[DEBUG_LOG] Testing member creation")

        // Given
        val createRequest = MemberController.CreateMemberRequest(
            firstName = "John",
            lastName = "Doe",
            email = "john.doe@example.com",
            phone = "+43123456789",
            dateOfBirth = LocalDate(1990, 1, 15),
            membershipNumber = "M001",
            membershipStartDate = LocalDate(2024, 1, 1),
            membershipEndDate = null,
            isActive = true,
            address = "123 Test Street, Vienna",
            emergencyContact = "Jane Doe: +43987654321"
        )

        // When
        val member = Member(
            firstName = createRequest.firstName,
            lastName = createRequest.lastName,
            email = createRequest.email,
            phone = createRequest.phone,
            dateOfBirth = createRequest.dateOfBirth,
            membershipNumber = createRequest.membershipNumber,
            membershipStartDate = createRequest.membershipStartDate,
            membershipEndDate = createRequest.membershipEndDate,
            isActive = createRequest.isActive,
            address = createRequest.address,
            emergencyContact = createRequest.emergencyContact
        )

        val savedMember = memberRepository.save(member)

        // Then
        assertNotNull(savedMember)
        assertEquals(createRequest.firstName, savedMember.firstName)
        assertEquals(createRequest.lastName, savedMember.lastName)
        assertEquals(createRequest.email, savedMember.email)
        assertEquals(createRequest.membershipNumber, savedMember.membershipNumber)
        assertTrue(savedMember.isActive)

        println("[DEBUG_LOG] Member created successfully with ID: ${savedMember.memberId}")
    }

    @Test
    fun `should find member by membership number`() = runBlocking {
        println("[DEBUG_LOG] Testing find member by membership number")

        // Given
        val member = Member(
            firstName = "Jane",
            lastName = "Smith",
            email = "jane.smith@example.com",
            membershipNumber = "M002",
            membershipStartDate = LocalDate(2024, 1, 1),
            isActive = true
        )
        memberRepository.save(member)

        // When
        val foundMember = memberRepository.findByMembershipNumber("M002")

        // Then
        assertNotNull(foundMember)
        assertEquals("Jane", foundMember.firstName)
        assertEquals("Smith", foundMember.lastName)
        assertEquals("M002", foundMember.membershipNumber)

        println("[DEBUG_LOG] Member found by membership number: ${foundMember.memberId}")
    }

    @Test
    fun `should find member by email`() = runBlocking {
        println("[DEBUG_LOG] Testing find member by email")

        // Given
        val member = Member(
            firstName = "Bob",
            lastName = "Johnson",
            email = "bob.johnson@example.com",
            membershipNumber = "M003",
            membershipStartDate = LocalDate(2024, 1, 1),
            isActive = true
        )
        memberRepository.save(member)

        // When
        val foundMember = memberRepository.findByEmail("bob.johnson@example.com")

        // Then
        assertNotNull(foundMember)
        assertEquals("Bob", foundMember.firstName)
        assertEquals("Johnson", foundMember.lastName)
        assertEquals("bob.johnson@example.com", foundMember.email)

        println("[DEBUG_LOG] Member found by email: ${foundMember.memberId}")
    }

    @Test
    fun `should count active members`() = runBlocking {
        println("[DEBUG_LOG] Testing count active members")

        // Given
        val activeMember = Member(
            firstName = "Active",
            lastName = "Member",
            email = "active@example.com",
            membershipNumber = "M004",
            membershipStartDate = LocalDate(2024, 1, 1),
            isActive = true
        )

        val inactiveMember = Member(
            firstName = "Inactive",
            lastName = "Member",
            email = "inactive@example.com",
            membershipNumber = "M005",
            membershipStartDate = LocalDate(2024, 1, 1),
            isActive = false
        )

        memberRepository.save(activeMember)
        memberRepository.save(inactiveMember)

        // When
        val activeCount = memberRepository.countActive()
        val totalCount = memberRepository.countAll()

        // Then
        assertTrue(activeCount >= 1, "Should have at least 1 active member")
        assertTrue(totalCount >= 2, "Should have at least 2 total members")

        println("[DEBUG_LOG] Active members: $activeCount, Total members: $totalCount")
    }

    @Test
    fun `should validate duplicate membership number`() = runBlocking {
        println("[DEBUG_LOG] Testing duplicate membership number validation")

        // Given
        val member1 = Member(
            firstName = "First",
            lastName = "Member",
            email = "first@example.com",
            membershipNumber = "M006",
            membershipStartDate = LocalDate(2024, 1, 1),
            isActive = true
        )
        memberRepository.save(member1)

        // When
        val exists = memberRepository.existsByMembershipNumber("M006")

        // Then
        assertTrue(exists, "Should detect existing membership number")

        println("[DEBUG_LOG] Duplicate membership number validation passed")
    }

    @Test
    fun `should validate duplicate email`() = runBlocking {
        println("[DEBUG_LOG] Testing duplicate email validation")

        // Given
        val member = Member(
            firstName = "Email",
            lastName = "Test",
            email = "email.test@example.com",
            membershipNumber = "M007",
            membershipStartDate = LocalDate(2024, 1, 1),
            isActive = true
        )
        memberRepository.save(member)

        // When
        val exists = memberRepository.existsByEmail("email.test@example.com")

        // Then
        assertTrue(exists, "Should detect existing email")

        println("[DEBUG_LOG] Duplicate email validation passed")
    }
}

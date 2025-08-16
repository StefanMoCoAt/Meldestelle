package at.mocode.infrastructure.auth.client

import at.mocode.infrastructure.auth.client.model.BerechtigungE
import com.benasher44.uuid.uuid4
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

/**
 * Tests for the AuthenticationService interface using mocks.
 * These tests verify the contract and behavior expectations without requiring real implementations.
 */
class AuthenticationServiceTest {

    private lateinit var authService: AuthenticationService
    private val testUserId = uuid4()
    private val testPersonId = uuid4()

    @BeforeEach
    fun setUp() {
        authService = mockk<AuthenticationService>()
    }

    // ========== Authentication Tests ==========

    @Test
    fun `authenticate should return Success for valid credentials`() = runTest {
        // Arrange
        val username = "testuser"
        val password = "validpassword"
        val expectedUser = AuthenticationService.AuthenticatedUser(
            userId = testUserId,
            personId = testPersonId,
            username = username,
            email = "test@example.com",
            permissions = listOf(BerechtigungE.PERSON_READ, BerechtigungE.VEREIN_READ)
        )
        val expectedToken = "valid.jwt.token"

        coEvery { authService.authenticate(username, password) } returns
            AuthenticationService.AuthResult.Success(expectedToken, expectedUser)

        // Act
        val result = authService.authenticate(username, password)

        // Assert
        assertTrue(result is AuthenticationService.AuthResult.Success)
        val successResult = result as AuthenticationService.AuthResult.Success
        assertEquals(expectedToken, successResult.token)
        assertEquals(expectedUser.userId, successResult.user.userId)
        assertEquals(expectedUser.username, successResult.user.username)
        assertEquals(expectedUser.email, successResult.user.email)
        assertEquals(2, successResult.user.permissions.size)
        assertTrue(successResult.user.permissions.contains(BerechtigungE.PERSON_READ))
        assertTrue(successResult.user.permissions.contains(BerechtigungE.VEREIN_READ))
    }

    @Test
    fun `authenticate should return Failure for invalid credentials`() = runTest {
        // Arrange
        val username = "testuser"
        val password = "wrongpassword"
        val expectedReason = "Invalid username or password"

        coEvery { authService.authenticate(username, password) } returns
            AuthenticationService.AuthResult.Failure(expectedReason)

        // Act
        val result = authService.authenticate(username, password)

        // Assert
        assertTrue(result is AuthenticationService.AuthResult.Failure)
        val failureResult = result as AuthenticationService.AuthResult.Failure
        assertEquals(expectedReason, failureResult.reason)
    }

    @Test
    fun `authenticate should return Locked for locked accounts`() = runTest {
        // Arrange
        val username = "lockeduser"
        val password = "password"
        val lockedUntil = LocalDateTime.now().plusHours(1)

        coEvery { authService.authenticate(username, password) } returns
            AuthenticationService.AuthResult.Locked(lockedUntil)

        // Act
        val result = authService.authenticate(username, password)

        // Assert
        assertTrue(result is AuthenticationService.AuthResult.Locked)
        val lockedResult = result as AuthenticationService.AuthResult.Locked
        assertEquals(lockedUntil, lockedResult.lockedUntil)
    }

    @Test
    fun `authenticate should handle empty username gracefully`() = runTest {
        // Arrange
        val emptyUsername = ""
        val password = "password"

        coEvery { authService.authenticate(emptyUsername, password) } returns
            AuthenticationService.AuthResult.Failure("Username cannot be empty")

        // Act
        val result = authService.authenticate(emptyUsername, password)

        // Assert
        assertTrue(result is AuthenticationService.AuthResult.Failure)
        val failureResult = result as AuthenticationService.AuthResult.Failure
        assertTrue(failureResult.reason.contains("Username"))
    }

    @Test
    fun `authenticate should handle empty password gracefully`() = runTest {
        // Arrange
        val username = "testuser"
        val emptyPassword = ""

        coEvery { authService.authenticate(username, emptyPassword) } returns
            AuthenticationService.AuthResult.Failure("Password cannot be empty")

        // Act
        val result = authService.authenticate(username, emptyPassword)

        // Assert
        assertTrue(result is AuthenticationService.AuthResult.Failure)
        val failureResult = result as AuthenticationService.AuthResult.Failure
        assertTrue(failureResult.reason.contains("Password"))
    }

    // ========== Password Change Tests ==========

    @Test
    fun `changePassword should return Success for valid password change`() = runTest {
        // Arrange
        val currentPassword = "oldpassword"
        val newPassword = "newpassword123"

        coEvery { authService.changePassword(testUserId, currentPassword, newPassword) } returns
            AuthenticationService.PasswordChangeResult.Success

        // Act
        val result = authService.changePassword(testUserId, currentPassword, newPassword)

        // Assert
        assertTrue(result is AuthenticationService.PasswordChangeResult.Success)
    }

    @Test
    fun `changePassword should validate current password`() = runTest {
        // Arrange
        val wrongCurrentPassword = "wrongpassword"
        val newPassword = "newpassword123"

        coEvery { authService.changePassword(testUserId, wrongCurrentPassword, newPassword) } returns
            AuthenticationService.PasswordChangeResult.Failure("Current password is incorrect")

        // Act
        val result = authService.changePassword(testUserId, wrongCurrentPassword, newPassword)

        // Assert
        assertTrue(result is AuthenticationService.PasswordChangeResult.Failure)
        val failureResult = result as AuthenticationService.PasswordChangeResult.Failure
        assertTrue(failureResult.reason.contains("Current password"))
    }

    @Test
    fun `changePassword should reject weak passwords`() = runTest {
        // Arrange
        val currentPassword = "oldpassword"
        val weakPassword = "123" // Too short and simple

        coEvery { authService.changePassword(testUserId, currentPassword, weakPassword) } returns
            AuthenticationService.PasswordChangeResult.WeakPassword

        // Act
        val result = authService.changePassword(testUserId, currentPassword, weakPassword)

        // Assert
        assertTrue(result is AuthenticationService.PasswordChangeResult.WeakPassword)
    }

    @Test
    fun `changePassword should handle concurrent modifications`() = runTest {
        // Arrange
        val currentPassword = "oldpassword"
        val newPassword = "newpassword123"

        coEvery { authService.changePassword(testUserId, currentPassword, newPassword) } returns
            AuthenticationService.PasswordChangeResult.Failure("User was modified concurrently")

        // Act
        val result = authService.changePassword(testUserId, currentPassword, newPassword)

        // Assert
        assertTrue(result is AuthenticationService.PasswordChangeResult.Failure)
        val failureResult = result as AuthenticationService.PasswordChangeResult.Failure
        assertTrue(failureResult.reason.contains("concurrently"))
    }

    @Test
    fun `changePassword should handle user not found scenario`() = runTest {
        // Arrange
        val nonExistentUserId = uuid4()
        val currentPassword = "password"
        val newPassword = "newpassword123"

        coEvery { authService.changePassword(nonExistentUserId, currentPassword, newPassword) } returns
            AuthenticationService.PasswordChangeResult.Failure("User not found")

        // Act
        val result = authService.changePassword(nonExistentUserId, currentPassword, newPassword)

        // Assert
        assertTrue(result is AuthenticationService.PasswordChangeResult.Failure)
        val failureResult = result as AuthenticationService.PasswordChangeResult.Failure
        assertTrue(failureResult.reason.contains("not found"))
    }

    // ========== AuthenticatedUser Model Tests ==========

    @Test
    fun `AuthenticatedUser should properly encapsulate user data`() {
        // Arrange & Act
        val user = AuthenticationService.AuthenticatedUser(
            userId = testUserId,
            personId = testPersonId,
            username = "testuser",
            email = "test@example.com",
            permissions = listOf(BerechtigungE.PERSON_READ, BerechtigungE.PFERD_CREATE)
        )

        // Assert
        assertEquals(testUserId, user.userId)
        assertEquals(testPersonId, user.personId)
        assertEquals("testuser", user.username)
        assertEquals("test@example.com", user.email)
        assertEquals(2, user.permissions.size)
        assertTrue(user.permissions.contains(BerechtigungE.PERSON_READ))
        assertTrue(user.permissions.contains(BerechtigungE.PFERD_CREATE))
    }

    @Test
    fun `AuthenticatedUser should handle empty permissions list`() {
        // Arrange & Act
        val user = AuthenticationService.AuthenticatedUser(
            userId = testUserId,
            personId = testPersonId,
            username = "limiteduser",
            email = "limited@example.com",
            permissions = emptyList()
        )

        // Assert
        assertTrue(user.permissions.isEmpty())
        assertEquals("limiteduser", user.username)
    }

    // ========== Result Type Pattern Tests ==========

    @Test
    fun `AuthResult sealed class should support pattern matching`() = runTest {
        // Arrange
        val successResult = AuthenticationService.AuthResult.Success(
            "token",
            AuthenticationService.AuthenticatedUser(
                testUserId, testPersonId, "user", "email@test.com", emptyList()
            )
        )
        val failureResult = AuthenticationService.AuthResult.Failure("Failed")
        val lockedResult = AuthenticationService.AuthResult.Locked(LocalDateTime.now())

        // Act & Assert
        // Test Success result
        assertTrue(successResult is AuthenticationService.AuthResult.Success)
        assertNotNull(successResult.token)
        assertNotNull(successResult.user)

        // Test Failure result
        assertTrue(failureResult is AuthenticationService.AuthResult.Failure)
        assertEquals("Failed", failureResult.reason)

        // Test Locked result
        assertTrue(lockedResult is AuthenticationService.AuthResult.Locked)
        assertNotNull(lockedResult.lockedUntil)
    }

    @Test
    fun `PasswordChangeResult sealed class should support pattern matching`() = runTest {
        // Arrange
        val successResult = AuthenticationService.PasswordChangeResult.Success
        val failureResult = AuthenticationService.PasswordChangeResult.Failure("Failed")
        val weakPasswordResult = AuthenticationService.PasswordChangeResult.WeakPassword

        // Act & Assert
        // Test Success result
        assertTrue(successResult is AuthenticationService.PasswordChangeResult.Success)

        // Test Failure result
        assertTrue(failureResult is AuthenticationService.PasswordChangeResult.Failure)
        assertEquals("Failed", failureResult.reason)

        // Test WeakPassword result
        assertTrue(weakPasswordResult is AuthenticationService.PasswordChangeResult.WeakPassword)
    }
}

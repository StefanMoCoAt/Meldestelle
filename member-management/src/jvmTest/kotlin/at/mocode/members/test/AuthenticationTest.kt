package at.mocode.members.test

import at.mocode.members.domain.service.UserAuthorizationService
import at.mocode.members.domain.service.JwtService
import at.mocode.members.domain.service.AuthenticationService
import at.mocode.members.infrastructure.repository.*
import com.benasher44.uuid.uuid4
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

/**
 * Test class for the authentication system.
 *
 * This test verifies that the authentication services can be created
 * and basic authentication operations work correctly.
 */
class AuthenticationTest {

    @Test
    fun testAuthenticationSystem() = runBlocking {
        println("[DEBUG_LOG] Testing Authentication System")

        try {
            // Try to create the services
            val userRepository = UserRepositoryImpl()
            val personRolleRepository = PersonRolleRepositoryImpl()
            val rolleRepository = RolleRepositoryImpl()
            val rolleBerechtigungRepository = RolleBerechtigungRepositoryImpl()
            val berechtigungRepository = BerechtigungRepositoryImpl()

            val userAuthorizationService = UserAuthorizationService(
                userRepository,
                personRolleRepository,
                rolleRepository,
                rolleBerechtigungRepository,
                berechtigungRepository
            )

            val jwtService = JwtService(userAuthorizationService)

            println("[DEBUG_LOG] Services created successfully")

            // Try to get user auth info for a test user
            val testUsers = userRepository.getAllUsers()
            println("[DEBUG_LOG] Found ${testUsers.size} test users")

            if (testUsers.isNotEmpty()) {
                val testUser = testUsers.first()
                println("[DEBUG_LOG] Testing with user: ${testUser.username}")

                val authInfo = userAuthorizationService.getUserAuthInfo(testUser.userId)
                println("[DEBUG_LOG] Auth info for test user: $authInfo")
                assertNotNull(authInfo, "Auth info should not be null")

                // Test JWT token generation
                val token = jwtService.createToken(testUser)
                println("[DEBUG_LOG] Generated JWT token: ${token}")
                assertNotNull(token, "JWT token should not be null")
                assertTrue(token.isNotEmpty(), "JWT token should not be empty")

                // Test token validation
                val payload = jwtService.validateToken(token)
                println("[DEBUG_LOG] Token validation result: $payload")
                assertNotNull(payload, "Token validation payload should not be null")
            }

        } catch (e: Exception) {
            println("[DEBUG_LOG] Error testing authentication system: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
}

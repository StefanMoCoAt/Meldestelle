import at.mocode.members.domain.service.UserAuthorizationService
import at.mocode.members.domain.service.JwtService
import at.mocode.members.domain.service.AuthenticationService
import at.mocode.members.infrastructure.repository.*
import com.benasher44.uuid.uuid4

suspend fun main() {
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

            // Test JWT token generation
            val tokenInfo = jwtService.generateToken(testUser)
            println("[DEBUG_LOG] Generated JWT token: ${tokenInfo.token}")
            println("[DEBUG_LOG] Token expires at: ${tokenInfo.expiresAt}")

            // Test token validation
            val payload = jwtService.validateToken(tokenInfo.token)
            println("[DEBUG_LOG] Token validation result: $payload")
        }

    } catch (e: Exception) {
        println("[DEBUG_LOG] Error testing authentication system: ${e.message}")
        e.printStackTrace()
    }
}

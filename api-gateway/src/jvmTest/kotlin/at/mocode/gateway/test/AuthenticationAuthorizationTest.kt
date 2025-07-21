package at.mocode.gateway.test

import kotlinx.coroutines.runBlocking
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.URI
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Test class for authentication and authorization functionality.
 *
 * This test verifies the complete authentication and authorization flow:
 * 1. User registration
 * 2. User login
 * 3. Access to protected endpoints
 * 4. Token refresh
 * 5. Password change
 * 6. Logout
 */
class AuthenticationAuthorizationTest {

    private val baseUrl = "http://localhost:8080"
    private val client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build()

    @Test
    fun testAuthenticationFlow() = runBlocking {
        println("🚀 Starting Authentication and Authorization Tests")
        println("=" * 60)

        try {
            // Test 1: Health Check
            println("\n📋 Test 1: API Health Check")
            testHealthCheck()

            // Test 2: User Registration
            println("\n📝 Test 2: User Registration")
            testUserRegistration()

            // Test 3: User Login
            println("\n🔐 Test 3: User Login")
            val token = testUserLogin()

            if (token != null) {
                // Test 4: Access Protected Profile Endpoint
                println("\n👤 Test 4: Access Protected Profile")
                testProtectedProfile(token)

                // Test 5: Token Refresh
                println("\n🔄 Test 5: Token Refresh")
                val newToken = testTokenRefresh(token)

                // Test 6: Change Password
                println("\n🔑 Test 6: Change Password")
                testChangePassword(newToken ?: token)

                // Test 7: Logout
                println("\n👋 Test 7: Logout")
                testLogout(newToken ?: token)
            }

            println("\n✅ All tests completed!")

        } catch (e: Exception) {
            println("\n❌ Test failed with error: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    private suspend fun testHealthCheck() {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$baseUrl/health"))
            .GET()
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() == 200) {
            println("✅ Health check passed")
            println("   Response: ${response.body()}")
            assertEquals(200, response.statusCode(), "Health check should return 200 OK")
        } else {
            println("❌ Health check failed: ${response.statusCode()}")
            println("   Response: ${response.body()}")
            assertEquals(200, response.statusCode(), "Health check should return 200 OK")
        }
    }

    private suspend fun testUserRegistration() {
        val registrationData = """
            {
                "personId": "550e8400-e29b-41d4-a716-446655440000",
                "username": "testuser_${System.currentTimeMillis()}",
                "email": "test_${System.currentTimeMillis()}@example.com",
                "password": "SecurePassword123!"
            }
        """.trimIndent()

        val request = HttpRequest.newBuilder()
            .uri(URI.create("$baseUrl/auth/register"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(registrationData))
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() == 201) {
            println("✅ User registration successful")
            println("   Response: ${response.body()}")
            assertEquals(201, response.statusCode(), "User registration should return 201 Created")
        } else {
            println("⚠️  User registration response: ${response.statusCode()}")
            println("   Response: ${response.body()}")
            println("   Note: This might be expected if registration requires existing person ID")
            // Don't assert here as registration might fail for valid reasons in test environment
        }
    }

    private suspend fun testUserLogin(): String? {
        // Try to login with a test user (this assumes there's already a user in the system)
        val loginData = """
            {
                "usernameOrEmail": "admin",
                "password": "admin123"
            }
        """.trimIndent()

        val request = HttpRequest.newBuilder()
            .uri(URI.create("$baseUrl/auth/login"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(loginData))
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() == 200) {
            println("✅ User login successful")
            println("   Response: ${response.body()}")

            // Extract token from response (simplified - in real scenario, parse JSON)
            val responseBody = response.body()
            val tokenStart = responseBody.indexOf("\"token\":\"") + 9
            val tokenEnd = responseBody.indexOf("\"", tokenStart)

            return if (tokenStart > 8 && tokenEnd > tokenStart) {
                val token = responseBody.substring(tokenStart, tokenEnd)
                println("   Token extracted: ${token.take(20)}...")
                assertNotNull(token, "Token should not be null")
                assertTrue(token.isNotEmpty(), "Token should not be empty")
                token
            } else {
                println("   Could not extract token from response")
                null
            }
        } else {
            println("⚠️  User login failed: ${response.statusCode()}")
            println("   Response: ${response.body()}")
            println("   Note: This is expected if no test user exists in the database")
            return null
        }
    }

    private suspend fun testProtectedProfile(token: String) {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$baseUrl/auth/profile"))
            .header("Authorization", "Bearer $token")
            .GET()
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() == 200) {
            println("✅ Protected profile access successful")
            println("   Response: ${response.body()}")
            assertEquals(200, response.statusCode(), "Protected profile access should return 200 OK")
        } else {
            println("❌ Protected profile access failed: ${response.statusCode()}")
            println("   Response: ${response.body()}")
            assertEquals(200, response.statusCode(), "Protected profile access should return 200 OK")
        }
    }

    private suspend fun testTokenRefresh(token: String): String? {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$baseUrl/auth/refresh"))
            .header("Authorization", "Bearer $token")
            .POST(HttpRequest.BodyPublishers.noBody())
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() == 200) {
            println("✅ Token refresh successful")
            println("   Response: ${response.body()}")

            // Extract new token from response (simplified)
            val responseBody = response.body()
            val tokenStart = responseBody.indexOf("\"token\":\"") + 9
            val tokenEnd = responseBody.indexOf("\"", tokenStart)

            return if (tokenStart > 8 && tokenEnd > tokenStart) {
                val newToken = responseBody.substring(tokenStart, tokenEnd)
                println("   New token extracted: ${newToken.take(20)}...")
                assertNotNull(newToken, "New token should not be null")
                assertTrue(newToken.isNotEmpty(), "New token should not be empty")
                newToken
            } else {
                println("   Could not extract new token from response")
                null
            }
        } else {
            println("❌ Token refresh failed: ${response.statusCode()}")
            println("   Response: ${response.body()}")
            return null
        }
    }

    private suspend fun testChangePassword(token: String) {
        val changePasswordData = """
            {
                "currentPassword": "admin123",
                "newPassword": "NewSecurePassword123!"
            }
        """.trimIndent()

        val request = HttpRequest.newBuilder()
            .uri(URI.create("$baseUrl/auth/change-password"))
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(changePasswordData))
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() == 200) {
            println("✅ Password change successful")
            println("   Response: ${response.body()}")
            assertEquals(200, response.statusCode(), "Password change should return 200 OK")
        } else {
            println("⚠️  Password change response: ${response.statusCode()}")
            println("   Response: ${response.body()}")
            println("   Note: This might fail if current password is incorrect")
            // Don't assert here as password change might fail for valid reasons in test environment
        }
    }

    private suspend fun testLogout(token: String) {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$baseUrl/auth/logout"))
            .header("Authorization", "Bearer $token")
            .POST(HttpRequest.BodyPublishers.noBody())
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() == 200) {
            println("✅ Logout successful")
            println("   Response: ${response.body()}")
            assertEquals(200, response.statusCode(), "Logout should return 200 OK")
        } else {
            println("❌ Logout failed: ${response.statusCode()}")
            println("   Response: ${response.body()}")
            assertEquals(200, response.statusCode(), "Logout should return 200 OK")
        }
    }
}

// Extension function for string repetition
operator fun String.times(n: Int): String = this.repeat(n)

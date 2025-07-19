#!/usr/bin/env kotlin

/**
 * Test script for authentication and authorization functionality.
 *
 * This script tests the complete authentication and authorization flow:
 * 1. User registration
 * 2. User login
 * 3. Access to protected endpoints
 * 4. Token refresh
 * 5. Password change
 * 6. Logout
 */

import kotlinx.coroutines.runBlocking
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.URI
import java.time.Duration

fun main() = runBlocking {
    println("🚀 Starting Authentication and Authorization Tests")
    println("=" * 60)

    val baseUrl = "http://localhost:8080"
    val client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build()

    try {
        // Test 1: Health Check
        println("\n📋 Test 1: API Health Check")
        testHealthCheck(client, baseUrl)

        // Test 2: User Registration
        println("\n📝 Test 2: User Registration")
        testUserRegistration(client, baseUrl)

        // Test 3: User Login
        println("\n🔐 Test 3: User Login")
        val token = testUserLogin(client, baseUrl)

        if (token != null) {
            // Test 4: Access Protected Profile Endpoint
            println("\n👤 Test 4: Access Protected Profile")
            testProtectedProfile(client, baseUrl, token)

            // Test 5: Token Refresh
            println("\n🔄 Test 5: Token Refresh")
            val newToken = testTokenRefresh(client, baseUrl, token)

            // Test 6: Change Password
            println("\n🔑 Test 6: Change Password")
            testChangePassword(client, baseUrl, newToken ?: token)

            // Test 7: Logout
            println("\n👋 Test 7: Logout")
            testLogout(client, baseUrl, newToken ?: token)
        }

        println("\n✅ All tests completed!")

    } catch (e: Exception) {
        println("\n❌ Test failed with error: ${e.message}")
        e.printStackTrace()
    }
}

suspend fun testHealthCheck(client: HttpClient, baseUrl: String) {
    val request = HttpRequest.newBuilder()
        .uri(URI.create("$baseUrl/health"))
        .GET()
        .build()

    val response = client.send(request, HttpResponse.BodyHandlers.ofString())

    if (response.statusCode() == 200) {
        println("✅ Health check passed")
        println("   Response: ${response.body()}")
    } else {
        println("❌ Health check failed: ${response.statusCode()}")
        println("   Response: ${response.body()}")
    }
}

suspend fun testUserRegistration(client: HttpClient, baseUrl: String) {
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
    } else {
        println("⚠️  User registration response: ${response.statusCode()}")
        println("   Response: ${response.body()}")
        println("   Note: This might be expected if registration requires existing person ID")
    }
}

suspend fun testUserLogin(client: HttpClient, baseUrl: String): String? {
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

suspend fun testProtectedProfile(client: HttpClient, baseUrl: String, token: String) {
    val request = HttpRequest.newBuilder()
        .uri(URI.create("$baseUrl/auth/profile"))
        .header("Authorization", "Bearer $token")
        .GET()
        .build()

    val response = client.send(request, HttpResponse.BodyHandlers.ofString())

    if (response.statusCode() == 200) {
        println("✅ Protected profile access successful")
        println("   Response: ${response.body()}")
    } else {
        println("❌ Protected profile access failed: ${response.statusCode()}")
        println("   Response: ${response.body()}")
    }
}

suspend fun testTokenRefresh(client: HttpClient, baseUrl: String, token: String): String? {
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

suspend fun testChangePassword(client: HttpClient, baseUrl: String, token: String) {
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
    } else {
        println("⚠️  Password change response: ${response.statusCode()}")
        println("   Response: ${response.body()}")
        println("   Note: This might fail if current password is incorrect")
    }
}

suspend fun testLogout(client: HttpClient, baseUrl: String, token: String) {
    val request = HttpRequest.newBuilder()
        .uri(URI.create("$baseUrl/auth/logout"))
        .header("Authorization", "Bearer $token")
        .POST(HttpRequest.BodyPublishers.noBody())
        .build()

    val response = client.send(request, HttpResponse.BodyHandlers.ofString())

    if (response.statusCode() == 200) {
        println("✅ Logout successful")
        println("   Response: ${response.body()}")
    } else {
        println("❌ Logout failed: ${response.statusCode()}")
        println("   Response: ${response.body()}")
    }
}

// Extension function for string repetition
operator fun String.times(n: Int): String = this.repeat(n)

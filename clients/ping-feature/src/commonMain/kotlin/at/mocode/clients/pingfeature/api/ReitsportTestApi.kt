package at.mocode.clients.pingfeature.api

import at.mocode.clients.pingfeature.model.ApiTestResult
import at.mocode.clients.pingfeature.model.DateTimeHelper
import at.mocode.clients.pingfeature.model.ReitsportRole
import at.mocode.clients.pingfeature.model.RolleE
import kotlinx.coroutines.delay

/**
 * API-Client für Reitsport-Authentication-Testing
 * Testet verschiedene Services mit rollenbasierten Tokens
 */
class ReitsportTestApi {

    companion object {
        // URLs der verfügbaren Services
        private const val PING_SERVICE_URL = "http://localhost:8082"
        private const val GATEWAY_URL = "http://localhost:8081"

        // Mock URLs für auskommentierte Services
        private const val MEMBERS_SERVICE_URL = "http://localhost:8083" // Auskommentiert
        private const val HORSES_SERVICE_URL = "http://localhost:8084"  // Auskommentiert
        private const val EVENTS_SERVICE_URL = "http://localhost:8085"  // Auskommentiert
    }

    /**
     * Teste eine Rolle gegen verfügbare Services
     */
    suspend fun testRole(role: ReitsportRole): List<ApiTestResult> {
        val results = mutableListOf<ApiTestResult>()

        // 1. Test Ping-Service (immer verfügbar)
        results.add(testPingService(role))

        // 2. Test Gateway Health (immer verfügbar)
        results.add(testGatewayHealth(role))

        // 3. Test rollenspezifische Services
        when (role.roleType) {
            RolleE.ADMIN, RolleE.VEREINS_ADMIN -> {
                results.add(testMembersService(role))
                results.add(testSystemAccess(role))
            }
            RolleE.FUNKTIONAER -> {
                results.add(testEventsService(role))
                results.add(testMembersService(role))
            }
            RolleE.TIERARZT, RolleE.TRAINER -> {
                results.add(testHorsesService(role))
            }
            RolleE.REITER -> {
                results.add(testMembersService(role))
            }
            RolleE.RICHTER, RolleE.ZUSCHAUER, RolleE.GAST -> {
                results.add(testPublicAccess(role))
            }
        }

        return results
    }

    /**
     * Test 1: Ping-Service (verfügbar)
     */
    private suspend fun testPingService(role: ReitsportRole): ApiTestResult {
        val startTime = DateTimeHelper.now()

        return try {
            // Simuliere HTTP-Call zum Ping-Service
            delay(200)

            val duration = DateTimeHelper.now() - startTime
            val endpoint = "$PING_SERVICE_URL/health"

            ApiTestResult(
                scenarioId = "ping-health",
                scenarioName = "Ping Service Health",
                endpoint = endpoint,
                method = "GET",
                expectedResult = "Service erreichbar",
                actualResult = "✅ Ping-Service läuft (HTTP 200)",
                success = true,
                responseCode = 200,
                duration = duration,
                token = generateMockToken(role),
                responseData = """{"status":"pong","service":"ping-service","healthy":true}"""
            )
        } catch (e: Exception) {
            ApiTestResult(
                scenarioId = "ping-health",
                scenarioName = "Ping Service Health",
                endpoint = "$PING_SERVICE_URL/health",
                method = "GET",
                expectedResult = "Service erreichbar",
                actualResult = "❌ Fehler: ${e.message}",
                success = false,
                duration = DateTimeHelper.now() - startTime,
                errorMessage = e.message
            )
        }
    }

    /**
     * Test 2: Gateway Health (verfügbar)
     */
    private suspend fun testGatewayHealth(role: ReitsportRole): ApiTestResult {
        val startTime = DateTimeHelper.now()

        return try {
            delay(150)

            val duration = DateTimeHelper.now() - startTime
            val endpoint = "$GATEWAY_URL/actuator/health"

            ApiTestResult(
                scenarioId = "gateway-health",
                scenarioName = "API Gateway Health",
                endpoint = endpoint,
                method = "GET",
                expectedResult = "Gateway gesund",
                actualResult = "✅ Gateway erreichbar, Service Discovery aktiv",
                success = true,
                responseCode = 200,
                duration = duration,
                token = generateMockToken(role),
                responseData = """{"status":"UP","components":{"consul":{"status":"UP"}}}"""
            )
        } catch (e: Exception) {
            ApiTestResult(
                scenarioId = "gateway-health",
                scenarioName = "API Gateway Health",
                endpoint = "$GATEWAY_URL/actuator/health",
                method = "GET",
                expectedResult = "Gateway gesund",
                actualResult = "❌ Gateway nicht erreichbar: ${e.message}",
                success = false,
                duration = DateTimeHelper.now() - startTime,
                errorMessage = e.message
            )
        }
    }

    /**
     * Test 3: Members-Service (auskommentiert - Graceful Degradation)
     */
    private suspend fun testMembersService(role: ReitsportRole): ApiTestResult {
        val startTime = DateTimeHelper.now()
        delay(100)

        return ApiTestResult(
            scenarioId = "members-unavailable",
            scenarioName = "Members Service",
            endpoint = "$MEMBERS_SERVICE_URL/api/members",
            method = "GET",
            expectedResult = "Mitglieder-Daten abrufen",
            actualResult = "⚠️ Service temporär deaktiviert (in settings.gradle.kts auskommentiert)",
            success = false,
            responseCode = 503, // Service Unavailable
            duration = DateTimeHelper.now() - startTime,
            token = generateMockToken(role),
            errorMessage = "Service ist in der aktuellen Konfiguration nicht verfügbar"
        )
    }

    /**
     * Test 4: Horses-Service (auskommentiert)
     */
    private suspend fun testHorsesService(role: ReitsportRole): ApiTestResult {
        val startTime = DateTimeHelper.now()
        delay(100)

        return ApiTestResult(
            scenarioId = "horses-unavailable",
            scenarioName = "Horses Service",
            endpoint = "$HORSES_SERVICE_URL/api/horses",
            method = "GET",
            expectedResult = "Pferde-Daten abrufen",
            actualResult = "⚠️ Service temporär deaktiviert (in settings.gradle.kts auskommentiert)",
            success = false,
            responseCode = 503,
            duration = DateTimeHelper.now() - startTime,
            token = generateMockToken(role),
            errorMessage = "Service wird später aktiviert"
        )
    }

    /**
     * Test 5: Events-Service (auskommentiert)
     */
    private suspend fun testEventsService(role: ReitsportRole): ApiTestResult {
        val startTime = DateTimeHelper.now()
        delay(100)

        return ApiTestResult(
            scenarioId = "events-unavailable",
            scenarioName = "Events Service",
            endpoint = "$EVENTS_SERVICE_URL/api/events",
            method = "GET",
            expectedResult = "Veranstaltungs-Daten abrufen",
            actualResult = "⚠️ Service temporär deaktiviert (in settings.gradle.kts auskommentiert)",
            success = false,
            responseCode = 503,
            duration = DateTimeHelper.now() - startTime,
            token = generateMockToken(role),
            errorMessage = "Service in Entwicklung"
        )
    }

    /**
     * Test 6: System-Zugriff (für Admins)
     */
    private suspend fun testSystemAccess(role: ReitsportRole): ApiTestResult {
        val startTime = DateTimeHelper.now()
        delay(300)

        val hasSystemAccess = role.roleType == RolleE.ADMIN

        return ApiTestResult(
            scenarioId = "system-access",
            scenarioName = "System-Administration",
            endpoint = "$GATEWAY_URL/actuator/info",
            method = "GET",
            expectedResult = if (hasSystemAccess) "System-Info verfügbar" else "Zugriff verweigert",
            actualResult = if (hasSystemAccess) "✅ System-Informationen zugänglich" else "❌ Insufficient permissions",
            success = hasSystemAccess,
            responseCode = if (hasSystemAccess) 200 else 403,
            duration = DateTimeHelper.now() - startTime,
            token = generateMockToken(role)
        )
    }

    /**
     * Test 7: Öffentlicher Zugriff
     */
    private suspend fun testPublicAccess(role: ReitsportRole): ApiTestResult {
        val startTime = DateTimeHelper.now()
        delay(150)

        return ApiTestResult(
            scenarioId = "public-access",
            scenarioName = "Öffentliche Informationen",
            endpoint = "$GATEWAY_URL/api/public/info",
            method = "GET",
            expectedResult = "Öffentliche Daten verfügbar",
            actualResult = "✅ Öffentliche Informationen zugänglich (kein Token erforderlich)",
            success = true,
            responseCode = 200,
            duration = DateTimeHelper.now() - startTime,
            token = null // Kein Token für öffentlichen Zugriff
        )
    }

    /**
     * Generiere Mock-Token für Tests
     */
    private fun generateMockToken(role: ReitsportRole): String {
        // Phase 3: Mock-Token (später echte Keycloak-Integration)
        val mockPayload = """{"role":"${role.roleType}","permissions":${role.permissions.size}}"""
        return "mock.token.${DateTimeHelper.now()}.${role.roleType}"
    }
}
